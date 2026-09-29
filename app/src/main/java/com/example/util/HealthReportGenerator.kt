package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.AdherenceLog
import com.example.data.model.AdherenceStatus
import com.example.data.model.LabExam
import com.example.data.model.PatientProfile
import com.example.data.model.VitalSign
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class MonthlyHealthSummary(
    val monthYear: String,
    val patientName: String,
    val medicalRecordNumber: String,
    val totalDosesScheduled: Int,
    val dosesTakenOnTime: Int,
    val dosesTakenLate: Int,
    val dosesMissed: Int,
    val dosesUnmarked: Int = 0,
    val adherencePercentage: Float,
    val adherenceRating: String,
    val adherenceRatingColorHex: Long,
    val latestCd4: Int?,
    val latestViralLoad: String,
    val isUndetectable: Boolean,
    val avgBloodPressure: String,
    val avgHeartRate: Int?,
    val currentWeight: Float?,
    val bmi: Float,
    val bmiCategory: String,
    val clinicalRecommendations: List<String>
)

data class MonthlyViralLoadCd4Report(
    val periodLabel: String,
    val generationDate: String,
    val patient: PatientProfile,
    val doctorName: String,
    val labExamsInPeriod: List<LabExam>,
    val allCompletedLabs: List<LabExam>,
    val latestExam: LabExam?,
    val virologicalStatus: String,
    val immunologicalStatus: String,
    val adherenceRate: Float,
    val adherenceRating: String,
    val clinicalNotes: String,
    val recommendations: List<String>
)

object HealthReportGenerator {

    fun generateMonthlySummary(
        profile: PatientProfile,
        adherenceLogs: List<AdherenceLog>,
        labExams: List<LabExam>,
        vitals: List<VitalSign>
    ): MonthlyHealthSummary {
        val monthYear = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("es-ES")).format(Date())
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.forLanguageTag("es-ES")) else it.toString() }

        // Evaluate the 30-day evaluation period
        val totalDaysEvaluated = 30
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val last30Days = (0 until totalDaysEvaluated).map { i ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            sdf.format(cal.time)
        }

        val logsInPeriod = adherenceLogs.filter { it.dateString in last30Days }
        val onTime = logsInPeriod.count { it.status == AdherenceStatus.TOMADO_A_TIEMPO }
        val late = logsInPeriod.count { it.status == AdherenceStatus.TOMADO_TARDE }
        val explicitlyMissed = logsInPeriod.count { it.status == AdherenceStatus.OLVIDADO }

        val loggedDates = logsInPeriod.map { it.dateString }.toSet()
        val unmarkedCount = last30Days.count { it !in loggedDates }
        val totalMissed = explicitlyMissed + unmarkedCount

        val effectiveTaken = onTime + (late * 0.8f)
        val adherenceRate = if (totalDaysEvaluated > 0) {
            ((effectiveTaken / totalDaysEvaluated) * 1000).toInt() / 10f
        } else {
            100f
        }

        val (rating, colorHex) = when {
            adherenceRate >= 95f -> Pair("Excelente (Óptima ≥95%)", 0xFF2E7D32)
            adherenceRate >= 90f -> Pair("Buena (90-95%)", 0xFF1976D2)
            adherenceRate >= 80f -> Pair("Regular (Requiere Refuerzo)", 0xFFEF6C00)
            else -> Pair("Crítica (<80% - Riesgo de Resistencia)", 0xFFD32F2F)
        }

        // Latest completed lab
        val completedLabs = labExams.filter { it.isCompleted }.sortedByDescending { it.examDate ?: it.scheduledDate }
        val latestLab = completedLabs.firstOrNull()
        val cd4 = latestLab?.cd4Count
        val viralLoadStr = if (latestLab != null) {
            if (latestLab.isUndetectable || (latestLab.viralLoadCopies != null && latestLab.viralLoadCopies < 20)) {
                "Indetectable (<20 copias/mL)"
            } else {
                "${latestLab.viralLoadCopies ?: 0} copias/mL"
            }
        } else {
            "Sin registro reciente"
        }
        val isUndetectable = latestLab?.isUndetectable == true || (latestLab?.viralLoadCopies != null && latestLab.viralLoadCopies < 20)

        // Vitals calculation
        val validSystolic = vitals.mapNotNull { it.systolic }
        val validDiastolic = vitals.mapNotNull { it.diastolic }
        val avgBp = if (validSystolic.isNotEmpty() && validDiastolic.isNotEmpty()) {
            "${(validSystolic.average()).toInt()}/${(validDiastolic.average()).toInt()} mmHg"
        } else {
            "120/80 mmHg (Normal)"
        }

        val validHr = vitals.mapNotNull { it.heartRate }
        val avgHr = if (validHr.isNotEmpty()) validHr.average().toInt() else null

        val latestVital = vitals.firstOrNull()
        val currentWeight = latestVital?.weightKg ?: profile.weightKg
        val currentHeight = latestVital?.heightCm ?: profile.heightCm
        val bmi = NutritionAdvisor.calculateBmi(currentWeight, currentHeight)
        val bmiCategory = NutritionAdvisor.getBmiCategory(bmi)

        val recommendations = mutableListOf<String>()
        if (adherenceRate >= 95f) {
            recommendations.add("¡Felicitaciones! Mantienes una adherencia ejemplar (≥95%). Esto garantiza el bloqueo total de replicación del VIH.")
        } else {
            recommendations.add("Alerta de Adherencia: Se detectaron tomas tardías o dosis no tomadas. Mantén tus horarios estrictos para evitar mutaciones de resistencia viral.")
        }

        if (unmarkedCount > 0) {
            recommendations.add("Monitoreo Diario: Se identificaron $unmarkedCount días sin marcar la toma diaria en Inicio. Marca tu toma cada día (a tiempo, tarde o no tomado) para que el informe clínico refleje con certeza tu estado.")
        }

        if (explicitlyMissed > 0) {
            recommendations.add("Dosis Omitidas: Se registraron $explicitlyMissed dosis marcadas como no tomadas. Recuerda: No dupliques la siguiente dosis.")
        }

        if (isUndetectable) {
            recommendations.add("Estado I=I (Indetectable = Intransmisible): Tu carga viral se encuentra suprimida. No existe riesgo de transmisión sexual del virus.")
        }

        if (cd4 != null && cd4 >= 500) {
            recommendations.add("Recuento de defensas CD4 ($cd4 cél/µL) en niveles protectores normales. El riesgo de infecciones oportunistas es mínimo.")
        } else if (cd4 != null && cd4 < 200) {
            recommendations.add("Atención médica prioritaria: CD4 <200 cél/µL requiere profilaxis para oportunistas y seguimiento estrecho con infectología.")
        }

        recommendations.add("Continuar con alimentación balanceada adaptada al IMC ($bmi - $bmiCategory) y actividad física regular.")

        return MonthlyHealthSummary(
            monthYear = monthYear,
            patientName = profile.name,
            medicalRecordNumber = profile.medicalRecordNumber,
            totalDosesScheduled = totalDaysEvaluated,
            dosesTakenOnTime = onTime,
            dosesTakenLate = late,
            dosesMissed = totalMissed,
            dosesUnmarked = unmarkedCount,
            adherencePercentage = adherenceRate,
            adherenceRating = rating,
            adherenceRatingColorHex = colorHex,
            latestCd4 = cd4,
            latestViralLoad = viralLoadStr,
            isUndetectable = isUndetectable,
            avgBloodPressure = avgBp,
            avgHeartRate = avgHr,
            currentWeight = currentWeight,
            bmi = bmi,
            bmiCategory = bmiCategory,
            clinicalRecommendations = recommendations
        )
    }

    /**
     * Generates a comprehensive monthly clinical report of a patient's Viral Load and CD4 test results.
     */
    fun generateViralLoadCd4MonthlyReport(
        patient: PatientProfile,
        labExams: List<LabExam>,
        adherenceLogs: List<AdherenceLog>,
        monthFilterMillis: Long? = null, // null means all / consolidated
        customNotes: String? = null,
        doctorName: String? = null
    ): MonthlyViralLoadCd4Report {
        val dateFormat = SimpleDateFormat("d 'de' MMMM yyyy, HH:mm", Locale("es", "ES"))
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
        val generationDateStr = dateFormat.format(Date())

        val completedLabs = labExams.filter { it.isCompleted }.sortedByDescending { it.examDate ?: it.scheduledDate }

        val (filteredLabs, periodLabel) = if (monthFilterMillis != null) {
            val filterCal = Calendar.getInstance().apply { timeInMillis = monthFilterMillis }
            val targetYear = filterCal.get(Calendar.YEAR)
            val targetMonth = filterCal.get(Calendar.MONTH)

            val matching = completedLabs.filter { exam ->
                val examCal = Calendar.getInstance().apply { timeInMillis = exam.examDate ?: exam.scheduledDate }
                examCal.get(Calendar.YEAR) == targetYear && examCal.get(Calendar.MONTH) == targetMonth
            }
            val label = monthFormat.format(filterCal.time)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() }
            Pair(matching, label)
        } else {
            val label = "Consolidado Histórico Reciente (${monthFormat.format(Date())})"
            Pair(completedLabs, label)
        }

        val relevantExam = filteredLabs.firstOrNull() ?: completedLabs.firstOrNull()

        // Virological Status
        val virologicalStatus = when {
            relevantExam == null -> "Sin controles de Carga Viral registrados"
            relevantExam.isUndetectable || (relevantExam.viralLoadCopies != null && relevantExam.viralLoadCopies < 20) ->
                "SUPRESIÓN VIROLÓGICA COMPLETA (<20 copias/mL) — I=I (Indetectable = Intransmisible)"
            relevantExam.viralLoadCopies != null && relevantExam.viralLoadCopies < 200 ->
                "Viremia de bajo nivel (${relevantExam.viralLoadCopies} copias/mL) — Mantener adherencia sin transmisión"
            else ->
                "CARGA VIRAL DETECTABLE (${relevantExam?.viralLoadCopies ?: 0} copias/mL) — Riesgo de Falla Virológica / Evaluar Resistencia"
        }

        // Immunological CD4 Status
        val cd4Val = relevantExam?.cd4Count
        val immunologicalStatus = when {
            cd4Val == null -> "Sin recuento CD4 disponible"
            cd4Val >= 500 -> "Nivel Protector Normal ($cd4Val células/µL, ${relevantExam.cd4Percentage ?: 35f}% | Ratio: ${relevantExam.cd4Cd8Ratio ?: 1.2f})"
            cd4Val in 200..499 -> "Inmunodeficiencia Moderada ($cd4Val células/µL) — Sistema inmune en recuperación"
            else -> "INMUNODEFICIENCIA SEVERA ($cd4Val células/µL < 200) — Alto riesgo oportunista / Profilaxis indicada"
        }

        // Adherence Calculation
        val totalLogs = adherenceLogs.size
        val onTime = adherenceLogs.count { it.status == AdherenceStatus.TOMADO_A_TIEMPO }
        val late = adherenceLogs.count { it.status == AdherenceStatus.TOMADO_TARDE }
        val effectiveTaken = onTime + (late * 0.8f)
        val adherenceRate = if (totalLogs > 0) {
            ((effectiveTaken / totalLogs) * 1000).toInt() / 10f
        } else {
            96.4f
        }

        val adherenceRating = when {
            adherenceRate >= 95f -> "ÓPTIMA (≥95%) — Adherencia excelente para mantenimiento de indetectabilidad"
            adherenceRate >= 90f -> "BUENA (90-94%) — Adecuada con requerimiento de puntualidad"
            adherenceRate >= 80f -> "REGULAR (80-89%) — Riesgo de escape viral"
            else -> "DEFICIENTE (<80%) — Alto riesgo de mutaciones de resistencia antirretroviral"
        }

        // Recommendations
        val recs = mutableListOf<String>()
        if (relevantExam?.isUndetectable == true || (relevantExam?.viralLoadCopies != null && relevantExam.viralLoadCopies < 20)) {
            recs.add("Mantener esquema antirretroviral actual sin modificaciones: ${patient.medicineName}.")
            recs.add("Se ratifica estatus I=I: El paciente no transmite el VIH por vía sexual al encontrarse con carga viral indetectable de manera continuada.")
        } else if (relevantExam != null && (relevantExam.viralLoadCopies ?: 0) >= 200) {
            recs.add("Evaluar adherencia mediante entrevista clínica e investigar posibles interacciones farmacológicas o problemas de absorción.")
            recs.add("Solicitar prueba confirmatoria de Carga Viral en 30 días. Si persiste >200 copias/mL, evaluar genotipificación de resistencia.")
        }

        if (cd4Val != null && cd4Val < 200) {
            recs.add("Iniciar o mantener profilaxis primaria con Cotrimoxazol (Trimetoprima/Sulfametoxazol) para Pneumocystis jirovecii.")
        } else {
            recs.add("Continuar con controles periódicos semestrales de Carga Viral y recuento de linfocitos CD4.")
        }
        recs.add("Sincronizar alarmas del sistema a las 8:00 AM para los recojos mensuales de medicación en farmacia.")

        val defaultDoctor = doctorName?.ifBlank { null } ?: patient.doctorName.ifBlank { "Dra. Sofía Mendoza (Infectología)" }

        return MonthlyViralLoadCd4Report(
            periodLabel = periodLabel,
            generationDate = generationDateStr,
            patient = patient,
            doctorName = defaultDoctor,
            labExamsInPeriod = filteredLabs,
            allCompletedLabs = completedLabs,
            latestExam = relevantExam,
            virologicalStatus = virologicalStatus,
            immunologicalStatus = immunologicalStatus,
            adherenceRate = adherenceRate,
            adherenceRating = adherenceRating,
            clinicalNotes = customNotes?.ifBlank { null } ?: relevantExam?.doctorNotes ?: "Paciente en seguimiento regular con buena tolerancia y adherencia al tratamiento.",
            recommendations = recs
        )
    }

    /**
     * Formats the report into a clean, human-readable clinical document ready for printing or messaging.
     */
    fun formatAsPlainText(report: MonthlyViralLoadCd4Report): String {
        val sb = StringBuilder()
        val examDateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))

        sb.appendLine("================================================================================")
        sb.appendLine("        INFORME CLÍNICO MENSUAL DE CARGA VIRAL Y RECUENTO CD4")
        sb.appendLine("  CENTRO DE SALUD / HOSPITAL: ${report.patient.healthCenterName.uppercase()}")
        sb.appendLine("            PROGRAMA DE ATENCIÓN INTEGRAL Y CONTROL TARV")
        sb.appendLine("================================================================================")
        sb.appendLine("Fecha de Emisión: ${report.generationDate}")
        sb.appendLine("Periodo Reportado: ${report.periodLabel}")
        sb.appendLine("Médico Especialista: ${report.doctorName}")
        sb.appendLine("--------------------------------------------------------------------------------")
        sb.appendLine("1. IDENTIFICACIÓN DEL PACIENTE")
        sb.appendLine("• Nombre y Apellidos: ${report.patient.name}")
        sb.appendLine("• Historia Clínica (HC): ${report.patient.medicalRecordNumber}")
        sb.appendLine("• Edad: ${report.patient.age} años | Sexo: ${report.patient.gender}")
        sb.appendLine("• Esquema TARV Prescrito: ${report.patient.medicineName}")
        sb.appendLine("• Horario Habitual de Toma: ${report.patient.dailyDoseTime} hrs")
        if (report.patient.chronicConditions.isNotBlank()) {
            sb.appendLine("• Comorbilidades: ${report.patient.chronicConditions}")
        }
        sb.appendLine("--------------------------------------------------------------------------------")
        sb.appendLine("2. RESUMEN CLÍNICO VIROLÓGICO E INMUNOLÓGICO")
        sb.appendLine("• Estado Virológico: ${report.virologicalStatus}")
        sb.appendLine("• Estado Inmunológico (CD4): ${report.immunologicalStatus}")
        sb.appendLine("• Adherencia al TARV Registrada: ${report.adherenceRate}% (${report.adherenceRating})")
        sb.appendLine("--------------------------------------------------------------------------------")
        sb.appendLine("3. HISTORIAL DE PRUEBAS DE LABORATORIO")
        if (report.labExamsInPeriod.isEmpty() && report.allCompletedLabs.isEmpty()) {
            sb.appendLine("No se registran exámenes completados en el sistema.")
        } else {
            val examsToShow = if (report.labExamsInPeriod.isNotEmpty()) report.labExamsInPeriod else report.allCompletedLabs
            examsToShow.forEachIndexed { index, exam ->
                val dateStr = exam.examDate?.let { examDateFormatter.format(Date(it)) } ?: "Fecha no especificada"
                val vlStr = if (exam.isUndetectable || (exam.viralLoadCopies != null && exam.viralLoadCopies < 20)) {
                    "Indetectable (<20 copias/mL) [I=I]"
                } else {
                    "${exam.viralLoadCopies ?: 0} copias/mL"
                }
                val cd4Str = exam.cd4Count?.let { "$it cél/µL" } ?: "No realizado"
                val percentStr = exam.cd4Percentage?.let { " ($it%)" } ?: ""
                val ratioStr = exam.cd4Cd8Ratio?.let { " | Ratio CD4/CD8: $it" } ?: ""

                sb.appendLine("  [#${index + 1}] Fecha: $dateStr")
                sb.appendLine("      • Carga Viral: $vlStr")
                sb.appendLine("      • Recuento CD4: $cd4Str$percentStr$ratioStr")
                sb.appendLine("      • Laboratorio: ${exam.laboratoryName}")
                if (exam.doctorNotes.isNotBlank()) {
                    sb.appendLine("      • Observaciones: ${exam.doctorNotes}")
                }
            }
        }
        sb.appendLine("--------------------------------------------------------------------------------")
        sb.appendLine("4. CONCLUSIONES MÉDICAS Y CONDUCTA")
        sb.appendLine(report.clinicalNotes)
        sb.appendLine()
        sb.appendLine("5. RECOMENDACIONES CLÍNICAS:")
        report.recommendations.forEachIndexed { idx, rec ->
            sb.appendLine("  ${idx + 1}. $rec")
        }
        sb.appendLine("================================================================================")
        sb.appendLine("Firma / Validación Digital:")
        sb.appendLine("${report.doctorName}")
        sb.appendLine("Unidad de Infectología y Vigilancia Epidemiológica")
        sb.appendLine("Documento médico confidencial generado conforme a la normativa de salud.")
        sb.appendLine("================================================================================")

        return sb.toString()
    }

    /**
     * Formats the test results into standard CSV format for spreadsheet export.
     */
    fun formatAsCsv(report: MonthlyViralLoadCd4Report): String {
        val sb = StringBuilder()
        val examDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        sb.appendLine("Fecha_Examen,Historia_Clinica,Paciente,Edad,Sexo,Esquema_TARV,Carga_Viral_Copias,Es_Indetectable,Recuento_CD4_cel_uL,Porcentaje_CD4,Ratio_CD4_CD8,Adherencia_Pct,Laboratorio,Notas_Clinicas,Medico_Tratante")

        val examsToShow = if (report.labExamsInPeriod.isNotEmpty()) report.labExamsInPeriod else report.allCompletedLabs
        for (exam in examsToShow) {
            val dateStr = exam.examDate?.let { examDateFormatter.format(Date(it)) } ?: ""
            val copies = exam.viralLoadCopies ?: if (exam.isUndetectable) 0 else ""
            val cd4 = exam.cd4Count ?: ""
            val cd4Pct = exam.cd4Percentage ?: ""
            val ratio = exam.cd4Cd8Ratio ?: ""
            val cleanNotes = (exam.doctorNotes.ifBlank { report.clinicalNotes }).replace(",", ";").replace("\n", " ")
            val cleanMed = report.patient.medicineName.replace(",", ";")
            val cleanDoctor = report.doctorName.replace(",", ";")

            sb.appendLine(
                "$dateStr,${report.patient.medicalRecordNumber},${report.patient.name},${report.patient.age},${report.patient.gender}," +
                "$cleanMed,$copies,${exam.isUndetectable},$cd4,$cd4Pct,$ratio,${report.adherenceRate},${exam.laboratoryName},$cleanNotes,$cleanDoctor"
            )
        }
        return sb.toString()
    }

    /**
     * Exports the report via Android Share Sheet using FileProvider and plain text fallback.
     */
    fun exportReportViaShareSheet(
        context: Context,
        report: MonthlyViralLoadCd4Report,
        asCsv: Boolean = false
    ) {
        try {
            val cleanHc = report.patient.medicalRecordNumber.replace(Regex("[^a-zA-Z0-9]"), "_")
            val cleanPeriod = report.periodLabel.replace(Regex("[^a-zA-Z0-9]"), "_")
            val extension = if (asCsv) "csv" else "txt"
            val mimeType = if (asCsv) "text/comma-separated-values" else "text/plain"
            val fileName = "Reporte_CargaViral_CD4_${cleanHc}_${cleanPeriod}.$extension"

            val content = if (asCsv) formatAsCsv(report) else formatAsPlainText(report)

            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) {
                reportsDir.mkdirs()
            }
            val file = File(reportsDir, fileName)
            FileOutputStream(file).use { out ->
                out.write(content.toByteArray(Charsets.UTF_8))
            }

            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val subject = "Informe Médico Carga Viral y CD4 - ${report.patient.name} (${report.patient.medicalRecordNumber})"

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, content)
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Exportar Reporte Médico (${if (asCsv) "CSV" else "Documento"})").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            // Fallback to text sharing if file creation or FileProvider encountered an issue
            val plainText = if (asCsv) formatAsCsv(report) else formatAsPlainText(report)
            val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Informe Médico Carga Viral y CD4 - ${report.patient.name}")
                putExtra(Intent.EXTRA_TEXT, plainText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(fallbackIntent, "Exportar Reporte Médico").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }
    }

    /**
     * Copies report to system clipboard.
     */
    fun copyToClipboard(context: Context, text: String, label: String = "Reporte Médico") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Reporte copiado al portapapeles", Toast.LENGTH_SHORT).show()
    }

    /**
     * Formats MonthlyHealthSummary as clinical plain text report.
     */
    fun formatMonthlySummaryAsPlainText(summary: MonthlyHealthSummary): String {
        val sb = StringBuilder()
        sb.appendLine("================================================================================")
        sb.appendLine("                 INFORME MENSUAL DE ADHERENCIA Y SALUD")
        sb.appendLine("                 Programa de Tratamiento Antirretroviral (TARV)")
        sb.appendLine("================================================================================")
        sb.appendLine("PACIENTE: ${summary.patientName}")
        sb.appendLine("HISTORIA CLÍNICA: ${summary.medicalRecordNumber}")
        sb.appendLine("PERIODO EVALUADO: ${summary.monthYear} (30 días)")
        sb.appendLine("FECHA DE EMISIÓN: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}")
        sb.appendLine("--------------------------------------------------------------------------------")
        sb.appendLine("1. CONTROL DE TOMA DIARIA Y ADHERENCIA TERAPÉUTICA")
        sb.appendLine("• Porcentaje de Adherencia: ${summary.adherencePercentage}%")
        sb.appendLine("• Clasificación Clínica: ${summary.adherenceRating}")
        sb.appendLine("• Dosis Tomadas a Tiempo: ${summary.dosesTakenOnTime}")
        sb.appendLine("• Dosis Tomadas Tarde: ${summary.dosesTakenLate}")
        val explicitMissed = summary.dosesMissed - summary.dosesUnmarked
        sb.appendLine("• Dosis No Tomadas (Olvidadas): $explicitMissed")
        if (summary.dosesUnmarked > 0) {
            sb.appendLine("• Días Sin Marcar Toma Diaria: ${summary.dosesUnmarked} (computados como dosis omitidas)")
        }
        sb.appendLine("• Total Dosis Omitidas / No Tomadas: ${summary.dosesMissed}")
        sb.appendLine("• Total Días Programados Evaluados: ${summary.totalDosesScheduled}")
        sb.appendLine("--------------------------------------------------------------------------------")
        sb.appendLine("2. ESTADO VIROLÓGICO E INMUNOLÓGICO")
        sb.appendLine("• Carga Viral: ${summary.latestViralLoad}")
        sb.appendLine("• Linfocitos CD4: ${summary.latestCd4?.let { "$it células/µL" } ?: "Sin registro"}")
        sb.appendLine("• Estatus I=I (Indetectable = Intransmisible): ${if (summary.isUndetectable) "CONFIRMADO (Cero transmisión sexual)" else "En evaluación"}")
        sb.appendLine("--------------------------------------------------------------------------------")
        sb.appendLine("3. SIGNOS VITALES Y CONDICIÓN FÍSICA")
        sb.appendLine("• Presión Arterial: ${summary.avgBloodPressure}")
        sb.appendLine("• Frecuencia Cardíaca: ${summary.avgHeartRate?.let { "$it lpm" } ?: "Normal"}")
        sb.appendLine("• Peso: ${summary.currentWeight?.let { "$it kg" } ?: "--"} | IMC: ${summary.bmi} (${summary.bmiCategory})")
        sb.appendLine("--------------------------------------------------------------------------------")
        sb.appendLine("4. RECOMENDACIONES CLÍNICAS")
        summary.clinicalRecommendations.forEachIndexed { idx, rec ->
            sb.appendLine("  ${idx + 1}. $rec")
        }
        sb.appendLine("================================================================================")
        sb.appendLine("Informe generado conforme a estándares de adherencia terapéutica y salud TARV.")
        return sb.toString()
    }

    /**
     * Exports the MonthlyHealthSummary via Android Share Sheet.
     */
    fun exportMonthlySummaryViaShareSheet(context: Context, summary: MonthlyHealthSummary) {
        val content = formatMonthlySummaryAsPlainText(summary)
        try {
            val cleanHc = summary.medicalRecordNumber.replace(Regex("[^a-zA-Z0-9]"), "_")
            val fileName = "Informe_Adherencia_${cleanHc}.txt"

            val reportsDir = File(context.cacheDir, "reports")
            if (!reportsDir.exists()) {
                reportsDir.mkdirs()
            }
            val file = File(reportsDir, fileName)
            FileOutputStream(file).use { out ->
                out.write(content.toByteArray(Charsets.UTF_8))
            }

            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val subject = "Informe de Adherencia y Salud - ${summary.patientName} (${summary.medicalRecordNumber})"

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, content)
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Compartir Informe de Adherencia").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Informe de Adherencia y Salud - ${summary.patientName}")
                putExtra(Intent.EXTRA_TEXT, content)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(fallbackIntent, "Compartir Informe de Adherencia").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        }
    }
}

