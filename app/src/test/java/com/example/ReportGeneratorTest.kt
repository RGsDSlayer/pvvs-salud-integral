package com.example

import com.example.data.model.AdherenceLog
import com.example.data.model.AdherenceStatus
import com.example.data.model.LabExam
import com.example.data.model.PatientProfile
import com.example.util.HealthReportGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportGeneratorTest {

    private val samplePatient = PatientProfile(
        id = 1,
        name = "Carlos Rodríguez",
        nickname = "Carlos",
        medicalRecordNumber = "HC-98421",
        age = 34,
        gender = "Masculino",
        heightCm = 174f,
        weightKg = 68.5f,
        chronicConditions = "Ninguna relevante",
        medicineName = "Dolutegravir / Tenofovir / Lamivudina (TLD)",
        dailyDoseTime = "21:00",
        healthCenterName = "Hospital Nacional Dos de Mayo",
        doctorName = "Dra. Sofía Mendoza"
    )

    private val sampleLabs = listOf(
        LabExam(
            id = 1,
            patientId = 1,
            scheduledDate = System.currentTimeMillis() - 86400000L * 30,
            examDate = System.currentTimeMillis() - 86400000L * 30,
            isCompleted = true,
            viralLoadCopies = 0,
            isUndetectable = true,
            cd4Count = 780,
            cd4Percentage = 36.5f,
            cd4Cd8Ratio = 1.25f,
            laboratoryName = "Laboratorio Central de Referencia",
            doctorNotes = "Excelente respuesta inmunovirológica sostenida."
        ),
        LabExam(
            id = 2,
            patientId = 1,
            scheduledDate = System.currentTimeMillis() - 86400000L * 210,
            examDate = System.currentTimeMillis() - 86400000L * 210,
            isCompleted = true,
            viralLoadCopies = 0,
            isUndetectable = true,
            cd4Count = 650,
            cd4Percentage = 32.0f,
            cd4Cd8Ratio = 1.10f,
            laboratoryName = "Laboratorio Central de Referencia",
            doctorNotes = "Control previo normal."
        )
    )

    private val sampleAdherence = listOf(
        AdherenceLog(id = 1, patientId = 1, dateString = "2026-09-01", scheduledTime = "21:00", takenTime = "21:00", status = AdherenceStatus.TOMADO_A_TIEMPO),
        AdherenceLog(id = 2, patientId = 1, dateString = "2026-09-02", scheduledTime = "21:00", takenTime = "21:05", status = AdherenceStatus.TOMADO_A_TIEMPO),
        AdherenceLog(id = 3, patientId = 1, dateString = "2026-09-03", scheduledTime = "21:00", takenTime = "22:15", status = AdherenceStatus.TOMADO_TARDE)
    )

    @Test
    fun testGenerateMonthlyReport_UndetectableAndNormalCd4() {
        val report = HealthReportGenerator.generateViralLoadCd4MonthlyReport(
            patient = samplePatient,
            labExams = sampleLabs,
            adherenceLogs = sampleAdherence
        )

        assertNotNull(report)
        assertEquals("Carlos Rodríguez", report.patient.name)
        assertEquals("HC-98421", report.patient.medicalRecordNumber)
        assertTrue("Virological status should be undetectable / I=I", report.virologicalStatus.contains("SUPRESIÓN VIROLÓGICA COMPLETA"))
        assertTrue("CD4 should indicate normal protector level", report.immunologicalStatus.contains("Nivel Protector Normal"))
        assertEquals(780, report.latestExam?.cd4Count)
        assertTrue("Should have recommendations", report.recommendations.isNotEmpty())
    }

    @Test
    fun testFormatAsPlainText_ContainsAllKeyClinicalSections() {
        val report = HealthReportGenerator.generateViralLoadCd4MonthlyReport(
            patient = samplePatient,
            labExams = sampleLabs,
            adherenceLogs = sampleAdherence
        )

        val plainText = HealthReportGenerator.formatAsPlainText(report)

        assertTrue(plainText.contains("INFORME CLÍNICO MENSUAL DE CARGA VIRAL Y RECUENTO CD4"))
        assertTrue(plainText.contains("HOSPITAL NACIONAL DOS DE MAYO"))
        assertTrue(plainText.contains("Carlos Rodríguez"))
        assertTrue(plainText.contains("HC-98421"))
        assertTrue(plainText.contains("Dolutegravir / Tenofovir / Lamivudina (TLD)"))
        assertTrue(plainText.contains("Indetectable (<20 copias/mL) [I=I]"))
        assertTrue(plainText.contains("780 cél/µL"))
        assertTrue(plainText.contains("Laboratorio Central de Referencia"))
        assertTrue(plainText.contains("Dra. Sofía Mendoza"))
    }

    @Test
    fun testFormatAsCsv_GeneratesValidCsvRows() {
        val report = HealthReportGenerator.generateViralLoadCd4MonthlyReport(
            patient = samplePatient,
            labExams = sampleLabs,
            adherenceLogs = sampleAdherence
        )

        val csv = HealthReportGenerator.formatAsCsv(report)

        assertTrue("CSV header must include Fecha_Examen", csv.startsWith("Fecha_Examen,Historia_Clinica,Paciente"))
        assertTrue("CSV should contain HC-98421", csv.contains("HC-98421"))
        assertTrue("CSV should contain Carlos Rodríguez", csv.contains("Carlos Rodríguez"))
        assertTrue("CSV should contain 780 for CD4", csv.contains("780"))
    }

    @Test
    fun testGenerateMonthlyReport_MonthFilter() {
        val targetMillis = sampleLabs.first().examDate!!

        val report = HealthReportGenerator.generateViralLoadCd4MonthlyReport(
            patient = samplePatient,
            labExams = sampleLabs,
            adherenceLogs = sampleAdherence,
            monthFilterMillis = targetMillis
        )

        assertEquals(1, report.labExamsInPeriod.size)
        assertEquals(780, report.labExamsInPeriod.first().cd4Count)
    }
}
