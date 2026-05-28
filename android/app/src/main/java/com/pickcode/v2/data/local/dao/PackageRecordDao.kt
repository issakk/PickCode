package com.pickcode.v2.data.local.dao

import androidx.room.*
import com.pickcode.v2.data.local.entity.PackageRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PackageRecordDao {
    @Query("SELECT * FROM package_records ORDER BY date DESC, time DESC")
    fun getAll(): Flow<List<PackageRecordEntity>>

    @Query("SELECT * FROM package_records WHERE id = :id")
    suspend fun getById(id: Long): PackageRecordEntity?

    @Insert
    suspend fun insert(entity: PackageRecordEntity): Long

    @Update
    suspend fun update(entity: PackageRecordEntity)

    @Delete
    suspend fun delete(entity: PackageRecordEntity)

    @Query("DELETE FROM package_records WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}
