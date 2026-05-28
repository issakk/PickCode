package com.pickcode.v2.data.local.dao

import androidx.room.*
import com.pickcode.v2.data.local.entity.PackageCodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PackageCodeDao {
    @Query("SELECT * FROM package_codes ORDER BY date DESC")
    fun getAll(): Flow<List<PackageCodeEntity>>

    @Query("SELECT * FROM package_codes WHERE id = :id")
    suspend fun getById(id: Long): PackageCodeEntity?

    @Query("SELECT * FROM package_codes WHERE code = :code AND date LIKE :datePrefix || '%'")
    suspend fun findByCodeAndDate(code: String, datePrefix: String): PackageCodeEntity?

    @Insert
    suspend fun insert(entity: PackageCodeEntity): Long

    @Update
    suspend fun update(entity: PackageCodeEntity)

    @Delete
    suspend fun delete(entity: PackageCodeEntity)

    @Query("DELETE FROM package_codes WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("DELETE FROM package_codes")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM package_codes")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM package_codes WHERE isPicked = 1")
    suspend fun getPickedCount(): Int
}
