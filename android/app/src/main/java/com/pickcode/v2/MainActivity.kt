package com.pickcode.v2

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pickcode.v2.di.ApplicationScope
import com.pickcode.v2.domain.engine.CodeImporter
import com.pickcode.v2.navigation.AppNavigation
import com.pickcode.v2.ui.theme.PickCodeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var codeImporter: CodeImporter

    @Inject
    @ApplicationScope
    lateinit var appScope: CoroutineScope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PickCodeTheme {
                AppNavigation()
            }
        }
        handleShare(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleShare(intent)
    }

    /** 别的 App「分享文本」过来时直接抽码入库，不用先打开界面再粘贴 */
    private fun handleShare(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return
        val text = intent.getStringExtra(Intent.EXTRA_TEXT)?.takeIf { it.isNotBlank() } ?: return

        appScope.launch {
            val message = runCatching { codeImporter.importFromText(text) }
                .fold(
                    onSuccess = { outcome ->
                        when (outcome) {
                            is CodeImporter.Outcome.NoRules -> "请先在「我的 → 匹配设置」里添加匹配规则"
                            is CodeImporter.Outcome.Done ->
                                if (outcome.addedCount > 0) {
                                    "已导入 ${outcome.addedCount} 个取件码"
                                } else {
                                    "这段文本里没有新的取件码"
                                }
                        }
                    },
                    onFailure = { "导入失败: ${it.message}" }
                )
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
            }
        }
    }
}
