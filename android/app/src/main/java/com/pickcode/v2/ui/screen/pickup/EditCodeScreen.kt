package com.pickcode.v2.ui.screen.pickup

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.pickcode.v2.ui.components.GradientHeader
import kotlinx.coroutines.launch
import com.pickcode.v2.ui.components.TagGrid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCodeScreen(
    navController: NavHostController,
    viewModel: EditCodeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        GradientHeader(title = "编辑取件码", onBack = { navController.popBackStack() })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Code field
            OutlinedTextField(
                value = uiState.code,
                onValueChange = { viewModel.updateCode(it) },
                label = { Text("取件码") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            // Company field
            OutlinedTextField(
                value = uiState.company,
                onValueChange = { viewModel.updateCompany(it) },
                label = { Text("物流公司") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            // Tags section
            Text("标签", style = MaterialTheme.typography.titleMedium)
            TagGrid(
                tags = uiState.tagOptions,
                selectedTags = uiState.selectedTags,
                onToggle = { viewModel.toggleTag(it) },
                onLongPress = { viewModel.deleteTag(it) }
            )

            // Custom tag input
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = uiState.customTag,
                    onValueChange = { viewModel.updateCustomTag(it) },
                    label = { Text("自定义标签") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
            Text(
                text = "长按标签可删除",
                style = MaterialTheme.typography.labelSmall,
                color = colorScheme.outline
            )
                Spacer(Modifier.width(8.dp))
                Button(onClick = { viewModel.addCustomTag() }) {
                    Text("添加")
                }
            }

            // Remark
            OutlinedTextField(
                value = uiState.remark,
                onValueChange = { viewModel.updateRemark(it) },
                label = { Text("备注") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(8.dp)
            )

            // Picked switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("标记已取", style = MaterialTheme.typography.titleMedium)
                Switch(
                    checked = uiState.isPicked,
                    onCheckedChange = { viewModel.updatePicked(it) }
                )
            }

            // Save button
            Button(
                onClick = {
                    scope.launch {
                        if (viewModel.save()) {
                            navController.popBackStack()
                        } else {
                            Toast.makeText(context, "请输入取件码", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Text("保存", modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}
