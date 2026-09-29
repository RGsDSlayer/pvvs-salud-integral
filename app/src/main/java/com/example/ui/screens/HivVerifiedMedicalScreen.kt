package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AdherenceGreen
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.MedicalTeal
import com.example.ui.theme.RibbonRed

enum class MedicalTopic(val title: String) {
    QUE_ES_VIH("1. ¿Qué es el VIH?"),
    SINTOMAS("2. Síntomas por Etapas"),
    CONTAGIO("3. Transmisión & Mitos"),
    PREVENCION("4. Prevención Médica")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HivVerifiedMedicalScreen(
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTopic by remember { mutableStateOf(MedicalTopic.QUE_ES_VIH) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Información sobre el VIH",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verificado",
                                tint = InfoBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Acceso público • Información verificada: Manual MSD / OMS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackToLogin,
                        modifier = Modifier.testTag("btn_back_to_login")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al Acceso"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Source citation header badge
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.FactCheck,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Fuentes científicas verificadas: Manual MSD (Merck Manuals) • OMS • CDC",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Tab Navigation across topics
            ScrollableTabRow(
                selectedTabIndex = selectedTopic.ordinal,
                edgePadding = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                MedicalTopic.values().forEach { topic ->
                    Tab(
                        selected = selectedTopic == topic,
                        onClick = { selectedTopic = topic },
                        text = {
                            Text(
                                text = topic.title,
                                fontWeight = if (selectedTopic == topic) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier.testTag("tab_${topic.name.lowercase()}")
                    )
                }
            }

            // Topic Content Body
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                when (selectedTopic) {
                    MedicalTopic.QUE_ES_VIH -> {
                        item { WhatIsHivSection() }
                    }
                    MedicalTopic.SINTOMAS -> {
                        item { SymptomsByStagesSection() }
                    }
                    MedicalTopic.CONTAGIO -> {
                        item { ContagionAndMythsSection() }
                    }
                    MedicalTopic.PREVENCION -> {
                        item { MedicalPreventionSection() }
                    }
                }

                // Return to Login Button Footer
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "¿Deseas gestionar tu tratamiento o citas?",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Los pacientes y médicos registrados pueden acceder con su usuario y contraseña para alarmas de recojo, calendario y adherencia.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onBackToLogin,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_return_login_bottom")
                            ) {
                                Text("Volver al Inicio de Sesión / Acceso", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

// -------------------------------------------------------------
// SECTION 1: ¿QUÉ ES EL VIH? (Manual MSD)
// -------------------------------------------------------------
@Composable
private fun WhatIsHivSection() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ElevatedCard(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Science, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "¿Qué es el VIH?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Definición Médica Oficial según Manual MSD",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "El Virus de la Inmunodeficiencia Humana (VIH) es un retrovirus de la familia Retroviridae (género Lentivirus). Su mecanismo fisiopatológico principal consiste en infectar y destruir gradualmente los linfocitos T colaboradores CD4+, células fundamentales para la coordinación de la respuesta inmunológica del organismo.",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
            }
        }

        // Difference between HIV and AIDS
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = RibbonRed, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Diferencia Crucial: VIH vs. SIDA",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = RibbonRed
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "De acuerdo con los criterios diagnósticos del Manual MSD y la OMS, tener VIH NO es lo mismo que tener SIDA:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "• Infección por VIH:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Es la presencia del virus en el organismo. Con el Tratamiento Antirretroviral (TARV), la replicación del virus se detiene casi por completo, el recuento de defensas (CD4) se mantiene alto y la persona NUNCA desarrolla SIDA.",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "• SIDA (Síndrome de Inmunodeficiencia Adquirida):",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = RibbonRed
                        )
                        Text(
                            text = "Es el estadio clínico más avanzado de la infección por VIH no tratada. Se define médicamente cuando los linfocitos CD4 descienden por debajo de 200 células/µL o cuando se presenta una enfermedad oportunista definitoria.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // Treatment and Expectation of Life
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = AdherenceGreen, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tratamiento Antirretroviral (TARV) y Calidad de Vida",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "El Manual MSD destaca que, gracias a las combinaciones modernas de antirretrovirales de dosis fija (un solo comprimido diario como TLD), las personas que viven con VIH que inician tratamiento temprano y mantienen buena adherencia tienen una esperanza y calidad de vida exactamente idéntica a la de la población general sin la infección.",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

// -------------------------------------------------------------
// SECTION 2: SÍNTOMAS POR ETAPAS CLÍNICAS (Manual MSD)
// -------------------------------------------------------------
@Composable
private fun SymptomsByStagesSection() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Introduction card
        ElevatedCard(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Etapas Clínicas y Sintomatología",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Según la clasificación médica del Manual MSD y los CDC, la infección por VIH cursa a través de 3 fases fisiopatológicas bien diferenciadas si no se inicia tratamiento antirretroviral.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Stage 1: Acute Infection
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AlertAmber.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("1", fontWeight = FontWeight.Bold, color = AlertAmber)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Fase 1: Infección Aguda o Primaria",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Aparece 2 a 4 semanas después del contagio",
                            style = MaterialTheme.typography.labelSmall,
                            color = AlertAmber,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Ocurre en aproximadamente el 50% al 70% de las personas recién infectadas (Síndrome Retroviral Agudo). Es una respuesta inmunológica a la viremia inicial masiva:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                val symptoms1 = listOf(
                    "Fiebre alta (>38.5°C) de inicio repentino",
                    "Erupción cutánea (rash) eritematoso maculopapular en tórax y cara",
                    "Linfadenopatía (ganglios inflamados y sensibles en cuello, axilas e ingles)",
                    "Faringitis / dolor de garganta y disfagia",
                    "Mialgias intensas (dolores musculares) y artralgias",
                    "Cefalea, fatiga pronunciada y sudores nocturnos",
                    "Úlceras mucosas en boca o genitales"
                )

                symptoms1.forEach { item ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AlertAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = item, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    color = AlertAmber.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Nota clínica Manual MSD: Frecuentemente se confunde con mononucleosis infecciosa, influenza o gripe estacional. Cede espontáneamente en 1 a 3 semanas.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(10.dp),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }

        // Stage 2: Latency
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(InfoBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("2", fontWeight = FontWeight.Bold, color = InfoBlue)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Fase 2: Infección Crónica o Latencia Clínica",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Periodo asintomático (de 2 a 10 años sin tratamiento)",
                            style = MaterialTheme.typography.labelSmall,
                            color = InfoBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Durante esta fase, el virus continúa replicándose a niveles basales en los ganglios linfáticos, destruyendo lentamente los linfocitos CD4 (descenso promedio de 50 células/µL por año):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                val symptoms2 = listOf(
                    "Mayoría asintomática: La persona se siente y se ve perfectamente saludable",
                    "Linfadenopatía persistente generalizada (ganglios indoloros en varios sitios)",
                    "Riesgo de transmisión activo si no se toma medicación antirretroviral",
                    "El diagnóstico oportuno en esta etapa evita el deterioro inmunológico"
                )

                symptoms2.forEach { item ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = InfoBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = item, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Stage 3: AIDS
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(RibbonRed.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("3", fontWeight = FontWeight.Bold, color = RibbonRed)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Fase 3: Fase Avanzada / SIDA",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = RibbonRed
                        )
                        Text(
                            text = "Inmunodepresión severa (CD4 < 200 células/µL)",
                            style = MaterialTheme.typography.labelSmall,
                            color = RibbonRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Ocurre cuando el sistema inmunitario ha sufrido un daño crítico. El Manual MSD documenta los siguientes síntomas cardinales:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                val symptoms3 = listOf(
                    "Síndrome de emaciación (pérdida involuntaria de más del 10% del peso corporal)",
                    "Fiebre persistente o recurrente de causa no determinada por más de 1 mes",
                    "Diarrea líquida crónica inexplicable de más de 30 días",
                    "Sudoración nocturna profusa que empapa la ropa de cama",
                    "Candidiasis orofaríngea recurrente (muguet / placas blancas en lengua y boca)",
                    "Infecciones oportunistas graves: Tuberculosis, neumonía por Pneumocystis jirovecii, toxoplasmosis cerebral, herpes zóster multidermatómico",
                    "Tumores malignos definitorios: Sarcoma de Kaposi o linfoma no Hodgkin"
                )

                symptoms3.forEach { item ->
                    Row(
                        modifier = Modifier.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = RibbonRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = item, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SECTION 3: MÉTODOS DE CONTAGIO & MITOS (Manual MSD & OMS)
// -------------------------------------------------------------
@Composable
private fun ContagionAndMythsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Overview Card
        ElevatedCard(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Vías de Transmisión Médicamente Comprobadas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "El Manual MSD y la OMS enfatizan que el VIH es un virus frágil en el medio ambiente que requiere contacto directo de fluidos corporales específicos (sangre, semen, fluidos rectales, fluidos vaginales y leche materna) con mucosas o torrente sanguíneo.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Valid Transmission Routes
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "3 Mecanismos Reales de Transmisión:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = RibbonRed
                )

                Spacer(modifier = Modifier.height(10.dp))

                val realRoutes = listOf(
                    Triple(
                        "1. Vía Sexual (Relaciones Sexuales sin Preservativo)",
                        "Contacto de fluidos sexuales (semen, secreciones vaginales o rectales) con mucosas genitales o rectales durante relaciones anales o vaginales (y en menor medida sexo oral) con personas con carga viral detectable. El sexo anal receptivo tiene la tasa de transmisión por acto más elevada.",
                        Icons.Default.VolunteerActivism
                    ),
                    Triple(
                        "2. Vía Sanguínea / Parenteral",
                        "Uso compartido de jeringas, agujas, cánulas u otros insumos de inyección contaminados con sangre infectada; transfusiones de sangre no tamizada (prácticamente erradicado en bancos de sangre regulados) o punciones accidentales con objetos punzocortantes en personal sanitario.",
                        Icons.Default.LocalHospital
                    ),
                    Triple(
                        "3. Transmisión Vertical (Materno-Infantil)",
                        "De una madre con VIH a su hijo durante el embarazo (a través de la placenta), en el momento del parto (por exposición a sangre y secreciones del canal del parto) o a través de la lactancia materna. Con TARV, esta transmisión se reduce a menos del 1%.",
                        Icons.Default.Favorite
                    )
                )

                realRoutes.forEach { (title, desc, icon) ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                            Icon(imageVector = icon, contentDescription = null, tint = RibbonRed, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Scientific Myths Debunked (What DOES NOT transmit HIV)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Block, contentDescription = null, tint = AdherenceGreen, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mitos Desmentidos: Lo que NO Transmite el VIH",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AdherenceGreen
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "El Manual MSD es categórico: el VIH NO sobrevive fuera del cuerpo ni se transmite por contacto casual:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                val myths = listOf(
                    "Saliva, lágrimas, sudor, orina o heces (salvo presencia evidente de sangre)",
                    "Besos en la boca, abrazos, caricias o apretones de manos",
                    "Compartir vasos, platos, cubiertos, alimentos o agua",
                    "Uso compartido de inodoros, duchas, toallas, gimnasios o piscinas",
                    "Picaduras de mosquitos u otros insectos vectores (el virus no sobrevive en el mosquito)",
                    "El aire, la tos, estornudos o convivir bajo el mismo techo escolar o laboral"
                )

                myths.forEach { myth ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = RibbonRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = myth, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SECTION 4: MÉTODOS DE PREVENCIÓN MÉDICA (Manual MSD & OMS)
// -------------------------------------------------------------
@Composable
private fun MedicalPreventionSection() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ElevatedCard(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Estrategias de Prevención Comprobadas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "La prevención biomédica contemporánea recomendada por el Manual MSD, OMS y CDC combina medidas conductuales y profilaxis farmacológica de alta efectividad.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // 1. Preservativo
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "1. Preservativos (Barrera Física)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "El uso correcto y sistemático del preservativo (masculino o femenino de látex o poliuretano) en cada relación sexual ofrece una eficacia superior al 98% para prevenir el VIH y otras infecciones de transmisión sexual (ITS).",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 20.sp
                )
            }
        }

        // 2. PrEP
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Medication, contentDescription = null, tint = InfoBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "2. PrEP (Profilaxis Pre-Exposición)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Prevención antes de la exposición",
                            style = MaterialTheme.typography.labelSmall,
                            color = InfoBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Consiste en la ingesta diaria (o a demanda) de antirretrovirales (Tenofovir disoproxilo + Emtricitabina) por personas sin VIH con alto riesgo de exposición sexual o parenteral. Reduce el riesgo de adquirir el VIH en más del 99% cuando se toma según lo prescrito.",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 20.sp
                )
            }
        }

        // 3. PEP
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.HealthAndSafety, contentDescription = null, tint = RibbonRed, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "3. PEP (Profilaxis Post-Exposición de Emergencia)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Urgencia médica dentro de las primeras 72 horas",
                            style = MaterialTheme.typography.labelSmall,
                            color = RibbonRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tratamiento antirretroviral de emergencia de 28 días que debe iniciarse preferiblemente antes de las 24 horas y como máximo dentro de las 72 horas posteriores a una posible exposición de riesgo (rotura de condón, agresión sexual o pinchazo con aguja). Disponible en servicios de urgencia hospitalaria.",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 20.sp
                )
            }
        }

        // 4. I = I (Undetectable = Untransmittable)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = AdherenceGreen, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "4. Indetectable = Intransmisible (I = I)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = AdherenceGreen
                        )
                        Text(
                            text = "Principio científico global ratificado por OMS y CDC",
                            style = MaterialTheme.typography.labelSmall,
                            color = AdherenceGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Una persona con VIH que recibe TARV de manera regular y mantiene su carga viral indetectable (<200 copias/mL) durante al menos 6 meses consecutivos NO transmite el virus a sus parejas sexuales por vía genital o anal. La adherencia al tratamiento es una poderosa herramienta de salud pública.",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 20.sp
                )
            }
        }

        // 5. Testing
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.FactCheck, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "5. Pruebas Rápidas de Diagnóstico y Tamizaje",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Pruebas de 4ta generación (antígeno p24 + anticuerpos) con punción digital proporcionan resultados confiables en 15 a 20 minutos. Son gratuitas, voluntarias y confidenciales en centros de salud. Conocer el estatus serológico permite iniciar tratamiento inmediato.",
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
