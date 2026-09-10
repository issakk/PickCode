package com.pickcode.v2.data.local.dao

import androidx.room.*
import com.pickcode.v2.data.local.entity.PackageCodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PackageCodeDao {
    @Query("SELECT * FROM package_codes ORDER BY date DESC")
    fun getAll(): Flow<List<PackageCodeEntity>>

    @Query("SELECT * FROM package_codes ORDER BY date DESC")
    suspend fun getAllOnce(): List<PackageCodeEntity>

    @Query("SELECT * FROM package_codes WHERE id = :id")
    suspend fun getById(id: Long): PackageCodeEntity?

    /** 同 (code, date) 已存在时返回 -1，依赖唯一索引拦截重复入库。 */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: PackageCodeEntity): Long

    @Update
    suspend fun update(entity: PackageCodeEntity)

    @Delete
    suspend fun delete(entity: PackageCodeEntity)

    @Query("DELETE FROM package_codes WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM package_codes")
    suspend fun deleteAll()
}
