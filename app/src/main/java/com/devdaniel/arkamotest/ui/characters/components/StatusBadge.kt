package com.devdaniel.arkamotest.ui.characters.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.devdaniel.arkamotest.domain.model.CharacterStatus

@Composable
fun StatusBadge(
    status: CharacterStatus,
    modifier: Modifier = Modifier,
) {
    val (color, label) = when (status) {
        CharacterStatus.Alive -> Color(0xFF2E7D32) to "Alive"
        CharacterStatus.Dead -> Color(0xFFC62828) to "Dead"
        CharacterStatus.Unknown -> Color(0xFF757575) to "Unknown"
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
