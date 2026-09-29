package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HealthCenters

@Composable
fun HealthCenterDropdownField(
    selectedCenter: String,
    onCenterSelected: (String) -> Unit,
    label: String = "Centro de Salud / Farmacia TARV",
    includeAllOption: Boolean = false,
    allOptionLabel: String = "Todos los Centros (23 Centros)",
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedCenter,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.LocalHospital,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                IconButton(onClick = { showDialog = true }) {
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Seleccionar Centro de Salud")
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDialog = true }
                .testTag("field_health_center_selector")
        )

        // Invisible overlay to ensure entire field tap triggers dialog
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showDialog = true }
        )
    }

    if (showDialog) {
        HealthCenterSelectionDialog(
            currentSelected = selectedCenter,
            onSelect = {
                onCenterSelected(it)
                showDialog = false
            },
            onDismiss = { showDialog = false },
            includeAllOption = includeAllOption,
            allOptionLabel = allOptionLabel
        )
    }
}

@Composable
fun HealthCenterSelectionDialog(
    currentSelected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    includeAllOption: Boolean = false,
    allOptionLabel: String = "Todos los Centros (23 Centros)"
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredCenters = remember(searchQuery, includeAllOption) {
        val baseList = if (includeAllOption) listOf(allOptionLabel) + HealthCenters.LIST else HealthCenters.LIST
        if (searchQuery.isBlank()) {
            baseList
        } else {
            baseList.filter {
                it.contains(searchQuery.trim(), ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalHospital,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Selecciona tu Centro de Salud",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Selecciona el centro donde realizas el recojo de medicamentos y controles de TARV:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar centro...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${filteredCenters.size} centros disponibles",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Centers List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredCenters) { center ->
                        val isSelected = center.equals(currentSelected.trim(), ignoreCase = true)
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.5.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(center) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalHospital,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = center,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Seleccionado",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}
