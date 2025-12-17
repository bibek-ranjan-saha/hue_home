package com.huehome.ui.selection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.Window
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.huehome.core.domain.model.ObjectType
import com.huehome.core.domain.model.SceneObject

/**
 * Object selection bottom sheet
 * Displays detected objects for user selection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectSelectionBottomSheet(
    objects: List<SceneObject>,
    selectedObject: SceneObject?,
    onObjectSelected: (SceneObject) -> Unit,
    onToggleObject: (SceneObject) -> Unit,
    onLabelObject: (SceneObject) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 300.dp, max = 600.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Detected Objects",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "${objects.size} objects found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                // Info icon
                IconButton(onClick = { /* Show help */ }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Object list
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(objects) { obj ->
                    ObjectCard(
                        sceneObject = obj,
                        isSelected = obj == selectedObject,
                        onSelect = { onObjectSelected(obj) },
                        onToggle = { onToggleObject(obj) },
                        onLabel = { onLabelObject(obj) }
                    )
                }
            }
        }
    }
}

/**
 * Individual object card
 */
@Composable
private fun ObjectCard(
    sceneObject: SceneObject,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onToggle: () -> Unit,
    onLabel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Object type icon
            Icon(
                imageVector = getObjectIcon(sceneObject.type),
                contentDescription = sceneObject.type.name,
                modifier = Modifier.size(40.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // Object info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = sceneObject.userLabel ?: getObjectTypeName(sceneObject.type),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Detected color
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(sceneObject.detectedColor.rgb))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                    )
                    
                    Text(
                        text = "Confidence: ${(sceneObject.detectedColor.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
                
                // Applied color if exists
                sceneObject.appliedColor?.let { appliedColor ->
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Applied color",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(appliedColor))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                        )
                        
                        Text(
                            text = "Color applied",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Action buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Toggle visibility
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (sceneObject.isActive) Icons.Default.Visibility 
                                     else Icons.Default.VisibilityOff,
                        contentDescription = if (sceneObject.isActive) "Hide" else "Show",
                        tint = if (sceneObject.isActive) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
                
                // Label object
                IconButton(
                    onClick = onLabel,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Label",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Get icon for object type
 */
private fun getObjectIcon(type: ObjectType) = when (type) {
    ObjectType.WALL -> Icons.Default.ViewInAr
    ObjectType.DOOR -> Icons.Default.MeetingRoom
    ObjectType.WINDOW -> Icons.Default.Window
    ObjectType.FLOOR -> Icons.Default.Layers
    ObjectType.CEILING -> Icons.Default.Layers
    ObjectType.UNKNOWN -> Icons.Default.QuestionMark
}

/**
 * Get display name for object type
 */
private fun getObjectTypeName(type: ObjectType) = when (type) {
    ObjectType.WALL -> "Wall"
    ObjectType.DOOR -> "Door"
    ObjectType.WINDOW -> "Window"
    ObjectType.FLOOR -> "Floor"
    ObjectType.CEILING -> "Ceiling"
    ObjectType.UNKNOWN -> "Unknown Object"
}
