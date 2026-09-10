package com.pickcode.v2.ui.components

import android.widget.Toast
import androidx.compose.ui.tooling.preview.Preview
import com.pickcode.v2.ui.theme.PickCodeTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pickcode.v2.domain.model.PackageCode
import com.pickcode.v2.ui.theme.successColor
import com.pickcode.v2.ui.util.formatCode
import com.pickcode.v2.ui.util.getCompanyShortName

/**
 * 取件码卡片：整卡点击复制，右侧「标记已取」+ 更多菜单（复制/编辑/已取/删除）。
 * 用 Surface + 描边而不是 Card + 阴影，保持列表滚动性能。
 */
@Composable
fun CodeCard(
    item: PackageCode,
    onTogglePicked: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var menuOpen by remember { mutableStateOf(false) }

    fun copyCode() {
        clipboard.setText(AnnotatedString(item.code))
        Toast.makeText(context, "已复制取件码 ${item.code}", Toast.LENGTH_SHORT).show()
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = colorScheme.surface,
        border = BorderStroke(1.dp, colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .clickable { copyCode() }
                .padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 快递公司首字
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (item.isPicked) colorScheme.surfaceVariant else colorScheme.primaryContainer,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getCompanyShortName(item.company),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isPicked) colorScheme.onSurfaceVariant else colorScheme.onPrimaryContainer
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatCode(item.code),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            textDecoration = if (item.isPicked) TextDecoration.LineThrough else null
                        ),
                        color = if (item.isPicked) colorScheme.onSurfaceVariant else colorScheme.onSurface,
                        maxLines = 1
                    )
                    if (item.isPicked) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            shape = MaterialTheme.shapes.extraSmall,
                            color = colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "已取",
                                style = MaterialTheme.typography.labelSmall,
                                color = colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                if (item.company.isNotEmpty()) {
                    Text(
                        text = item.company,
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                if (item.address.isNotEmpty() && item.address != "手动添加") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = colorScheme.outline,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = item.address,
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.outline,
                            maxLines = 1
                        )
                    }
                }

                if (item.tags.isNotEmpty()) {
                    Row(modifier = Modifier.padding(top = 4.dp)) {
                        item.tags.forEach { tag ->
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = colorScheme.secondaryContainer,
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                if (item.remark.isNotEmpty()) {
                    Text(
                        text = item.remark,
                        style = MaterialTheme.typography.labelSmall,
                        color = colorScheme.outline,
                        maxLines = 2
                    )
                }
            }

            IconButton(onClick = onTogglePicked, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = if (item.isPicked) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                    contentDescription = if (item.isPicked) "标记未取" else "标记已取",
                    tint = if (item.isPicked) successColor() else colorScheme.outline,
                    modifier = Modifier.size(22.dp)
                )
            }

            Box {
                IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(40.dp)) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "更多操作",
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                // 只在展开时组合菜单，避免长列表里每个 item 都多挂一层
                if (menuOpen) {
                    DropdownMenu(expanded = true, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("复制取件码") },
                            leadingIcon = { Icon(Icons.Outlined.ContentCopy, contentDescription = null) },
                            onClick = { menuOpen = false; copyCode() }
                        )
                        DropdownMenuItem(
                            text = { Text("编辑") },
                            leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                            onClick = { menuOpen = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = { Text(if (item.isPicked) "标记为未取" else "标记为已取") },
                            leadingIcon = {
                                Icon(
                                    if (item.isPicked) Icons.Outlined.CheckCircle else Icons.Filled.CheckCircle,
                                    contentDescription = null
                                )
                            },
                            onClick = { menuOpen = false; onTogglePicked() }
                        )
                        DropdownMenuItem(
                            text = { Text("删除", color = colorScheme.error) },
                            leadingIcon = {
                                Icon(Icons.Outlined.Delete, contentDescription = null, tint = colorScheme.error)
                            },
                            onClick = { menuOpen = false; onDelete() }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun CodeCardPreview() {
    PickCodeTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CodeCard(
                item = PackageCode(
                    id = 1,
                    code = "8-2-3048",
                    date = "2026-09-10 10:00:00",
                    sendDate = "2026-09-10 10:00:00",
                    company = "菜鸟驿站",
                    address = "3 号楼架空层",
                    tags = listOf("家"),
                    remark = "两件一起取"
                ),
                onTogglePicked = {}, onEdit = {}, onDelete = {}
            )
            CodeCard(
                item = PackageCode(
                    id = 2,
                    code = "C-5-178",
                    date = "2026-09-10 09:00:00",
                    sendDate = "2026-09-10 09:00:00",
                    company = "丰巢",
                    address = "小区南门丰巢柜",
                    isPicked = true
                ),
                onTogglePicked = {}, onEdit = {}, onDelete = {}
            )
        }
    }
}
