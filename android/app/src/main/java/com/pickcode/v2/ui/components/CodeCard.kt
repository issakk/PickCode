package com.pickcode.v2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pickcode.v2.domain.model.PackageCode
import com.pickcode.v2.ui.theme.success
import com.pickcode.v2.ui.util.formatCode
import com.pickcode.v2.ui.util.getCompanyShortName

@Composable
fun CodeCard(
    item: PackageCode,
    onTogglePicked: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colorScheme.surface,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        if (item.isPicked) colorScheme.outline else colorScheme.primary,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getCompanyShortName(item.company),
                    color = colorScheme.onPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatCode(item.code),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isPicked) colorScheme.outline else colorScheme.onSurface,
                    letterSpacing = (-0.3).sp
                )
                if (item.company.isNotEmpty()) {
                    Text(text = item.company, fontSize = 11.sp, color = colorScheme.onSurfaceVariant)
                }
                if (item.address.isNotEmpty() && item.address != "手动添加") {
                    Text(text = item.address, fontSize = 11.sp, color = colorScheme.outline)
                }
                if (item.tags.isNotEmpty()) {
                    Row(modifier = Modifier.padding(top = 3.dp)) {
                        item.tags.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = colorScheme.primaryContainer,
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 10.sp,
                                    color = colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
                if (item.remark.isNotEmpty()) {
                    Text(text = item.remark, fontSize = 11.sp, color = colorScheme.outline)
                }
            }

            Checkbox(
                checked = item.isPicked,
                onCheckedChange = { onTogglePicked() },
                modifier = Modifier.size(36.dp),
                colors = CheckboxDefaults.colors(checkedColor = colorScheme.success)
            )

            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.MoreVert, contentDescription = "更多", modifier = Modifier.size(18.dp))
            }
        }
    }
}
