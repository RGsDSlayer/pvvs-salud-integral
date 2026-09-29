package com.example.util

data class MealSuggestion(
    val title: String,
    val description: String,
    val iconName: String = "restaurant",
    val caloriesApprox: String,
    val proteinApprox: String,
    val carbsApprox: String = "",
    val fatsApprox: String = "",
    val clinicalRationale: String = "",
    val ingredients: List<String> = emptyList()
)

enum class PathologyCategory(
    val id: String,
    val displayName: String,
    val subtitle: String,
    val medicalGuideline: String,
    val guidelineBadge: String,
    val colorHex: Long,
    val iconType: String
) {
    HIPERTENSION(
        id = "hipertension",
        displayName = "Hipertensión Arterial",
        subtitle = "Control estricto de presión arterial y protección endotelial",
        medicalGuideline = "Guías Clínicas DASH (Dietary Approaches to Stop Hypertension) y Consenso AHA/ACC",
        guidelineBadge = "Guía DASH / AHA",
        colorHex = 0xFFD32F2F,
        iconType = "favorite"
    ),
    DIABETES_T2(
        id = "diabetes_t2",
        displayName = "Diabetes Tipo 2",
        subtitle = "Control glucémico, prevención de picos postprandiales y resistencia insulínica",
        medicalGuideline = "Estándares de Atención Médica en Diabetes - ADA 2024 (American Diabetes Association)",
        guidelineBadge = "Guía ADA 2024",
        colorHex = 0xFF0288D1,
        iconType = "bloodtype"
    ),
    DISLIPIDEMIA(
        id = "dislipidemia",
        displayName = "Dislipidemia",
        subtitle = "Reducción de Colesterol LDL, Triglicéridos y prevención de aterogénesis",
        medicalGuideline = "Directrices NCEP ATP III y Guías conjuntas ESC/EAS sobre Dislipidemias",
        guidelineBadge = "Guía NCEP ATP III / ESC",
        colorHex = 0xFFF57C00,
        iconType = "water_drop"
    ),
    RENAL_HEPATICA(
        id = "renal_hepatica",
        displayName = "Afección Renal / Hepática",
        subtitle = "Protección glomerular nefro-hepática, desintoxicación y equilibrio electrolítico",
        medicalGuideline = "Guías de Práctica Clínica KDIGO (Renal) y Guías Clínicas EASL (Hepática)",
        guidelineBadge = "Guía KDIGO / EASL",
        colorHex = 0xFF00897B,
        iconType = "shield"
    ),
    INMUNO_GENERAL(
        id = "inmuno_general",
        displayName = "Salud Inmune General PVVS",
        subtitle = "Soporte virológico celular, linfocitos CD4 y mantenimiento de masa muscular magra",
        medicalGuideline = "Directrices de Nutrición y VIH/SIDA de la OMS / ONUSIDA y FAO",
        guidelineBadge = "Guía OMS / ONUSIDA",
        colorHex = 0xFF2E7D32,
        iconType = "verified"
    )
}

data class PathologyNutritionPlan(
    val category: PathologyCategory,
    val clinicalRationaleSummary: String,
    val scientificEvidenceNotes: String,
    val breakfast: MealSuggestion,
    val lunch: MealSuggestion,
    val dinner: MealSuggestion,
    val snack: MealSuggestion,
    val foodsToAvoid: List<String>,
    val keyNutrients: List<String>,
    val hydrationAndTarvTips: List<String>
)

data class NutritionPlan(
    val bmi: Float,
    val bmiCategory: String,
    val bmiColorHex: Long,
    val summaryAdvice: String,
    val breakfast: MealSuggestion,
    val lunch: MealSuggestion,
    val snack: MealSuggestion,
    val dinner: MealSuggestion,
    val micronutrientFocus: List<String>,
    val tarvSafetyTips: List<String>,
    val chronicConditionNotes: List<String>,
    val activePathologyPlan: PathologyNutritionPlan,
    val allPathologyPlans: List<PathologyNutritionPlan>
)

object NutritionAdvisor {

    fun calculateBmi(weightKg: Float, heightCm: Float): Float {
        if (heightCm <= 0f || weightKg <= 0f) return 22.0f
        val heightM = heightCm / 100f
        return (weightKg / (heightM * heightM) * 10).toInt() / 10f
    }

    fun getBmiCategory(bmi: Float): String {
        return when {
            bmi < 18.5f -> "Bajo Peso"
            bmi < 25.0f -> "Peso Saludable / Normal"
            bmi < 30.0f -> "Sobrepeso"
            bmi < 35.0f -> "Obesidad Grado I"
            else -> "Obesidad Grado II o Mayor"
        }
    }

    fun getPlanForPathology(category: PathologyCategory): PathologyNutritionPlan {
        return when (category) {
            PathologyCategory.HIPERTENSION -> PathologyNutritionPlan(
                category = PathologyCategory.HIPERTENSION,
                clinicalRationaleSummary = "Patrón alimentario hiposódico enriquecido con potasio, magnesio y calcio bioactivo. Promueve la vasodilatación mediante la estimulación de óxido nítrico y alivia la resistencia vascular periférica.",
                scientificEvidenceNotes = "Basado en los ensayos clínicos multicéntricos del estudio DASH (Dietary Approaches to Stop Hypertension) respaldados por la American Heart Association (AHA) y el American College of Cardiology (ACC). Reduce la presión arterial sistólica entre 8 y 14 mmHg.",
                breakfast = MealSuggestion(
                    title = "Desayuno DASH Cardioprotector con Avena, Plátano y Arándanos",
                    description = "Tazón de avena integral cocida en agua o leche descremada sin azúcar, aromatizada con canela en polvo. Servido con rodajas de plátano fresco (rico en potasio), arándanos (antocianinas) y 2 claras de huevo revueltas con espinaca fresca en gotas de aceite de oliva virgen extra. Acompañado de infusión de flor de jamaica/hibisco.",
                    caloriesApprox = "360 kcal",
                    proteinApprox = "22g proteína",
                    carbsApprox = "48g carbohidratos complejos",
                    fatsApprox = "8g grasas monoinsaturadas",
                    clinicalRationale = "Aporte de 850 mg de potasio y 95 mg de magnesio que compiten activamente con el sodio en los túbulos renales, facilitando la natriuresis y la relajación del músculo liso arteriolar.",
                    ingredients = listOf("Avena en hojuelas enteras (50g)", "1 plátano maduro mediano", "1/2 taza de arándanos frescos", "2 claras de huevo fresco pasteurizado", "1 taza de hojas de espinaca fresca", "Infusión de flor de jamaica sin azúcar")
                ),
                lunch = MealSuggestion(
                    title = "Almuerzo Hiposódico Rico en Óxido Nítrico con Pescado Blanco y Betarraga",
                    description = "Filete de pescado blanco (merluza, trucha o corvina) a la plancha marinado con romero, orégano, ajo triturado y zumo de limón fresco (0% sal añadida). Servido con quinoa perlada al vapor y ensalada viva de betarraga rallada, rúcula, apio y rodajas de pepino con 1 cucharada sopera de aceite de oliva extra virgen.",
                    caloriesApprox = "510 kcal",
                    proteinApprox = "36g proteína magra",
                    carbsApprox = "52g carbohidratos de bajo índice glucémico",
                    fatsApprox = "14g lípidos saludables",
                    clinicalRationale = "Los nitratos dietéticos naturales de la betarraga y la rúcula se biotransforman en óxido nítrico endotelial, disminuyendo significativamente la rigidez arterial y la poscarga cardíaca.",
                    ingredients = listOf("Filete de pescado blanco fresco 150g", "Quinoa cocida 3/4 taza", "1/2 betarraga/remolacha fresca rallada", "1 taza de rúcula y apio cortado", "1 cucharada de aceite de oliva virgen extra", "Limón y finas hierbas naturales")
                ),
                dinner = MealSuggestion(
                    title = "Cena Ligera Sedante Vascular con Crema de Calabacín y Pavo",
                    description = "Crema suave de calabacín (zucchini), poro/puerro y apio preparada al vapor sin caldos concentrados ni salmuera, emulsionada con aceite de oliva en crudo. Acompañada de pechuga de pavo desmenuzada al orégano y rodajas de tomate fresco. Infusión nocturna de manzanilla o melisa sin azúcar.",
                    caloriesApprox = "320 kcal",
                    proteinApprox = "28g proteína",
                    carbsApprox = "22g carbohidratos",
                    fatsApprox = "10g lípidos",
                    clinicalRationale = "Digestión rápida con nulo residuo osmótico sódico. Previene el incremento tensional durante el ciclo circadiano del sueño (patrón non-dipper) y previene la retención de líquidos nocturna.",
                    ingredients = listOf("Pechuga de pavo a la plancha 120g", "2 calabacines medianos al vapor", "1 tallo de poro/puerro y apio", "1 tomate maduro en rodajas", "Infusión de melisa o tilo")
                ),
                snack = MealSuggestion(
                    title = "Merienda Cardioprotectora",
                    description = "1 manzana fresca con cáscara + 6 nueces crudas sin sal (fuente de ácido alfa-linolénico).",
                    caloriesApprox = "160 kcal",
                    proteinApprox = "4g proteína"
                ),
                foodsToAvoid = listOf(
                    "Embutidos, jamones curados, salchichas, tocino y patés (sodio crítico > 1,200 mg/100g).",
                    "Cubitos de caldo concentrado de carne o pollo, sopas deshidratadas y glutamato monosódico.",
                    "Salsas comerciales ultraprocesadas: sillao/soya regular, mostaza comercial, kétchup y aderezos embotellados.",
                    "Enlatados en salmuera (atún en salmuera, aceitunas saladas, encurtidos con alto sodio).",
                    "Snacks fritos salados, papas fritas embolsadas, galletas saladas procesadas.",
                    "Bebidas energéticas, gaseosas regulares y consumo excesivo de cafeína concentrada."
                ),
                keyNutrients = listOf(
                    "Potasio (K+): Contrarresta la retención de sodio y reduce la tensión arterial (plátano, espinacas, palta, calabaza).",
                    "Magnesio (Mg2+): Modula el tono vascular arterial y previene vasoespasmos (semillas de calabaza, avena, nueces).",
                    "Calcio biológico: Regula la excitabilidad neuromuscular vascular (yogurt descremado, semillas de chía, vegetales verdes oscuros).",
                    "Polifenoles y nitratos naturales: Favorecen la síntesis de óxido nítrico endotelial (betarraga, rúcula, arándanos)."
                ),
                hydrationAndTarvTips = listOf(
                    "Beber entre 2.0 y 2.5 litros de agua pura al día para asegurar la excreción renal del exceso de sodio.",
                    "La medicación TARV debe tomarse con agua pura, evitando acompañarla con aguas minerales con alto sodio gaseoso.",
                    "El uso de diuréticos o antihipertensivos debe sincronizarse con la toma del TARV según prescripción médica."
                )
            )

            PathologyCategory.DIABETES_T2 -> PathologyNutritionPlan(
                category = PathologyCategory.DIABETES_T2,
                clinicalRationaleSummary = "Estrategia hipoglucemiante basada en el Método del Plato de la ADA y alimentos de baja carga glucémica. Maximiza la fibra soluble para enlentecer la absorción intestinal de glucosa y optimizar la captación periférica de glucosa vía GLUT-4.",
                scientificEvidenceNotes = "Alineado con los Estándares de Atención Médica en Diabetes de la American Diabetes Association (ADA 2024) y ensayos clínicos de Dieta Mediterránea de Bajo Índice Glucémico. Reduce la HbA1c y estabiliza la variabilidad glucémica postprandial.",
                breakfast = MealSuggestion(
                    title = "Desayuno Normoglucémico con Omelette de Espinaca, Champiñones y Palta",
                    description = "Omelette elaborado con 2 huevos enteros camperos, espinacas frescas picadas, champiñones y semillas de chía cocinado con aceite de oliva. Servido con 1 rebanada de pan 100% centeno integral o masa madre (alto en beta-glucanos) y 1/4 de palta/aguacate. Acompañado de café negro o té verde con canela ceilán pura.",
                    caloriesApprox = "340 kcal",
                    proteinApprox = "20g proteína",
                    carbsApprox = "16g carbohidratos (7g fibra dietética)",
                    fatsApprox = "18g grasas monoinsaturadas saludables",
                    clinicalRationale = "Matriz lipoproteica y de fibra que aplana por completo la curva glucémica matutina. La canela actúa como sensibilizador de los receptores de insulina insulares.",
                    ingredients = listOf("2 huevos frescos camperos", "1 taza de espinacas tiernas", "4 champiñones en rodajas", "1 rebanada de pan de centeno integral 100%", "1/4 de palta en rebanadas", "Té verde con canela en polvo")
                ),
                lunch = MealSuggestion(
                    title = "Almuerzo Método del Plato ADA: Lentejas, Pollo al Romero y Ensalada Verde",
                    description = "Estructura de plato balanceado: 50% de ensalada verde fresca (lechuga romana, pepino con piel, rabanitos, apio y espárragos con vinagre de manzana y oliva virgen extra); 25% de proteína magra (pechuga de pollo a la plancha con romero y ajo); y 25% de carbohidrato complejo de absorción lenta (1/2 taza de lentejas cocidas con zanahoria picada y laurel).",
                    caloriesApprox = "480 kcal",
                    proteinApprox = "38g proteína magra",
                    carbsApprox = "36g carbohidratos complejos (12g fibra neta)",
                    fatsApprox = "12g grasas insaturadas",
                    clinicalRationale = "El ácido acético del vinagre de manzana y la fibra soluble (amilosa) de las lentejas retrasan el vaciamiento gástrico, inhibiendo la alfa-glucosidasa y previniendo los picos de glucosa a las 2 horas.",
                    ingredients = listOf("Pechuga de pollo a la plancha 140g", "Lentejas pardinas cocidas 1/2 taza", "Plato abundante de lechuga romana, pepino y espárragos", "1 cucharada de vinagre de manzana orgánico", "1 cucharada de aceite de oliva virgen extra")
                ),
                dinner = MealSuggestion(
                    title = "Cena Hipoglucemiante con Pescado Blanco, Brócoli y Camote Asado",
                    description = "Filete de pescado blanco (merluza o trucha) cocinado al vapor sobre lecho de brócoli al dente, calabacín y espárragos verdes rociados con aceite de oliva virgen extra y semillas de sésamo. Acompañado de 2 cucharadas de camote/batata asado con su cáscara (almidón resistente prebiótico). Infusión tibia de manzanilla.",
                    caloriesApprox = "310 kcal",
                    proteinApprox = "30g proteína",
                    carbsApprox = "18g carbohidratos (6g fibra)",
                    fatsApprox = "9g grasas monoinsaturadas",
                    clinicalRationale = "El sulforafano del brócoli reduce la gluconeogénesis hepática nocturna. La proteína magra de pescado blanco previene hipoglucemias y estabiliza los niveles basales de glucosa hasta el amanecer.",
                    ingredients = listOf("Filete de pescado blanco 140g al vapor", "1 taza de brócoli fresco al dente", "1 calabacín asado en bastones", "2 cucharadas de camote asado con piel", "Semillas de ajonjolí y aceite de oliva")
                ),
                snack = MealSuggestion(
                    title = "Colación Glucosa Estable",
                    description = "1 taza de yogurt griego natural sin azúcar con 8 almendras crudas picadas. Proteína pura sin almidón.",
                    caloriesApprox = "140 kcal",
                    proteinApprox = "13g proteína"
                ),
                foodsToAvoid = listOf(
                    "Azúcar blanca, azúcar rubia, panela, miel de abeja en exceso y jarabe de maíz de alta fructosa.",
                    "Bebidas gaseosas regulares, jugos de fruta envasados e incluso jugos naturales colados (sin fibra disparan la glucemia).",
                    "Harinas blancas refinadas, pan blanco de molde, galletas dulces, queques y productos de panadería industrial.",
                    "Arroz blanco simple en porciones gigantes o puré de papa instantáneo con alto índice glucémico.",
                    "Golosinas, chocolates de leche con azúcar añadida, mermeladas industriales y cereales azucarados de caja.",
                    "Alcohol en ayunas (peligro crítico de hipoglucemia severa tardía por inhibición de la gluconeogénesis hepática)."
                ),
                keyNutrients = listOf(
                    "Fibra Soluble (Beta-glucanos y Pectinas): Forma un gel intraluminal que frena la absorción de azúcares (avena, legumbres, chía).",
                    "Cromo y Magnesio: Oligoelementos catalizadores de la cascada de señalización del receptor de insulina (nueces, verduras de hoja verde).",
                    "Ácidos Grasos Monoinsaturados (Omega-9): Mejoran la fluidez de membrana y la translocación de GLUT-4 (aceite de oliva, palta).",
                    "Antioxidantes Anti-Glicación: Previenen el daño microvascular retiniano y renal por hemoglobina glicada (frutos rojos, té verde)."
                ),
                hydrationAndTarvTips = listOf(
                    "Monitorear la glucemia en ayunas regularmente, especialmente al iniciar o cambiar esquemas de TARV.",
                    "Beber abundante agua pura entre comidas para favorecer la función renal y la eliminación de cuerpos cetónicos si fuera el caso.",
                    "Nunca omitir comidas principales para evitar desequilibrios en el perfil glucémico."
                )
            )

            PathologyCategory.DISLIPIDEMIA -> PathologyNutritionPlan(
                category = PathologyCategory.DISLIPIDEMIA,
                clinicalRationaleSummary = "Terapia nutricional hipolipemiante centrada en la reducción rigurosa de ácidos grasos saturados (<7% de calorías totales) y eliminación total de grasas trans. Enriquecida con fibra soluble, fitoesteroles y ácidos grasos Omega-3 de cadena larga (EPA/DHA) para reducir triglicéridos y colesterol LDL aterogénico.",
                scientificEvidenceNotes = "Fundamentado en el panel de expertos del National Cholesterol Education Program (NCEP ATP III) y las directrices conjuntas de la European Society of Cardiology (ESC) y la European Atherosclerosis Society (EAS). Disminuye el colesterol LDL entre 10% y 25% y triglicéridos hasta en un 30%.",
                breakfast = MealSuggestion(
                    title = "Desayuno Hipolipemiante con Porridge de Avena, Chía y Nueces",
                    description = "Avena integral en hojuelas cocida con bebida de soja o almendras sin azúcar, 1 cucharada sopera de semillas de chía hidratadas (omega-3 ALA vegetal), 4 mitades de nueces troceadas y 1/2 taza de fresas o moras frescas. Acompañado de infusión de té verde rico en epigalocatequina galato (EGCG).",
                    caloriesApprox = "350 kcal",
                    proteinApprox = "14g proteína vegetal",
                    carbsApprox = "44g carbohidratos ricos en fibra soluble (9g fibra)",
                    fatsApprox = "13g lípidos cardioprotectores (0g colesterol)",
                    clinicalRationale = "Los beta-glucanos de la avena y los mucílagos de la chía atrapan el colesterol y las sales biliares en la luz ileal, forzando al hígado a sobreexpresar receptores LDL para captar colesterol sérico.",
                    ingredients = listOf("Avena integral en hojuelas 45g", "Semillas de chía hidratadas 15g", "4 nueces peladas picadas", "Fresas o moras frescas 1/2 taza", "Bebida vegetal sin azúcar añadida 200ml", "Té verde antioxidante")
                ),
                lunch = MealSuggestion(
                    title = "Almuerzo Cardioprotector Omega-3 con Pescado Azul y Alcachofas",
                    description = "Filete de pescado azul (caballa, jurel, salmón o trucha) horneado o a la plancha con limón, orégano y ajo. Servido con 1/2 taza de arroz integral o quinoa roja, y guarnición abundante de alcachofas al vapor con ensalada de espinaca baby, tomate y 1 cucharada de aceite de oliva extra virgen prensado en frío.",
                    caloriesApprox = "520 kcal",
                    proteinApprox = "35g proteína marina de alto valor",
                    carbsApprox = "42g carbohidratos complejos",
                    fatsApprox = "19g grasas saludables (altísimo Omega-3 EPA/DHA)",
                    clinicalRationale = "El EPA y DHA reducen la síntesis hepática de apolipoproteína B-100 y triglicéridos. La cinarina de la alcachofa estimula la coléresis y la degradación biliar del colesterol excedente.",
                    ingredients = listOf("Filete de pescado azul 150g", "Arroz integral o quinoa 1/2 taza", "2 corazones de alcachofa al vapor", "Espinacas tiernas y tomate", "1 cucharada de aceite de oliva virgen extra")
                ),
                dinner = MealSuggestion(
                    title = "Cena Antiaterogénica con Salteado de Tofu o Pavo y Vegetales",
                    description = "Salteado de dados de tofu firme o pechuga de pavo con berenjena en cubos, champiñones, pimiento rojo y calabacín, cocinados con 1 cucharadita de aceite de oliva virgen extra y semillas de sésamo/ajonjolí. Acompañado de ensalada cruda de pepino con vinagre de manzana. Infusión depurativa de alcachofa o boldo.",
                    caloriesApprox = "330 kcal",
                    proteinApprox = "27g proteína",
                    carbsApprox = "18g carbohidratos (6g fibra)",
                    fatsApprox = "11g grasas poliinsaturadas",
                    clinicalRationale = "Cero grasas saturadas sólidas y aporte de fitoesteroles que compiten con las micelas lipídicas en los enterocitos. La berenjena aporta antioxidantes que previenen la oxidación de las partículas de LDL.",
                    ingredients = listOf("Tofu firme o pechuga de pavo 130g", "Berenjena en cubos al vapor/salteada", "Champiñones frescos y pimiento", "Calabacín en rodajas", "Semillas de sésamo y aceite de oliva")
                ),
                snack = MealSuggestion(
                    title = "Merienda Fitoesterol",
                    description = "1 puñado pequeño de almendras y pistachos sin tostar ni sal + 1 kiwi rico en vitamina C y fibra prebiótica.",
                    caloriesApprox = "160 kcal",
                    proteinApprox = "5g proteína"
                ),
                foodsToAvoid = listOf(
                    "Grasas trans industriales e hidrogenadas: margarinas sólidas, mantecas para repostería y hojaldres.",
                    "Carnes rojas grasosas con veteado blanco, costillas de cerdo, piel de pollo/pavo, chicharrones y embutidos grasos.",
                    "Frituras profundas y aceites vegetales recalentados o reutilizados (generan peróxidos lipídicos aterogénicos).",
                    "Lácteos enteros con grasa: leche entera, crema de leche espesa, mantequilla, quesos grasos amarillos y curados.",
                    "Alimentos con colesterol libre masivo: vísceras (hígado, sesos, riñón) y mayonesa industrial en exceso.",
                    "Bebidas alcohólicas (los azúcares del alcohol son convertidos de inmediato por el hígado en triglicéridos séricos)."
                ),
                keyNutrients = listOf(
                    "Ácidos Grasos Omega-3 (EPA / DHA): Reducen los triglicéridos séricos, disminuyen la inflamación endotelial y estabilizan la placa aterosclerótica.",
                    "Fibra Soluble Pectina y Mucílagos: Bloquea la reabsorción enterohepática de colesterol (avena, manzana, semillas de chía).",
                    "Fitoesteroles Vegetales: Reducen la absorción de colesterol dietético compitiendo a nivel de transportador NPC1L1 (aceites vegetales no refinados, nueces).",
                    "Polifenoles y Flavonoides: Evitan la oxidación de partículas de LDL (té verde, frutos del bosque, aceite de oliva virgen)."
                ),
                hydrationAndTarvTips = listOf(
                    "Ciertos esquemas antirretrovirales históricos o inhibidores de proteasa pueden alterar el perfil lipídico; este plan previene activamente dicha dislipidemia medicamentosa.",
                    "Mantener actividad física aeróbica regular de 30 minutos diarios combinada con este plan eleva el colesterol protector HDL.",
                    "Controlar perfil lipídico completo cada 6 meses en conjunto con la carga viral."
                )
            )

            PathologyCategory.RENAL_HEPATICA -> PathologyNutritionPlan(
                category = PathologyCategory.RENAL_HEPATICA,
                clinicalRationaleSummary = "Terapia nefroprotectora y hepatoprotectora orientada a reducir la sobrecarga de filtración glomerular y el estrés oxidativo microsomal hepático. Control cuali-cuantitativo de proteínas de alto valor biológico (sin sobrepasar 0.8g/kg/día), restricción rigurosa de sodio, control de fósforo inorgánico (aditivos alimentarios) y protección de la barrera intestinal.",
                scientificEvidenceNotes = "Respaldado por las Guías Clínicas Internacionales KDIGO (Kidney Disease: Improving Global Outcomes) para la preservación de la tasa de filtración glomerular y las directrices de la European Association for the Study of the Liver (EASL) para la prevención de esteatohepatitis y toxicidad farmacológica en pacientes bajo terapia antirretroviral.",
                breakfast = MealSuggestion(
                    title = "Desayuno Nefro-Hepático con Claras de Huevo y Manzana Asada",
                    description = "Revoltillo de 2 claras de huevo cocidas con orégano y unas gotas de aceite de oliva virgen extra sobre 1 rebanada de pan blanco sin sal y libre de conservantes de fósforo artificial. Acompañado de 1 manzana cocida o asada al horno con canela (fácil digestión, bajo fósforo y potasio controlado) y té suave de manzanilla o diente de león sin azúcar.",
                    caloriesApprox = "280 kcal",
                    proteinApprox = "16g proteína limpia de alto valor biológico",
                    carbsApprox = "36g carbohidratos suaves",
                    fatsApprox = "6g lípidos esenciales",
                    clinicalRationale = "La ovoalbúmina de las claras tiene el mayor coeficiente de utilización neta de nitrógeno sin generar exceso de urea ni ácido úrico, protegiendo las nefronas residuales. La manzana asada aporta pectina protectora hepática sin sobrecarga de fósforo.",
                    ingredients = listOf("2 claras de huevo fresco", "1 rebanada de pan blanco artesanal sin sal ni fosfatos", "1 manzana dulce asada con canela", "Gotas de aceite de oliva virgen extra", "Té de manzanilla o diente de león")
                ),
                lunch = MealSuggestion(
                    title = "Almuerzo Protector Glomerular con Pollo Magro y Vegetales al Vapor",
                    description = "Porción controlada (90-100g) de pechuga de pollo desgrasada al vapor aromatizada con laurel, orégano y jengibre fresco. Acompañada de 3/4 taza de arroz blanco cocido o fideos de arroz, y guarnición de calabacín, zanahoria y vainitas al vapor (preparadas con remojo previo si requiere control estricto de potasio). Condimentado en crudo con 1 cucharadita de aceite de oliva, sin sal común.",
                    caloriesApprox = "440 kcal",
                    proteinApprox = "26g proteína neta",
                    carbsApprox = "54g carbohidratos de fácil asimilación",
                    fatsApprox = "9g grasas saludables",
                    clinicalRationale = "La restricción moderada de proteínas evita la hiperfiltración capilar intraglomerular y la proteinuria. Cero aditivos de fosfato industrial. El jengibre y el laurel favorecen la función biliar sin generar estrés a los hepatocitos.",
                    ingredients = listOf("Pechuga de pollo fresca 95g al vapor", "Arroz blanco hervido 3/4 taza", "Calabacín y zanahoria al vapor 1 taza", "Vainitas tiernas cocidas", "1 cucharadita de aceite de oliva virgen extra")
                ),
                dinner = MealSuggestion(
                    title = "Cena Ligera Hipotóxica con Pescado Blanco y Crema de Zapallo",
                    description = "Crema suave de zapallo/calabaza y zanahoria hervida en agua pura, aderezada con una pizca de cúrcuma y gotas de aceite de oliva en crudo. Acompañada de un filete pequeño (80g) de pescado blanco magro (lenguado o corvina) a la plancha con limón. Infusión nocturna tibia de cardo mariano (silimarina protectora hepática) o menta poleo.",
                    caloriesApprox = "290 kcal",
                    proteinApprox = "22g proteína ligera",
                    carbsApprox = "28g carbohidratos",
                    fatsApprox = "7g grasas saludables",
                    clinicalRationale = "Minimiza la generación de amonio y productos nitrogenados durante el descanso nocturno. La silimarina estabiliza las membranas celulares de los hepatocitos, amortiguando el metabolismo de la polifarmacia y protegiendo el citocromo P450.",
                    ingredients = listOf("Filete de lenguado o corvina 85g", "Zapallo/calabaza hervida 1 taza", "Zanahoria cocida suave", "Gotas de aceite de oliva virgen", "Infusión de cardo mariano")
                ),
                snack = MealSuggestion(
                    title = "Merienda Suave Renal-Hepática",
                    description = "Compota casera de pera al vapor con canela en rama (sin azúcar añadida, hidratante y baja en residuos proteicos).",
                    caloriesApprox = "100 kcal",
                    proteinApprox = "1g proteína"
                ),
                foodsToAvoid = listOf(
                    "Fosfatos inorgánicos industriales (aditivos E-338 a E-452 presentes en gaseosas negras, embutidos y carnes procesadas): se absorben casi al 100% y aceleran la calcificación vascular y daño renal.",
                    "Bebidas alcohólicas bajo cualquier forma (absolutamente contraindicadas: toxicidad hepática directa e interacción con antirretrovirales).",
                    "Carnes crudas, ceviches o mariscos crudos (riesgo crítico de infecciones gastrointestinales oportunistas y sepsis por Vibrio o Salmonella en pacientes con TARV).",
                    "Sal común de mesa en exceso, cubos de caldo y sazonadores industriales con glutamato.",
                    "Sustitutos comerciales de sal a base de Cloruro de Potasio (peligro inminente de hiperpotasemia letal en pacientes con función renal comprometida).",
                    "Exceso descontrolado de carnes rojas y suplementos proteicos de gimnasio (batidos de whey protein creatina sin supervisión médica)."
                ),
                keyNutrients = listOf(
                    "Proteínas de Alto Valor Biológico Controladas: Mantienen la masa celular magra sin generar sobrecarga nitrogenada (claras de huevo, pollo magro, pescado blanco).",
                    "Antioxidantes Hepáticos (Silimarina, Colina y Glutatión): Protegen el parénquima hepático de la toxicidad medicamentosa (cardo mariano, alcachofa, espinaca cocida).",
                    "Control de Sodio y Fósforo: Previene la hipertensión glomerular y la osteodistrofia renal.",
                    "Agua Ultra-Filtrada: Facilita la depuración renal constante y previene la litiasis por fármacos antirretrovirales como atazanavir."
                ),
                hydrationAndTarvTips = listOf(
                    "Beber entre 2.5 a 3.0 litros de agua pura al día para asegurar un flujo urinario continuo que evite la precipitación de metabolitos antirretrovirales en los túbulos renales.",
                    "Realizar control semestral de creatinina sérica, tasa de filtración glomerular estimada (eGFR) y transaminasas hepáticas (TGO/TGP).",
                    "Consultar de inmediato al médico si se presenta orina oscura, hinchazón en piernas/párpados o fatiga inusual."
                )
            )

            PathologyCategory.INMUNO_GENERAL -> PathologyNutritionPlan(
                category = PathologyCategory.INMUNO_GENERAL,
                clinicalRationaleSummary = "Nutrición integral para pacientes con diagnóstico de VIH basada en la preservación del sistema inmune, el aumento y mantenimiento de linfocitos CD4 y la prevención del síndrome de desgaste (wasting). Enfoque prioritario en inocuidad alimentaria y biodisponibilidad de micronutrientes.",
                scientificEvidenceNotes = "Directrices de Nutrición y VIH/SIDA de la Organización Mundial de la Salud (OMS) y ONUSIDA. Garantiza un aporte calórico-proteico óptimo que mejora la respuesta virológica al TARV y preserva la masa celular activa.",
                breakfast = MealSuggestion(
                    title = "Desayuno Inmuno-Protector Mediterráneo",
                    description = "Tostada de pan de masa madre integral con palta/aguacate fresco, 1 huevo pochado bien cocido (seguridad alimentaria), tomate en rodajas finas y semillas de girasol. Acompañado de 1 taza de papaya o melón fresco en cubos y café o infusión antioxidante.",
                    caloriesApprox = "410 kcal",
                    proteinApprox = "18g proteína",
                    carbsApprox = "42g carbohidratos",
                    fatsApprox = "17g grasas saludables",
                    clinicalRationale = "Combinación sinérgica de carotenoides, zinc del huevo y grasas monoinsaturadas para respaldar la síntesis de receptores celulares inmunes.",
                    ingredients = listOf("Pan de masa madre integral 60g", "1 huevo fresco bien cocido", "1/3 de palta en rodajas", "1 taza de papaya fresca", "Semillas de girasol y aceite de oliva")
                ),
                lunch = MealSuggestion(
                    title = "Almuerzo Balanceado Inmune con Lentejas y Pollo Dorado",
                    description = "Guiso tradicional de lentejas con zanahoria, espinaca y pimientos rojos, acompañado de 120g de pechuga de pollo a la plancha y 1/2 taza de arroz integral. Servido con ensalada de hojas verdes, pepino y aderezo de limón con aceite de oliva extra virgen.",
                    caloriesApprox = "540 kcal",
                    proteinApprox = "36g proteína",
                    carbsApprox = "58g carbohidratos complejos",
                    fatsApprox = "14g lípidos saludables",
                    clinicalRationale = "La combinación de vitamina C de los pimientos con el hierro y zinc de las lentejas optimiza la absorción hematológica y la producción de leucocitos.",
                    ingredients = listOf("Pechuga de pollo 120g", "Lentejas cocidas 3/4 taza", "Arroz integral 1/2 taza", "Espinacas y pimiento rojo picado", "Aceite de oliva y limón")
                ),
                dinner = MealSuggestion(
                    title = "Cena Equilibrada de Fácil Digestión con Crema de Zapallo y Atún",
                    description = "Crema natural de zapallo/calabaza con un toque de jengibre fresco, acompañada de atún en agua con ensalada fresca de hojas verdes, tomate cherry y semillas de chía. Infusión caliente de anís o manzanilla.",
                    caloriesApprox = "360 kcal",
                    proteinApprox = "28g proteína",
                    carbsApprox = "32g carbohidratos",
                    fatsApprox = "9g grasas insaturadas",
                    clinicalRationale = "Fácil asimilación celular nocturna sin interrumpir el descanso profundo, periodo durante el cual se produce la máxima regeneración celular de linfocitos T.",
                    ingredients = listOf("Atún al natural en agua 120g", "Crema de zapallo con jengibre 1 tazón", "Hojas verdes y tomates cherry", "Semillas de chía y gotas de oliva", "Infusión digestiva")
                ),
                snack = MealSuggestion(
                    title = "Colación Antioxidante",
                    description = "Yogurt griego natural pasteurizado con un puñado de arándanos frescos y 6 almendras.",
                    caloriesApprox = "180 kcal",
                    proteinApprox = "12g proteína"
                ),
                foodsToAvoid = listOf(
                    "Alimentos crudos o semicrudos de origen animal: carnes término medio, ceviche crudo, sushi, huevos con yema líquida o mariscos crudos (riesgo de salmonelosis o listeriosis).",
                    "Lácteos y quesos no pasteurizados (quesos artesanales de campo sin control sanitario).",
                    "Agua sin hervir o no embotellada / filtrada (riesgo de Cryptosporidium o Giardia).",
                    "Toronja / Pomelo y suplementos de Hierba de San Juan (interacción metabólica enzimática con antirretrovirales).",
                    "Alimentos ultraprocesados con nulo valor nutricional o exceso de colorantes artificiales."
                ),
                keyNutrients = listOf(
                    "Zinc y Selenio: Esenciales para la replicación y función adecuada de los linfocitos CD4 (pescados, huevos, legumbres, semillas).",
                    "Vitamina D3: Modula la respuesta inmune adaptativa y preserva la densidad mineral ósea frente al TARV.",
                    "Vitamina C y Vitamina E: Potentes antioxidantes que reducen el estrés oxidativo provocado por la replicación viral.",
                    "Proteínas de Alto Valor Biológico: Mantienen la masa celular muscular activa y previenen la caquexia."
                ),
                hydrationAndTarvTips = listOf(
                    "Consumir 2 a 2.5 litros de agua filtrada o hervida diariamente.",
                    "Tomar la medicación antirretroviral siempre a la misma hora del día, con o sin alimentos según indique el prospecto médico.",
                    "Lavar y desinfectar escrupulosamente todas las frutas y verduras que se consuman crudas."
                )
            )
        }
    }

    fun generatePlan(
        weightKg: Float,
        heightCm: Float,
        goal: String,
        chronicConditions: List<String>
    ): NutritionPlan {
        val bmi = calculateBmi(weightKg, heightCm)
        val category = getBmiCategory(bmi)

        val bmiColor = when {
            bmi < 18.5f -> 0xFFEF6C00 // Orange
            bmi < 25.0f -> 0xFF2E7D32 // Green
            bmi < 30.0f -> 0xFFF57C00 // Amber
            else -> 0xFFD32F2F // Red
        }

        val isUnderweight = bmi < 18.5f
        val isOverweight = bmi >= 25.0f

        val hasHypertension = chronicConditions.any { it.contains("Hipertensión", ignoreCase = true) }
        val hasDiabetes = chronicConditions.any { it.contains("Diabetes", ignoreCase = true) }
        val hasDyslipidemia = chronicConditions.any { it.contains("Dislipidemia", ignoreCase = true) || it.contains("Colesterol", ignoreCase = true) || it.contains("Triglicéridos", ignoreCase = true) }
        val hasRenalOrHepatic = chronicConditions.any { it.contains("Renal", ignoreCase = true) || it.contains("Hepática", ignoreCase = true) }

        // Determine active primary pathology plan based on diagnosed conditions
        val activeCategory = when {
            hasRenalOrHepatic -> PathologyCategory.RENAL_HEPATICA
            hasDiabetes -> PathologyCategory.DIABETES_T2
            hasHypertension -> PathologyCategory.HIPERTENSION
            hasDyslipidemia -> PathologyCategory.DISLIPIDEMIA
            else -> PathologyCategory.INMUNO_GENERAL
        }

        val activePathologyPlan = getPlanForPathology(activeCategory)
        val allPathologyPlans = PathologyCategory.values().map { getPlanForPathology(it) }

        val chronicNotes = mutableListOf<String>()
        if (hasHypertension) {
            chronicNotes.add("Hipertensión Arterial (Guía DASH): Límite de sodio < 2g/día. Potenciar potasio, magnesio y calcio natural.")
        }
        if (hasDiabetes) {
            chronicNotes.add("Diabetes Tipo 2 (Guía ADA): Control estricto de carga glucémica y carbohidratos complejos de absorción lenta.")
        }
        if (hasDyslipidemia) {
            chronicNotes.add("Dislipidemia (Guía NCEP ATP III): Eliminar grasas trans, limitar saturadas a < 7% y priorizar Omega-3 y fibra soluble.")
        }
        if (hasRenalOrHepatic) {
            chronicNotes.add("Afección Renal / Hepática (Guías KDIGO / EASL): Control cuantitativo de proteínas limpias, restricción de fósforo artificial y descanso metabólico hepático.")
        }
        if (chronicNotes.isEmpty()) {
            chronicNotes.add("Sin condiciones crónicas agregadas: Plan optimizado para el soporte inmune de linfocitos CD4 e inocuidad alimentaria.")
        }

        val summary = when {
            isUnderweight -> "Tu IMC indica bajo peso. El plan se enfoca en superávit calórico saludable con alta densidad de nutrientes para fortalecer tu masa muscular y tus defensas."
            isOverweight -> "Tu IMC indica sobrepeso. El plan prioriza alimentos saciantes con alto volumen y fibra, protegiendo tu masa muscular y evitando sobrecarga hepática y lipídica."
            else -> "¡Excelente! Tu IMC se encuentra en rango saludable. El plan está optimizado para mantener tu sistema inmune al máximo, proteger tus órganos y garantizar energía constante."
        }

        return NutritionPlan(
            bmi = bmi,
            bmiCategory = category,
            bmiColorHex = bmiColor,
            summaryAdvice = summary,
            breakfast = activePathologyPlan.breakfast,
            lunch = activePathologyPlan.lunch,
            snack = activePathologyPlan.snack,
            dinner = activePathologyPlan.dinner,
            micronutrientFocus = activePathologyPlan.keyNutrients,
            tarvSafetyTips = activePathologyPlan.hydrationAndTarvTips,
            chronicConditionNotes = chronicNotes,
            activePathologyPlan = activePathologyPlan,
            allPathologyPlans = allPathologyPlans
        )
    }
}
