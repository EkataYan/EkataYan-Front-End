package com.ekatayan.app.data.local

import com.ekatayan.app.R
import com.ekatayan.app.data.model.Attraction
import com.ekatayan.app.data.model.AttractionHighlight
import com.ekatayan.app.data.model.DestinationDetails
import com.ekatayan.app.data.model.WishlistItem
import com.ekatayan.app.data.model.WishlistItemType

/** Frontend prototype content behind the reusable destination and attraction routes. */
object DestinationDetailsCatalog {
    private val attractions = listOf(
        Attraction(
            id = "gal_vihara",
            wishlistItemId = 1001,
            parentDestinationId = "polonnaruwa",
            name = "Gal Vihara",
            location = "Polonnaruwa, Sri Lanka",
            shortDescription = "A remarkable rock temple featuring four iconic Buddha statues.",
            description = "Gal Vihara is one of Sri Lanka's most remarkable archaeological sites. Its four Buddha figures were carved directly into a long granite face during the Polonnaruwa era, combining quiet spiritual character with exceptional stone craftsmanship.",
            imageRes = R.drawable.polonnaruwa,
            categories = listOf("Historical", "Cultural", "UNESCO Site"),
            bestTime = "Early morning",
            idealFor = listOf("History", "Culture", "Photography"),
            highlights = listOf(
                AttractionHighlight("Reclining Buddha", R.drawable.polonnaruwa),
                AttractionHighlight("Seated Buddha", R.drawable.polonnaruwa),
                AttractionHighlight("Standing Buddha Statues", R.drawable.polonnaruwa),
            ),
            tips = listOf(
                "Wear comfortable clothing and footwear for walking around the site.",
                "Visit early in the morning to avoid the strongest heat.",
                "Carry water and sun protection.",
                "Respect the sacred setting and follow posted photography guidance.",
            ),
        ),
        Attraction(
            "royal_palace", 1002, "polonnaruwa", "Royal Palace", "Polonnaruwa, Sri Lanka",
            "The remains of the royal palace, once the heart of the medieval kingdom.",
            "The Royal Palace complex preserves the foundations and towering brick walls of King Parakramabahu's royal residence. Walking through the audience hall and surrounding ruins gives a clear sense of the scale of the medieval capital.",
            R.drawable.polonnaruwa, listOf("Historical", "Architecture"), bestTime = "Morning",
            idealFor = listOf("History", "Architecture"),
            tips = listOf("Allow time to explore the nearby council chamber and royal bath."),
        ),
        Attraction(
            "rankoth_vehera", 1003, "polonnaruwa", "Rankoth Vehera", "Polonnaruwa, Sri Lanka",
            "One of Sri Lanka's largest stupas, built during the reign of King Nissanka Malla.",
            "Rankoth Vehera is an immense brick stupa whose traditional form recalls the great monuments of Anuradhapura. Its open ceremonial grounds remain an active place of reverence.",
            R.drawable.anuradhapura, listOf("Religious", "Historical"), bestTime = "Morning or late afternoon",
            idealFor = listOf("Culture", "History"), tips = listOf("Dress respectfully and remove footwear where requested."),
        ),
        Attraction(
            "parakrama_samudra", 1004, "polonnaruwa", "Parakrama Samudra", "Polonnaruwa, Sri Lanka",
            "An ancient reservoir showcasing the advanced irrigation technology of the era.",
            "Parakrama Samudra is a vast historic reservoir associated with King Parakramabahu I. Its calm shoreline demonstrates how sophisticated water management sustained the ancient city and surrounding farmland.",
            R.drawable.minneriya_national_park, listOf("Nature", "Historical"), bestTime = "Sunset",
            idealFor = listOf("Scenery", "Photography"), tips = listOf("Stay on accessible public paths near the water."),
        ),
        Attraction(
            "lankatilaka_temple", 1005, "polonnaruwa", "Lankatilaka Temple", "Polonnaruwa, Sri Lanka",
            "A striking temple known for its impressive architecture and historical significance.",
            "Lankatilaka Temple is a monumental image house framed by tall brick walls. The surviving structure, sculptures and narrow approach reveal the ambition of Polonnaruwa's sacred architecture.",
            R.drawable.polonnaruwa, listOf("Cultural", "Historical"), bestTime = "Morning",
            idealFor = listOf("Architecture", "History"), tips = listOf("The stone surfaces can become hot, so visit earlier in the day."),
        ),
        Attraction(
            "sigiriya_rock_fortress", 1101, "sigiriya", "Sigiriya Rock Fortress", "Matale District, Sri Lanka",
            "The ancient citadel, gardens and summit ruins of Lion Rock.",
            "Sigiriya rises dramatically above landscaped water gardens and forest. The route to the summit passes historic frescoes and the Lion Gate before reaching the remains of a royal citadel.",
            R.drawable.sigiriya, listOf("Historical", "UNESCO Site"), bestTime = "Early morning",
            idealFor = listOf("History", "Views", "Hiking"), tips = listOf("Bring water and start before the midday heat."),
        ),
        Attraction(
            "pidurangala_rock", 1102, "sigiriya", "Pidurangala Rock", "Matale District, Sri Lanka",
            "A rewarding rock climb with a celebrated view of Sigiriya.",
            "Pidurangala combines a historic cave temple with a short, steep climb to a broad summit overlooking Sigiriya and the surrounding plains.",
            R.drawable.hiking, listOf("Nature", "Hiking"), bestTime = "Sunrise",
            idealFor = listOf("Hiking", "Photography"), tips = listOf("Wear shoes with good grip for the final rocky section."),
        ),
        Attraction(
            "temple_of_the_tooth", 1201, "kandy", "Temple of the Tooth", "Kandy, Sri Lanka",
            "Kandy's revered temple complex beside the lake.",
            "The Temple of the Sacred Tooth Relic is at the spiritual heart of Kandy. Its layered courtyards, shrines and ceremonial spaces reflect centuries of living Buddhist tradition.",
            R.drawable.kandy, listOf("Cultural", "Religious", "UNESCO Site"), bestTime = "Morning",
            idealFor = listOf("Culture", "History"), tips = listOf("Cover shoulders and knees and remove footwear before entering."),
        ),
        Attraction(
            "kandy_lake", 1202, "kandy", "Kandy Lake", "Kandy, Sri Lanka",
            "A peaceful lakeside walk at the centre of the hill city.",
            "Kandy Lake creates a calm green edge around the historic city centre and offers an easy walk with views toward the temple precinct.",
            R.drawable.kandy_esala_perahara, listOf("Nature", "Scenic"), bestTime = "Late afternoon",
            idealFor = listOf("Walking", "Photography"), tips = listOf("Use the shaded side of the lake during warmer hours."),
        ),
        Attraction(
            "galle_fort", 1301, "galle", "Galle Fort", "Galle, Sri Lanka",
            "Ramparts, lanes and colonial architecture beside the Indian Ocean.",
            "Galle Fort is a walkable historic quarter of ramparts, bastions, museums and lively streets. Its layered architecture records centuries of maritime exchange.",
            R.drawable.galle, listOf("Historical", "Coastal", "UNESCO Site"), bestTime = "Late afternoon",
            idealFor = listOf("History", "Walking", "Food"), tips = listOf("Walk the ramparts toward sunset and carry water."),
        ),
        Attraction(
            "unawatuna_beach", 1302, "galle", "Unawatuna Beach", "Galle District, Sri Lanka",
            "A sheltered crescent beach a short journey from Galle.",
            "Unawatuna is known for its curved golden shore, swimming areas and relaxed cafes, making it an easy coastal stop near Galle.",
            R.drawable.unawatuna, listOf("Beach", "Coastal"), idealFor = listOf("Swimming", "Relaxing"),
            tips = listOf("Check local sea conditions before swimming."),
        ),
        Attraction(
            "bridge_viewpoint", 1401, "nine_arch_bridge", "Bridge Viewpoint", "Ella, Sri Lanka",
            "A leafy viewpoint over the arches and railway line.",
            "Several forest paths around the Nine Arch Bridge open onto elevated views of the viaduct, tea-covered slopes and passing trains.",
            R.drawable.nine_arch_bridge, listOf("Scenic", "Photography"), bestTime = "Early morning",
            idealFor = listOf("Photography", "Walking"), tips = listOf("Remain clear of the railway line and follow local safety signs."),
        ),
        Attraction(
            "little_adams_peak", 1402, "nine_arch_bridge", "Little Adam's Peak", "Ella, Sri Lanka",
            "An accessible ridge walk with wide hill-country views.",
            "Little Adam's Peak is a popular short hike through tea country to a series of viewpoints above Ella's green valleys.",
            R.drawable.hiking, listOf("Hiking", "Nature"), bestTime = "Morning",
            idealFor = listOf("Hiking", "Views"), tips = listOf("Carry water and avoid exposed ridges during storms."),
        ),
    )

    private val destinations = listOf(
        DestinationDetails(
            "polonnaruwa", 14, "Polonnaruwa", "North Central Province, Sri Lanka",
            "Polonnaruwa, the medieval capital of Sri Lanka, is a UNESCO World Heritage Site renowned for remarkable ruins, ancient palaces, Buddhist temples and sophisticated irrigation systems. Flourishing between the 11th and 13th centuries, it reflects the island's rich history, advanced urban planning and enduring cultural heritage.",
            R.drawable.polonnaruwa, listOf("Historical", "Cultural", "UNESCO Site"),
            listOf("gal_vihara", "royal_palace", "rankoth_vehera", "parakrama_samudra", "lankatilaka_temple"),
        ),
        DestinationDetails(
            "sigiriya", 1, "Sigiriya", "Matale District, Sri Lanka",
            "Sigiriya is an ancient rock citadel surrounded by landscaped gardens, reservoirs and forest. The climb reveals frescoes, mirror-wall inscriptions and the monumental Lion Gate before reaching panoramic summit ruins.",
            R.drawable.sigiriya, listOf("Historical", "Cultural", "UNESCO Site"),
            listOf("sigiriya_rock_fortress", "pidurangala_rock"),
        ),
        DestinationDetails(
            "kandy", 12, "Kandy", "Central Province, Sri Lanka",
            "Kandy is Sri Lanka's cultural hill capital, shaped by a sacred temple, a central lake and green ridges. Its living traditions, historic streets and access to gardens make it a rewarding cultural base.",
            R.drawable.kandy, listOf("Cultural", "Hill Country", "UNESCO Site"),
            listOf("temple_of_the_tooth", "kandy_lake"),
        ),
        DestinationDetails(
            "galle", 15, "Galle", "Southern Province, Sri Lanka",
            "Galle brings together an atmospheric fortified old town and Sri Lanka's southern coast. Rampart walks, historic architecture, local food and nearby beaches create an easy mix of heritage and seaside travel.",
            R.drawable.galle, listOf("Historical", "Coastal", "UNESCO Site"),
            listOf("galle_fort", "unawatuna_beach"),
        ),
        DestinationDetails(
            "nine_arch_bridge", 11, "Nine Arch Bridge", "Ella, Uva Province, Sri Lanka",
            "Nine Arch Bridge is a graceful stone railway viaduct set among tea fields and dense hill-country greenery. Forest paths lead to several viewpoints around this much-loved Ella landmark.",
            R.drawable.nine_arch_bridge, listOf("Scenic", "Engineering", "Hill Country"),
            listOf("bridge_viewpoint", "little_adams_peak"),
        ),
    )

    private val destinationById = destinations.associateBy(DestinationDetails::id)
    private val destinationByWishlistId = destinations.associateBy(DestinationDetails::wishlistItemId)
    private val attractionById = attractions.associateBy(Attraction::id)

    fun destination(id: String): DestinationDetails? = destinationById[id]
    fun destinationIdForWishlistItem(itemId: Int): String? = destinationByWishlistId[itemId]?.id
    fun attraction(id: String): Attraction? = attractionById[id]
    fun popularPlaces(destination: DestinationDetails): List<Attraction> =
        destination.popularPlaceIds.mapNotNull(attractionById::get)

    val attractionWishlistItems: List<WishlistItem> = attractions.map { attraction ->
        WishlistItem(
            id = attraction.wishlistItemId,
            name = attraction.name,
            description = attraction.shortDescription,
            location = attraction.location,
            imageRes = attraction.imageRes,
            itemType = WishlistItemType.ATTRACTION,
            parentDestinationId = attraction.parentDestinationId,
        )
    }
}
