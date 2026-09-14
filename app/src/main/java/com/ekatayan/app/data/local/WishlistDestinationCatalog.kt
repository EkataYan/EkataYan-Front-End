package com.ekatayan.app.data.local

import com.ekatayan.app.data.model.WishlistItem

import com.ekatayan.app.R

/** Static destination catalogue; Room stores only stable destination IDs in wishlist membership rows. */
object WishlistDestinationCatalog {
    val destinations = listOf(
        WishlistItem(1, "Sigiriya", "An ancient rock fortress rising above Sri Lanka's central plains.", "Matale District", R.drawable.sigiriya),
        WishlistItem(2, "Dalada Maligawa", "A sacred Buddhist temple that houses the revered Tooth Relic.", "Kandy", R.drawable.kandy),
        WishlistItem(3, "Galle Fort", "A historic coastal fort filled with colonial architecture and ocean views.", "Galle", R.drawable.galle),
        WishlistItem(4, "Mirissa", "A laid-back southern beach known for whale watching and sunsets.", "Southern Province", R.drawable.mirissa),
        WishlistItem(5, "Unawatuna", "A sheltered golden beach with calm water and coral reefs.", "Galle District", R.drawable.unawatuna),
        WishlistItem(6, "Bentota", "A tropical resort town popular for beaches and water sports.", "Southern Province", R.drawable.bentota),
        WishlistItem(7, "Arugam Bay", "A world-famous surf destination on Sri Lanka's east coast.", "Eastern Province", R.drawable.arugambay),
        WishlistItem(8, "Ella", "A misty hill-country escape surrounded by tea estates and trails.", "Uva Province", R.drawable.ravana_falls),
        WishlistItem(9, "Nuwara Eliya", "Cool mountain scenery, tea gardens, and colonial charm.", "Central Province", R.drawable.hiking),
        WishlistItem(10, "Haputale", "A peaceful ridge town with sweeping views across the southern plains.", "Uva Province", R.drawable.hiking),
        WishlistItem(11, "Nine Arch Bridge", "An iconic stone railway bridge framed by Ella's lush jungle.", "Ella", R.drawable.nine_arch_bridge),
        WishlistItem(12, "Kandy", "Sri Lanka's cultural capital, set beside a scenic lake.", "Central Province", R.drawable.kandy),
        WishlistItem(13, "Anuradhapura", "An ancient capital of stupas, monasteries, and sacred sites.", "North Central Province", R.drawable.anuradhapura),
        WishlistItem(14, "Polonnaruwa", "Remarkable ruins and monuments from Sri Lanka's medieval capital.", "North Central Province", R.drawable.polonnaruwa),
        WishlistItem(15, "Galle", "A vibrant southern city where heritage meets the Indian Ocean.", "Southern Province", R.drawable.galle),
        WishlistItem(16, "Jaffna", "Northern culture, island landscapes, and distinctive historic landmarks.", "Northern Province", R.drawable.anuradhapura),
        WishlistItem(17, "Trincomalee", "A natural harbour with temples and clear east-coast beaches.", "Eastern Province", R.drawable.unawatuna),
        WishlistItem(18, "Yala", "Sri Lanka's best-known national park for leopards and wildlife.", "Southern Province", R.drawable.hiking),
        WishlistItem(19, "Udawalawe", "An open-country national park famous for wild elephants.", "Sabaragamuwa Province", R.drawable.hiking),
        WishlistItem(20, "Hikkaduwa", "A lively beach town known for coral, surfing, and sunsets.", "Southern Province", R.drawable.bentota),
        WishlistItem(21, "Negombo", "A lagoon-side coastal city with beaches and a historic fishing culture.", "Western Province", R.drawable.colombo),
    ) + DestinationDetailsCatalog.attractionWishlistItems
}
