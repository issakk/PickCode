package com.pickcode.v2.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pickcode.v2.domain.model.PackageCode
import com.pickcode.v2.ui.theme.*
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
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Company icon circle
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (item.isPicked) Divider else Primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getCompanyShortName(item.company),
                    color = Surface,
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
                    color = if (item.isPicked) TextTertiary else TextPrimary,
                    letterSpacing = (-0.3).sp
                )
                if (item.company.isNotEmpty()) {
                    Text(text = item.company, fontSize = 11.sp, color = TextSecondary)
                }
                if (item.address.isNotEmpty() && item.address != "手动添加") {
                    Text(text = item.address, fontSize = 11.sp, color = TextTertiary)
                }
                if (item.tags.isNotEmpty()) {
                    Row(modifier = Modifier.padding(top = 3.dp)) {
                        item.tags.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = TagStartEndBg,
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 10.sp,
                                    color = TagStartEndText,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
                if (item.remark.isNotEmpty()) {
                    Text(text = item.remark, fontSize = 11.sp, color = TextTertiary)
                }
            }

            // Picked toggle
            Checkbox(
                checked = item.isPicked,
                onCheckedChange = { onTogglePicked() },
                modifier = Modifier.size(36.dp),
                colors = CheckboxDefaults.colors(checkedColor = SuccessGreen)
            )

            // Edit button
            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.MoreVert, contentDescription = "更多", modifier = Modifier.size(18.dp))
            }
        }
    }
}
