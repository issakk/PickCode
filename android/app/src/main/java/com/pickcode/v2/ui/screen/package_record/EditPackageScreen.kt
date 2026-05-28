package com.pickcode.v2.ui.screen.package_record

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.pickcode.v2.ui.components.GradientHeader
import com.pickcode.v2.ui.components.PlatformIcon
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EditPackageScreen(
    navController: NavHostController,
    viewModel: EditPackageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxSize()) {
        GradientHeader(title = if (uiState.isEdit) "编辑包裹" else "添加包裹", onBack = { navController.popBackStack() })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Platform selector
            Text("选择平台", style = MaterialTheme.typography.titleMedium)
            val platforms = listOf("淘宝", "京东", "拼多多", "抖音", "唯品会", "小红书", "1688", "其他")
            val chunked = platforms.chunked(4)
            chunked.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { platform ->
                        val selected = uiState.platform == platform
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .padding(4.dp)
                                .clickable { viewModel.updatePlatform(platform) },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) colorScheme.primaryContainer else colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(if (selected) 2.dp else 0.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                PlatformIcon(platform, modifier = Modifier.size(32.dp))
                                Spacer(Modifier.height(4.dp))
                                Text(platform, fontSize = MaterialTheme.typography.bodySmall.fontSize)
                            }
                        }
                    }
                    repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }

            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("商品名称") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            OutlinedTextField(
                value = uiState.price,
                onValueChange = { viewModel.updatePrice(it) },
                label = { Text("价格（选填）") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            // Date picker
            OutlinedTextField(
                value = uiState.date,
                onValueChange = {},
                label = { Text("日期") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val cal = Calendar.getInstance()
                        DatePickerDialog(context, { _, y, m, d ->
                            viewModel.updateDate("$y-${(m + 1).toString().padStart(2, '0')}-${d.toString().padStart(2, '0')}")
                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                    },
                readOnly = true,
                enabled = false,
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    if (viewModel.save()) {
                        navController.popBackStack()
                    } else {
                        Toast.makeText(context, "请输入商品名称", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存", modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}
