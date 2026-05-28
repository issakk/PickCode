package com.pickcode.v2.data.repository

import com.pickcode.v2.data.local.dao.PackageCodeDao
import com.pickcode.v2.data.local.entity.PackageCodeEntity
import com.pickcode.v2.domain.model.PackageCode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PackageCodeRepository @Inject constructor(
    private val dao: PackageCodeDao
) {
    fun getAll(): Flow<List<PackageCode>> = dao.getAll().map { list -> list.map { it.toDomain() } }

    suspend fun getById(id: Long): PackageCode? = dao.getById(id)?.toDomain()

    suspend fun insert(code: PackageCode): Long = dao.insert(code.toEntity())

    suspend fun update(code: PackageCode) = dao.update(code.toEntity())

    suspend fun delete(code: PackageCode) = dao.delete(code.toEntity())

    suspend fun deleteByIds(ids: List<Long>) = dao.deleteByIds(ids)

    suspend fun deleteAll() = dao.deleteAll()

    suspend fun findByCodeAndDate(code: String, datePrefix: String): PackageCode? =
        dao.findByCodeAndDate(code, datePrefix)?.toDomain()

    suspend fun getStats(): Pair<Int, Int> = Pair(dao.getCount(), dao.getPickedCount())
}

private fun PackageCodeEntity.toDomain() = PackageCode(
    id = id, code = code, date = date, sendDate = sendDate,
    company = company, address = address, isPicked = isPicked,
    isManual = isManual, tags = tags, remark = remark
)

private fun PackageCode.toEntity() = PackageCodeEntity(
    id = id, code = code, date = date, sendDate = sendDate,
    company = company, address = address, isPicked = isPicked,
    isManual = isManual, tags = tags, remark = remark
)
