package com.ekatayan.app.data.model

import androidx.annotation.StringRes
import com.ekatayan.app.R
import java.time.LocalDate
import java.time.LocalTime

/** Optional structured service information; only fields for the selected category are saved. */
enum class ListingField(@get:StringRes val label: Int, val kind: FieldKind = FieldKind.TEXT) {
    ROOM_TYPE(R.string.bp_room_type), GUESTS(R.string.bp_guests, FieldKind.COUNT),
    BEDS(R.string.bp_beds, FieldKind.COUNT), UNITS(R.string.bp_units, FieldKind.QUANTITY),
    CHECK_IN(R.string.bp_check_in, FieldKind.TIME), CHECK_OUT(R.string.bp_check_out, FieldKind.TIME),
    AMENITIES(R.string.bp_amenities), CANCELLATION(R.string.bp_cancellation),
    CUISINE(R.string.bp_cuisine), OPENING_HOURS(R.string.bp_opening_hours),
    PRICE_RANGE(R.string.bp_price_range), MENU(R.string.bp_menu),
    RESERVATIONS(R.string.bp_reservations, FieldKind.BOOLEAN), SEATS(R.string.bp_seats, FieldKind.QUANTITY),
    VEHICLE_TYPE(R.string.bp_vehicle_type), PASSENGERS(R.string.bp_passengers, FieldKind.COUNT),
    DRIVER(R.string.bp_driver, FieldKind.BOOLEAN), AIR_CONDITIONING(R.string.bp_air_conditioning, FieldKind.BOOLEAN),
    PICKUP(R.string.bp_pickup), DURATION(R.string.bp_duration), GROUP_SIZE(R.string.bp_group_size, FieldKind.COUNT),
    INCLUDED(R.string.bp_included), EXCLUDED(R.string.bp_excluded), MEETING_POINT(R.string.bp_meeting_point),
    PROPERTY_TYPE(R.string.bp_property_type), BEDROOMS(R.string.bp_bedrooms, FieldKind.QUANTITY),
    BATHROOMS(R.string.bp_bathrooms, FieldKind.QUANTITY), ITEM_CATEGORY(R.string.bp_item_category),
    QUANTITY(R.string.bp_quantity, FieldKind.QUANTITY), DEPOSIT(R.string.bp_deposit, FieldKind.MONEY),
    RENTAL_CONDITIONS(R.string.bp_rental_conditions);

    fun isValid(value: String): Boolean = value.isBlank() || when (kind) {
        FieldKind.TEXT -> true
        FieldKind.COUNT -> value.toIntOrNull()?.let { it > 0 } == true
        FieldKind.QUANTITY -> value.toIntOrNull()?.let { it >= 0 } == true
        FieldKind.MONEY -> value.toDoubleOrNull()?.let { it.isFinite() && it >= 0 } == true
        FieldKind.TIME -> validTime(value)
        FieldKind.BOOLEAN -> value == "true" || value == "false"
    }
}

enum class FieldKind { TEXT, COUNT, QUANTITY, MONEY, TIME, BOOLEAN }

val ListingCategory.fields: List<ListingField>
    get() = when (this) {
        ListingCategory.ROOMS -> listOf(ListingField.ROOM_TYPE, ListingField.GUESTS, ListingField.BEDS,
            ListingField.UNITS, ListingField.CHECK_IN, ListingField.CHECK_OUT, ListingField.AMENITIES, ListingField.CANCELLATION)
        ListingCategory.DINING -> listOf(ListingField.CUISINE, ListingField.OPENING_HOURS, ListingField.PRICE_RANGE,
            ListingField.MENU, ListingField.RESERVATIONS, ListingField.SEATS)
        ListingCategory.TRANSPORT -> listOf(ListingField.VEHICLE_TYPE, ListingField.PASSENGERS, ListingField.DRIVER,
            ListingField.AIR_CONDITIONING, ListingField.PICKUP)
        ListingCategory.ACTIVITIES -> listOf(ListingField.DURATION, ListingField.GROUP_SIZE, ListingField.INCLUDED,
            ListingField.EXCLUDED, ListingField.MEETING_POINT)
        ListingCategory.VACATION -> listOf(ListingField.PROPERTY_TYPE, ListingField.GUESTS, ListingField.BEDROOMS,
            ListingField.BATHROOMS, ListingField.UNITS, ListingField.AMENITIES, ListingField.CHECK_IN, ListingField.CHECK_OUT)
        ListingCategory.RENTALS -> listOf(ListingField.ITEM_CATEGORY, ListingField.QUANTITY, ListingField.DEPOSIT,
            ListingField.PICKUP, ListingField.RENTAL_CONDITIONS)
        ListingCategory.OTHER -> emptyList()
    }

fun BusinessType?.listingCategory(): ListingCategory = when (this) {
    BusinessType.TOURS -> ListingCategory.ACTIVITIES
    BusinessType.TRANSPORT -> ListingCategory.TRANSPORT
    BusinessType.GEAR, BusinessType.EQUIPMENT -> ListingCategory.RENTALS
    BusinessType.RESTAURANT -> ListingCategory.DINING
    BusinessType.VACATION -> ListingCategory.VACATION
    BusinessType.OTHER -> ListingCategory.OTHER
    else -> ListingCategory.ROOMS
}

/** One optional inclusive date range and optional daily time slots, without a booking engine. */
data class ListingAvailability(
    val available: Boolean = true,
    val from: String = "",
    val until: String = "",
    val timeSlots: String = "",
) {
    fun isValid(): Boolean {
        val datesValid = if (from.isBlank() && until.isBlank()) true else runCatching {
            val start = LocalDate.parse(from)
            val end = LocalDate.parse(until)
            !end.isBefore(start)
        }.getOrDefault(false)
        return datesValid && (timeSlots.isBlank() || timeSlots.split(',').all { validTime(it.trim()) })
    }
}

private fun validTime(value: String): Boolean = Regex("\\d{2}:\\d{2}").matches(value) &&
    runCatching { LocalTime.parse(value) }.isSuccess
