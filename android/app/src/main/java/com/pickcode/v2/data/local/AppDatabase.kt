package com.pickcode.v2.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pickcode.v2.data.local.converter.Converters
import com.pickcode.v2.data.local.dao.MatchRuleDao
import com.pickcode.v2.data.local.dao.PackageCodeDao
import com.pickcode.v2.data.local.entity.MatchRuleEntity
import com.pickcode.v2.data.local.entity.PackageCodeEntity

@Database(
    entities = [PackageCodeEntity::class, MatchRuleEntity::class],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun packageCodeDao(): PackageCodeDao
    abstract fun matchRuleDao(): MatchRuleDao
}
