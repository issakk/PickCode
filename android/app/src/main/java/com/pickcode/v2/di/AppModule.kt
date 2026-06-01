package com.pickcode.v2.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pickcode.v2.data.datastore.SettingsDataStore
import com.pickcode.v2.data.local.AppDatabase
import com.pickcode.v2.data.local.dao.MatchRuleDao
import com.pickcode.v2.data.local.dao.PackageCodeDao
import com.pickcode.v2.data.local.dao.PackageRecordDao
import com.pickcode.v2.domain.engine.MatchEngine
import com.pickcode.v2.domain.engine.SmsReader
import dagger.Module
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE match_rules ADD COLUMN keyword TEXT NOT NULL DEFAULT ''")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "pickcode.db")
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun providePackageCodeDao(db: AppDatabase): PackageCodeDao = db.packageCodeDao()

    @Provides
    fun providePackageRecordDao(db: AppDatabase): PackageRecordDao = db.packageRecordDao()

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
    }
}
