package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NoFood
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PatientProfile
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AdherenceGreen
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.MedicalTeal
import com.example.ui.theme.RibbonRed
import com.example.util.MealSuggestion
import com.example.util.NutritionAdvisor
import com.example.util.PathologyCategory
import com.example.util.PathologyNutritionPlan

@Composable
fun NutritionScreen(
    patientProfile: PatientProfile?,
    onUpdateWeightAndHeight: ((Float, Float) -> Unit)? = null,
    onUpdateChronicConditions: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val patientConditionsList = remember(patientProfile?.chronicConditions) {
        patientProfile?.chronicConditions?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
    }

    var selectedConditions by remember(patientConditionsList) {
        mutableStateOf(patientConditionsList.toSet())
    }

    // Determine default selected pathology category based on patient's diagnosed conditions
    var selectedPathology by remember(selectedConditions) {
        val initialCategory = when {
            selectedConditions.any { it.contains("Renal", ignoreCase = true) || it.contains("Hepática", ignoreCase = true) } -> PathologyCategory.RENAL_HEPATICA
            selectedConditions.any { it.contains("Diabetes", ignoreCase = true) } -> PathologyCategory.DIABETES_T2
            selectedConditions.any { it.contains("Hipertensión", ignoreCase = true) } -> PathologyCategory.HIPERTENSION
            selectedConditions.any { it.contains("Dislipidemia", ignoreCase = true) || it.contains("Colesterol", ignoreCase = true) } -> PathologyCategory.DISLIPIDEMIA
            else -> PathologyCategory.INMUNO_GENERAL
        }
        mutableStateOf(initialCategory)
    }

    // Get specific plan for the actively selected pathology tab
    val activePathologyPlan = remember(selectedPathology) {
        NutritionAdvisor.getPlanForPathology(selectedPathology)
    }

    val chronicOptions = listOf(
        "Hipertensión arterial" to PathologyCategory.HIPERTENSION,
        "Diabetes tipo 2" to PathologyCategory.DIABETES_T2,
        "Dislipidemia (Colesterol / Triglicéridos)" to PathologyCategory.DISLIPIDEMIA,
        "Afección Renal / Hepática" to PathologyCategory.RENAL_HEPATICA
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Section header for Medical Nutrition Plans
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Planes de Alimentación Médicos por Patología",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Planes de Desayuno, Almuerzo y Cena respaldados por guías clínicas científicas (DASH, ADA, NCEP ATP III, KDIGO y EASL) adaptados al paciente PVVS.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Pathology selector chips
                    Text(
                        text = "Selecciona la patología para ver su menú clínico:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(PathologyCategory.values()) { category ->
                            val isSelected = selectedPathology == category
                            val isPatientCondition = when (category) {
                                PathologyCategory.HIPERTENSION -> selectedConditions.any { it.contains("Hipertensión", ignoreCase = true) }
                                PathologyCategory.DIABETES_T2 -> selectedConditions.any { it.contains("Diabetes", ignoreCase = true) }
                                PathologyCategory.DISLIPIDEMIA -> selectedConditions.any { it.contains("Dislipidemia", ignoreCase = true) || it.contains("Colesterol", ignoreCase = true) }
                                PathologyCategory.RENAL_HEPATICA -> selectedConditions.any { it.contains("Renal", ignoreCase = true) || it.contains("Hepática", ignoreCase = true) }
                                PathologyCategory.INMUNO_GENERAL -> selectedConditions.isEmpty()
                            }

                            val chipColor = Color(category.colorHex)

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) chipColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) chipColor else Color.Transparent
                                ),
                                modifier = Modifier
                                    .clickable { selectedPathology = category }
                                    .testTag("chip_pathology_${category.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isPatientCondition) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(chipColor)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        text = category.displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) chipColor else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Toggle patient diagnosed conditions
                    Text(
                        text = "Patologías activas diagnosticadas en el paciente:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            chronicOptions.forEach { (conditionName, category) ->
                                val isChecked = selectedConditions.contains(conditionName)
                                FilterChip(
                                    selected = isChecked,
                                    onClick = {
                                        val updated = if (isChecked) {
                                            selectedConditions - conditionName
                                        } else {
                                            selectedConditions + conditionName
                                        }
                                        selectedConditions = updated
                                        onUpdateChronicConditions?.invoke(updated.joinToString(","))
                                        // Auto select this pathology when clicked
                                        selectedPathology = category
                                    },
                                    label = {
                                        Text(conditionName, fontSize = 11.sp, fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Normal)
                                    },
                                    leadingIcon = if (isChecked) {
                                        { Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(category.colorHex).copy(alpha = 0.15f),
                                        selectedLabelColor = Color(category.colorHex)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Pathology Medical Evidence Card
        item {
            MedicalEvidenceCard(pathologyPlan = activePathologyPlan)
        }

        // Section header for 3 Daily Meals
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Menú Específico: ${activePathologyPlan.category.displayName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = Color(activePathologyPlan.category.colorHex).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = activePathologyPlan.category.guidelineBadge,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(activePathologyPlan.category.colorHex),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // 1. Desayuno
        item {
            MealCard(
                type = "Desayuno",
                meal = activePathologyPlan.breakfast,
                icon = Icons.Default.WbSunny,
                badgeColor = AlertAmber,
                pathologyColor = Color(activePathologyPlan.category.colorHex)
            )
        }

        // 2. Almuerzo
        item {
            MealCard(
                type = "Almuerzo",
                meal = activePathologyPlan.lunch,
                icon = Icons.Default.Restaurant,
                badgeColor = AdherenceGreen,
                pathologyColor = Color(activePathologyPlan.category.colorHex)
            )
        }

        // 3. Cena
        item {
            MealCard(
                type = "Cena",
                meal = activePathologyPlan.dinner,
                icon = Icons.Default.LocalDrink,
                badgeColor = MedicalTeal,
                pathologyColor = Color(activePathologyPlan.category.colorHex)
            )
        }

        // 4. Merienda / Colación
        item {
            MealCard(
                type = "Colación / Merienda Opcional",
                meal = activePathologyPlan.snack,
                icon = Icons.Default.Spa,
                badgeColor = InfoBlue,
                pathologyColor = Color(activePathologyPlan.category.colorHex)
            )
        }

        // Foods to Avoid / Prohibited for this Pathology
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = RibbonRed.copy(alpha = 0.08f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, RibbonRed.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.NoFood, contentDescription = null, tint = RibbonRed, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Alimentos a Evitar Estrictamente (${activePathologyPlan.category.displayName})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = RibbonRed
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    activePathologyPlan.foodsToAvoid.forEach { foodTip ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = "✖ ", fontWeight = FontWeight.Bold, color = RibbonRed, fontSize = 12.sp)
                            Text(
                                text = foodTip,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Key Nutrients for this Pathology & HIV Immune System
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Egg, contentDescription = null, tint = MedicalTeal, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Nutrientes Clave y Mecanismo Celular Protector",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    activePathologyPlan.keyNutrients.forEach { micro ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = "✔ ", fontWeight = FontWeight.Bold, color = MedicalTeal, fontSize = 12.sp)
                            Text(text = micro, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        // TARV & Food Safety
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Inocuidad Alimentaria e Interacciones con TARV",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    activePathologyPlan.hydrationAndTarvTips.forEach { tip ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(text = "• ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@Composable
private fun MedicalEvidenceCard(
    pathologyPlan: PathologyNutritionPlan
) {
    val pathologyColor = Color(pathologyPlan.category.colorHex)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = pathologyColor.copy(alpha = 0.08f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, pathologyColor.copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = pathologyColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Respaldo Médico Verificado",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = pathologyColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = pathologyPlan.scientificEvidenceNotes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Objetivo Clínico:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = pathologyColor
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = pathologyPlan.clinicalRationaleSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MealCard(
    type: String,
    meal: MealSuggestion,
    icon: ImageVector,
    badgeColor: Color,
    pathologyColor: Color
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(badgeColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = type,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusBadge(
                        text = meal.caloriesApprox,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    StatusBadge(
                        text = meal.proteinApprox,
                        color = AdherenceGreen,
                        backgroundColor = AdherenceGreen.copy(alpha = 0.12f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Meal title
            Text(
                text = meal.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Meal description
            Text(
                text = meal.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Macronutrient extra chips if available
            if (meal.carbsApprox.isNotBlank() || meal.fatsApprox.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (meal.carbsApprox.isNotBlank()) {
                        StatusBadge(
                            text = meal.carbsApprox,
                            color = InfoBlue,
                            backgroundColor = InfoBlue.copy(alpha = 0.12f)
                        )
                    }
                    if (meal.fatsApprox.isNotBlank()) {
                        StatusBadge(
                            text = meal.fatsApprox,
                            color = AlertAmber,
                            backgroundColor = AlertAmber.copy(alpha = 0.12f)
                        )
                    }
                }
            }

            // Clinical Rationale box
            if (meal.clinicalRationale.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = pathologyColor.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = pathologyColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Fundamento Médico:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = pathologyColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = meal.clinicalRationale,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Ingredients list if available
            if (meal.ingredients.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ingredientes sugeridos:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                meal.ingredients.forEach { ing ->
                    Row(
                        modifier = Modifier.padding(vertical = 1.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(badgeColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = ing,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

