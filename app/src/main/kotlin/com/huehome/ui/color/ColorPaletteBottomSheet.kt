package com.huehome.ui.color

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.huehome.core.domain.model.ColorRecommendation
import com.huehome.core.domain.model.RecommendationCategory

/**
 * Color palette bottom sheet
 * Displays AI-generated color recommendations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPaletteBottomSheet(
    recommendations: List<ColorRecommendation>,
    selectedColor: ColorRecommendation?,
    onColorSelected: (ColorRecommendation) -> Unit,
    onApply: () -> Unit,
    onPreview: () -> Unit,
    onCompare: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    isPreviewMode: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 300.dp, max = 500.dp),
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
                Text(
                    text = "Color Recommendations",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                // Undo/Redo buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onUndo,
                        enabled = canUndo
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "Undo",
                            tint = if (canUndo) MaterialTheme.colorScheme.primary 
                                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                    
                    IconButton(
                        onClick = onRedo,
                        enabled = canRedo
                    ) {
                        Icon(
                            imageVector = Icons.Default.Redo,
                            contentDescription = "Redo",
                            tint = if (canRedo) MaterialTheme.colorScheme.primary 
                                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Selected color info
            selectedColor?.let { color ->
                SelectedColorCard(
                    recommendation = color,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Color recommendations grid
            Text(
                text = "Suggestions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recommendations) { recommendation ->
                    ColorRecommendationCard(
                        recommendation = recommendation,
                        isSelected = recommendation == selectedColor,
                        onClick = { onColorSelected(recommendation) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Preview/Compare button
                OutlinedButton(
                    onClick = if (isPreviewMode) onCompare else onPreview,
                    modifier = Modifier.weight(1f),
                    enabled = selectedColor != null
                ) {
                    Icon(
                        imageVector = if (isPreviewMode) Icons.Default.Compare else Icons.Default.Visibility,
                        contentDescription = if (isPreviewMode) "Compare" else "Preview"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isPreviewMode) "Compare" else "Preview")
                }
                
                // Apply button
                Button(
                    onClick = onApply,
                    modifier = Modifier.weight(1f),
                    enabled = selectedColor != null
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Apply"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Apply")
                }
            }
        }
    }
}

/**
 * Selected color card with details
 */
@Composable
private fun SelectedColorCard(
    recommendation: ColorRecommendation,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color preview
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(recommendation.color))
                    .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )
            
            // Color info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = getCategoryName(recommendation.category),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = recommendation.reason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Confidence indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { recommendation.confidence },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                    )
                    
                    Text(
                        text = "${(recommendation.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Individual color recommendation card
 */
@Composable
private fun ColorRecommendationCard(
    recommendation: ColorRecommendation,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(100.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Color circle
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(recommendation.color))
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Category label
        Text(
            text = getCategoryName(recommendation.category),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary 
                    else MaterialTheme.colorScheme.onSurface
        )
        
        // Confidence
        Text(
            text = "${(recommendation.confidence * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

/**
 * Get display name for recommendation category
 */
private fun getCategoryName(category: RecommendationCategory): String {
    return when (category) {
        RecommendationCategory.COMPLEMENTARY -> "Complementary"
        RecommendationCategory.ANALOGOUS -> "Analogous"
        RecommendationCategory.TRIADIC -> "Triadic"
        RecommendationCategory.MONOCHROMATIC -> "Monochromatic"
        RecommendationCategory.SPLIT_COMPLEMENTARY -> "Split Comp."
        RecommendationCategory.CONTRAST -> "High Contrast"
        RecommendationCategory.MODERN -> "Modern"
        RecommendationCategory.MINIMAL -> "Minimal"
        RecommendationCategory.WARM -> "Warm"
        RecommendationCategory.LUXURY -> "Luxury"
        RecommendationCategory.SCANDINAVIAN -> "Scandinavian"
    }
}
