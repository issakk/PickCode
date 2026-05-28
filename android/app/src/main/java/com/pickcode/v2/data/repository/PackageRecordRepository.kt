package com.pickcode.v2.data.repository

import com.pickcode.v2.data.local.dao.PackageRecordDao
import com.pickcode.v2.data.local.entity.PackageRecordEntity
import com.pickcode.v2.domain.model.PackageRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PackageRecordRepository @Inject constructor(
    private val dao: PackageRecordDao
) {
    fun getAll(): Flow<List<PackageRecord>> = dao.getAll().map { list -> list.map { it.toDomain() } }

    suspend fun getById(id: Long): PackageRecord? = dao.getById(id)?.toDomain()

    suspend fun insert(record: PackageRecord): Long = dao.insert(record.toEntity())

    suspend fun update(record: PackageRecord) = dao.update(record.toEntity())

    suspend fun delete(record: PackageRecord) = dao.delete(record.toEntity())
}

private fun PackageRecordEntity.toDomain() = PackageRecord(
    id = id, platform = platform, name = name,
    price = price, time = time, checked = checked, date = date
)

private fun PackageRecord.toEntity() = PackageRecordEntity(
    id = id, platform = platform, name = name,
    price = price, time = time, checked = checked, date = date
)
