package com.example

import com.example.util.NutritionAdvisor
import com.example.util.PathologyCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NutritionAdvisorTest {

    @Test
    fun testBmiCalculationAndCategories() {
        // Normal weight: 70kg, 175cm -> BMI 22.8
        val bmiNormal = NutritionAdvisor.calculateBmi(70f, 175f)
        assertEquals(22.8f, bmiNormal, 0.2f)
        assertEquals("Peso Saludable / Normal", NutritionAdvisor.getBmiCategory(bmiNormal))

        // Underweight: 48kg, 170cm -> BMI 16.6
        val bmiUnder = NutritionAdvisor.calculateBmi(48f, 170f)
        assertEquals(16.6f, bmiUnder, 0.2f)
        assertEquals("Bajo Peso", NutritionAdvisor.getBmiCategory(bmiUnder))

        // Overweight: 85kg, 170cm -> BMI 29.4
        val bmiOver = NutritionAdvisor.calculateBmi(85f, 170f)
        assertEquals(29.4f, bmiOver, 0.2f)
        assertEquals("Sobrepeso", NutritionAdvisor.getBmiCategory(bmiOver))

        // Obese: 100kg, 165cm -> BMI 36.7
        val bmiObese = NutritionAdvisor.calculateBmi(100f, 165f)
        assertEquals("Obesidad Grado II o Mayor", NutritionAdvisor.getBmiCategory(bmiObese))
    }

    @Test
    fun testHypertensionMealPlanHasEvidenceAndThreeMeals() {
        val plan = NutritionAdvisor.getPlanForPathology(PathologyCategory.HIPERTENSION)
        assertEquals(PathologyCategory.HIPERTENSION, plan.category)

        // Medical evidence backed by DASH / AHA
        assertTrue(plan.scientificEvidenceNotes.contains("DASH"))
        assertTrue(plan.scientificEvidenceNotes.contains("American Heart Association") || plan.scientificEvidenceNotes.contains("AHA"))

        // Desayuno, Almuerzo, Cena
        assertNotNull(plan.breakfast)
        assertTrue(plan.breakfast.title.isNotBlank())
        assertTrue(plan.breakfast.description.isNotBlank())
        assertTrue(plan.breakfast.clinicalRationale.isNotBlank())

        assertNotNull(plan.lunch)
        assertTrue(plan.lunch.title.isNotBlank())
        assertTrue(plan.lunch.description.isNotBlank())
        assertTrue(plan.lunch.clinicalRationale.isNotBlank())

        assertNotNull(plan.dinner)
        assertTrue(plan.dinner.title.isNotBlank())
        assertTrue(plan.dinner.description.isNotBlank())
        assertTrue(plan.dinner.clinicalRationale.isNotBlank())

        // Foods to avoid
        assertTrue(plan.foodsToAvoid.isNotEmpty())
        assertTrue(plan.foodsToAvoid.any { it.contains("sodio", ignoreCase = true) || it.contains("Embutidos", ignoreCase = true) })
    }

    @Test
    fun testDiabetesT2MealPlanHasEvidenceAndThreeMeals() {
        val plan = NutritionAdvisor.getPlanForPathology(PathologyCategory.DIABETES_T2)
        assertEquals(PathologyCategory.DIABETES_T2, plan.category)

        // Medical evidence backed by ADA
        assertTrue(plan.scientificEvidenceNotes.contains("ADA") || plan.scientificEvidenceNotes.contains("American Diabetes Association"))

        // Desayuno, Almuerzo, Cena
        assertTrue(plan.breakfast.title.isNotBlank())
        assertTrue(plan.lunch.title.isNotBlank())
        assertTrue(plan.dinner.title.isNotBlank())

        // Foods to avoid
        assertTrue(plan.foodsToAvoid.any { it.contains("Azúcar", ignoreCase = true) || it.contains("Gaseosas", ignoreCase = true) })
    }

    @Test
    fun testDyslipidemiaMealPlanHasEvidenceAndThreeMeals() {
        val plan = NutritionAdvisor.getPlanForPathology(PathologyCategory.DISLIPIDEMIA)
        assertEquals(PathologyCategory.DISLIPIDEMIA, plan.category)

        // Medical evidence backed by NCEP ATP III / ESC
        assertTrue(plan.scientificEvidenceNotes.contains("NCEP ATP III") || plan.scientificEvidenceNotes.contains("ESC"))

        // Desayuno, Almuerzo, Cena
        assertTrue(plan.breakfast.title.isNotBlank())
        assertTrue(plan.lunch.title.isNotBlank())
        assertTrue(plan.dinner.title.isNotBlank())

        // Key nutrients
        assertTrue(plan.keyNutrients.any { it.contains("Omega-3", ignoreCase = true) })
    }

    @Test
    fun testRenalHepaticMealPlanHasEvidenceAndThreeMeals() {
        val plan = NutritionAdvisor.getPlanForPathology(PathologyCategory.RENAL_HEPATICA)
        assertEquals(PathologyCategory.RENAL_HEPATICA, plan.category)

        // Medical evidence backed by KDIGO / EASL
        assertTrue(plan.scientificEvidenceNotes.contains("KDIGO") || plan.scientificEvidenceNotes.contains("EASL"))

        // Desayuno, Almuerzo, Cena
        assertTrue(plan.breakfast.title.isNotBlank())
        assertTrue(plan.lunch.title.isNotBlank())
        assertTrue(plan.dinner.title.isNotBlank())

        // Hydration and safety
        assertTrue(plan.hydrationAndTarvTips.isNotEmpty())
    }

    @Test
    fun testGeneratePlanWithConditionsMatchesPathology() {
        val planHtn = NutritionAdvisor.generatePlan(
            weightKg = 72f,
            heightCm = 170f,
            goal = "Mantener salud",
            chronicConditions = listOf("Hipertensión arterial")
        )
        assertEquals(PathologyCategory.HIPERTENSION, planHtn.activePathologyPlan.category)

        val planDm = NutritionAdvisor.generatePlan(
            weightKg = 80f,
            heightCm = 175f,
            goal = "Bajar de peso",
            chronicConditions = listOf("Diabetes tipo 2")
        )
        assertEquals(PathologyCategory.DIABETES_T2, planDm.activePathologyPlan.category)

        val planRenal = NutritionAdvisor.generatePlan(
            weightKg = 65f,
            heightCm = 168f,
            goal = "Cuidado renal",
            chronicConditions = listOf("Afección Renal / Hepática")
        )
        assertEquals(PathologyCategory.RENAL_HEPATICA, planRenal.activePathologyPlan.category)
    }
}
