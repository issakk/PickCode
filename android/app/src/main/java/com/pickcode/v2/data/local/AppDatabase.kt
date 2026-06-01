package com.pickcode.v2.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pickcode.v2.data.local.converter.Converters
import com.pickcode.v2.data.local.dao.MatchRuleDao
import com.pickcode.v2.data.local.dao.PackageCodeDao
import com.pickcode.v2.data.local.dao.PackageRecordDao
import com.pickcode.v2.data.local.entity.MatchRuleEntity
import com.pickcode.v2.data.local.entity.PackageCodeEntity
import com.pickcode.v2.data.local.entity.PackageRecordEntity

@Database(
    entities = [PackageCodeEntity::class, PackageRecordEntity::class, MatchRuleEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun packageCodeDao(): PackageCodeDao
    abstract fun packageRecordDao(): PackageRecordDao
    abstract fun matchRuleDao(): MatchRuleDao
}
