package com.pickcode.v2.data.local.dao

import androidx.room.*
import com.pickcode.v2.data.local.entity.MatchRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchRuleDao {
    @Query("SELECT * FROM match_rules ORDER BY createTime DESC")
    fun getAll(): Flow<List<MatchRuleEntity>>

    @Query("SELECT * FROM match_rules WHERE enabled = 1")
    suspend fun getEnabled(): List<MatchRuleEntity>

    @Query("SELECT * FROM match_rules WHERE id = :id")
    suspend fun getById(id: String): MatchRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MatchRuleEntity)

    @Update
    suspend fun update(entity: MatchRuleEntity)

    @Delete
    suspend fun delete(entity: MatchRuleEntity)

    @Query("SELECT COUNT(*) FROM match_rules WHERE name = :name AND id != :excludeId")
    suspend fun countByName(name: String, excludeId: String = ""): Int
}
