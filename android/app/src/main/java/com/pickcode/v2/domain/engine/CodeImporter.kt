package com.pickcode.v2.domain.engine

import com.pickcode.v2.data.repository.MatchRuleRepository
import com.pickcode.v2.data.repository.PackageCodeRepository
import com.pickcode.v2.domain.model.PackageCode
import com.pickcode.v2.ui.util.todayString
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 「文本 → 取件码 → 入库」的唯一入口：短信扫描、分享导入、手动添加都走这里，
 * 免得几处各写一遍去重和「未知快递/地址」兜底。重复由 (code, date) 唯一索引挡下。
 */
@Singleton
class CodeImporter @Inject constructor(
    private val packageCodeRepository: PackageCodeRepository,
    private val matchRuleRepository: MatchRuleRepository,
    private val matchEngine: MatchEngine,
    private val smsReader: SmsReader
) {

    sealed interface Outcome {
        /** 一条启用规则都没有，没法抽 */
        data object NoRules : Outcome

        data class Done(val addedCount: Int) : Outcome
    }

    /** 扫最近 daysBack 天的短信 */
    suspend fun importFromSms(daysBack: Int = 4): Outcome {
        val rules = matchRuleRepository.getEnabled()
        if (rules.isEmpty()) return Outcome.NoRules

        var added = 0
        for (msg in smsReader.readRecentSms(daysBack)) {
            val info = matchEngine.extractInfo(msg.content, rules)
            added += insertAll(
                info.codes.map { code ->
                    PackageCode(
                        code = code,
                        date = msg.sendDate,
                        sendDate = msg.sendDate,
                        company = info.express.ifEmpty { "未知快递" },
                        address = info.address.ifEmpty { "未知地址" },
                        isManual = false
                    )
                }
            )
        }
        return Outcome.Done(added)
    }

    /** 从分享进来或粘贴的文本里抽码，按今天入库 */
    suspend fun importFromText(text: String): Outcome {
        val rules = matchRuleRepository.getEnabled()
        if (rules.isEmpty()) return Outcome.NoRules

        val info = matchEngine.extractInfo(text, rules)
        val now = todayString()
        return Outcome.Done(
            insertAll(
                info.codes.map { code ->
                    PackageCode(
                        code = code,
                        date = now,
                        sendDate = now,
                        company = info.express.ifEmpty { "未知快递" },
                        address = info.address.ifEmpty { "未知地址" },
                        isManual = true
                    )
                }
            )
        )
    }

    private suspend fun insertAll(codes: List<PackageCode>): Int {
        val seen = mutableSetOf<Pair<String, String>>()
        var added = 0
        for (code in codes) {
            if (code.code.isEmpty() || !seen.add(code.code to code.date)) continue
            // 同 (code, date) 已存在时 insert 返回 -1
            if (packageCodeRepository.insert(code) != -1L) added++
        }
        return added
    }
}
