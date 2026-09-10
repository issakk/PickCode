package com.pickcode.v2.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pickcode.v2.data.datastore.SettingsDataStore
import com.pickcode.v2.data.local.AppDatabase
import com.pickcode.v2.data.local.dao.MatchRuleDao
import com.pickcode.v2.data.local.dao.PackageCodeDao
import com.pickcode.v2.domain.engine.MatchEngine
import com.pickcode.v2.domain.engine.SmsReader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier

/** 需要活过 Activity 的活儿（比如分享导入）用的作用域 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE match_rules ADD COLUMN keyword TEXT NOT NULL DEFAULT ''")
        }
    }

    /** 2 → 3：(code, date) 唯一索引（先删历史重复行）+ 删除已下线的 package_records 表。 */
    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "DELETE FROM package_codes WHERE id NOT IN " +
                    "(SELECT MIN(id) FROM package_codes GROUP BY code, date)"
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS index_package_codes_code_date " +
                    "ON package_codes (code, date)"
            )
            db.execSQL("DROP TABLE IF EXISTS package_records")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "pickcode.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()

    @Provides
    fun providePackageCodeDao(db: AppDatabase): PackageCodeDao = db.packageCodeDao()

    @Provides
    fun provideMatchRuleDao(db: AppDatabase): MatchRuleDao = db.matchRuleDao()

    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): SettingsDataStore =
        SettingsDataStore(context)

    @Provides
    @Singleton
    fun provideMatchEngine(): MatchEngine = MatchEngine()

    @Provides
    @Singleton
    fun provideSmsReader(@ApplicationContext context: Context): SmsReader = SmsReader(context)

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
        // 没有超时的话 AI 接口一旦挂住，界面会一直转圈
        install(HttpTimeout) {
            connectTimeoutMillis = 15_000
            requestTimeoutMillis = 60_000
            socketTimeoutMillis = 60_000
        }
    }

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
