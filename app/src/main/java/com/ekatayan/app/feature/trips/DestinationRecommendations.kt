package com.ekatayan.app.feature.trips

data class DestinationGuide(val places: List<String>, val description: String)

private val guides = mapOf(
    "kandy" to DestinationGuide(listOf("Temple of the Tooth", "Kandy Lake", "Peradeniya Botanical Garden", "Udawatta Kele"), "A cultural hill city filled with heritage, nature, and calm lake views."),
    "ella" to DestinationGuide(listOf("Nine Arch Bridge", "Little Adam's Peak", "Ravana Falls", "Ella Rock"), "A scenic mountain escape with tea country views and rewarding walks."),
    "galle" to DestinationGuide(listOf("Galle Fort", "Jungle Beach", "Unawatuna Beach", "Japanese Peace Pagoda"), "A coastal getaway where colonial history meets golden beaches."),
    "colombo" to DestinationGuide(listOf("Galle Face Green", "Colombo National Museum", "Viharamahadevi Park", "Pettah Market"), "A lively capital with food, culture, shopping, and oceanfront sunsets."),
    "nuwara eliya" to DestinationGuide(listOf("Gregory Lake", "Horton Plains", "Pedro Tea Estate", "Victoria Park"), "A cool-weather retreat surrounded by tea estates and green hills."),
    "sigiriya" to DestinationGuide(listOf("Sigiriya Rock Fortress", "Pidurangala Rock", "Minneriya National Park", "Dambulla Cave Temple"), "An ancient wonderland of rock, wildlife, and remarkable history."),
    "mirissa" to DestinationGuide(listOf("Mirissa Beach", "Parrot Rock", "Coconut Tree Hill", "Whale watching"), "A relaxed south-coast escape known for beaches, sunsets, and the sea."),
)

fun guideFor(destination: String): DestinationGuide = guides.entries.firstOrNull { destination.contains(it.key, ignoreCase = true) }?.value
    ?: DestinationGuide(emptyList(), "Explore local highlights, try the regional food, and ask locals for their favourite places.")
