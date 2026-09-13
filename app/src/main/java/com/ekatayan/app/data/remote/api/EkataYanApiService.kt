package com.ekatayan.app.data.remote.api

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import retrofit2.http.*

data class ApiEnvelope<T>(val success: Boolean, val data: T? = null, val error: ApiErrorDto? = null)
data class ApiErrorDto(val code: String, val message: String)
data class TripDto(val id: String, val name: String, val destinations: List<String>, @SerializedName("start_date") val startDate: String, @SerializedName("end_date") val endDate: String, val budget: String, val currency: String = "LKR", val travelers: Int = 1, val interests: List<String> = emptyList(), @SerializedName("preferred_activities") val preferredActivities: List<String> = emptyList(), @SerializedName("travel_style") val travelStyle: String = "balanced", @SerializedName("accommodation_preference") val accommodationPreference: String = "any", @SerializedName("transportation_preference") val transportationPreference: String = "any", @SerializedName("additional_requirements") val additionalRequirements: String = "", @SerializedName("created_by") val createdBy: String, val source: String = "manual", @SerializedName("planner_context") val plannerContext: PlannerPreviewRequest? = null)
data class TripRequest(val name: String, val destinations: List<String>, @SerializedName("start_date") val startDate: String, @SerializedName("end_date") val endDate: String, val budget: String, val currency: String = "LKR", val travelers: Int = 1, val interests: List<String> = emptyList(), @SerializedName("preferred_activities") val preferredActivities: List<String> = emptyList(), @SerializedName("travel_style") val travelStyle: String = "balanced", @SerializedName("accommodation_preference") val accommodationPreference: String = "any", @SerializedName("transportation_preference") val transportationPreference: String = "any", @SerializedName("additional_requirements") val additionalRequirements: String = "")
data class MemberDto(@SerializedName("trip_id") val tripId: String, @SerializedName("user_id") val userId: String, val role: String)
data class MessageDto(val id: String, @SerializedName("trip_id") val tripId: String, @SerializedName("sender_id") val senderId: String, val content: String, @SerializedName("created_at") val createdAt: String)
data class MessageRequest(val content: String)
data class NotificationDto(val id: String, @SerializedName("trip_id") val tripId: String?, val type: String, val title: String, val body: String, @SerializedName("read_at") val readAt: String?, @SerializedName("created_at") val createdAt: String)
data class ExpenseParticipantDto(@SerializedName("user_id") val userId: String, val share: String)
data class ExpenseDto(val id: String, @SerializedName("trip_id") val tripId: String, val title: String, val amount: String, val currency: String, val category: String, @SerializedName("paid_by") val paidBy: String, @SerializedName("split_type") val splitType: String, @SerializedName("incurred_at") val incurredAt: String, @SerializedName("expense_participants") val participants: List<ExpenseParticipantDto> = emptyList())
data class ExpenseRequest(val title: String, val amount: String, val currency: String = "LKR", val category: String, @SerializedName("paid_by") val paidBy: String, @SerializedName("split_type") val splitType: String, val participants: List<ExpenseParticipantDto>, @SerializedName("incurred_at") val incurredAt: String)
data class WeatherDto(val location: String, val date: String, val condition: String, val icon: String?, @SerializedName("min_celsius") val minCelsius: Double, @SerializedName("max_celsius") val maxCelsius: Double, @SerializedName("rain_chance") val rainChance: Int?, @SerializedName("uv_index") val uvIndex: Double?, val humidity: Int?, @SerializedName("wind_kph") val windKph: Double?, val sunrise: String?)
data class GenerateItineraryRequest(@SerializedName("trip_id") val tripId: String, val name: String, val destinations: List<String>, @SerializedName("start_date") val startDate: String, @SerializedName("end_date") val endDate: String, val budget: String, val currency: String = "LKR", val travelers: Int, val interests: List<String>, @SerializedName("preferred_activities") val preferredActivities: List<String>, @SerializedName("travel_style") val travelStyle: String, @SerializedName("accommodation_preference") val accommodationPreference: String, @SerializedName("transportation_preference") val transportationPreference: String, @SerializedName("additional_requirements") val additionalRequirements: String = "")
data class PlannerDestinationDto(val name: String, @SerializedName("place_id") val placeId: String? = null, val latitude: Double? = null, val longitude: Double? = null)
data class PlannerPreviewRequest(val destinations: List<PlannerDestinationDto>, @SerializedName("traveller_type") val travellerType: String, @SerializedName("traveller_count") val travellerCount: Int, @SerializedName("start_date") val startDate: String, @SerializedName("end_date") val endDate: String, @SerializedName("transport_preferences") val transportPreferences: List<String> = emptyList(), @SerializedName("accommodation_preference") val accommodationPreference: String? = null, @SerializedName("travel_style") val travelStyle: String? = null, val interests: List<String> = emptyList(), @SerializedName("travel_pace") val travelPace: String? = null, @SerializedName("special_requests") val specialRequests: String? = null, @SerializedName("allow_ai_destination_suggestions") val allowAiDestinationSuggestions: Boolean = false, @SerializedName("suggest_additional_places") val suggestAdditionalPlaces: Boolean = false)
data class ItineraryLocationDto(val name: String, val latitude: Double? = null, val longitude: Double? = null)
data class AiItineraryActivityDto(val id: String, val name: String, val category: String, val location: ItineraryLocationDto, @SerializedName("start_time") val startTime: String, @SerializedName("end_time") val endTime: String, @SerializedName("duration_minutes") val durationMinutes: Int, val description: String, @SerializedName("estimated_cost_lkr") val estimatedCostLkr: Long, @SerializedName("transport_from_previous") val transportFromPrevious: String = "", @SerializedName("travel_time_minutes") val travelTimeMinutes: Int = 0)
data class CostRangeDto(val min: Long, val max: Long)
data class AiItineraryDayDto(@SerializedName("day_number") val dayNumber: Int, val date: String, val destination: String, val title: String, val summary: String, val activities: List<AiItineraryActivityDto>, @SerializedName("day_estimated_cost_lkr") val dayEstimatedCostLkr: CostRangeDto)
data class TripSummaryDto(val title: String, val summary: String, val route: List<String>, @SerializedName("start_date") val startDate: String, @SerializedName("end_date") val endDate: String, @SerializedName("duration_days") val durationDays: Int, @SerializedName("traveller_type") val travellerType: String, @SerializedName("traveller_count") val travellerCount: Int, @SerializedName("travel_style") val travelStyle: String, @SerializedName("travel_pace") val travelPace: String)
data class CostEstimateDto(val currency: String, val accommodation: CostRangeDto, val transport: CostRangeDto, val food: CostRangeDto, val activities: CostRangeDto, val total: CostRangeDto, val disclaimer: String)
data class AiItineraryResponseDto(val trip: TripSummaryDto, val days: List<AiItineraryDayDto>, @SerializedName("cost_estimate") val costEstimate: CostEstimateDto, val recommendations: List<String> = emptyList())
data class PlannedItinerarySaveRequest(val planner: PlannerPreviewRequest, val itinerary: AiItineraryResponseDto)
data class ModifyItineraryRequest(@SerializedName("trip_context") val tripContext: PlannerPreviewRequest, @SerializedName("current_itinerary") val currentItinerary: AiItineraryResponseDto, val instruction: String, @SerializedName("target_day") val targetDay: Int? = null)
data class PlannedItinerarySaveDto(val trip: TripDto, @SerializedName("structured_itinerary") val structuredItinerary: AiItineraryResponseDto)
data class TripDetailsDto(val trip: TripDto, @SerializedName("structured_itinerary") val structuredItinerary: AiItineraryResponseDto? = null)
data class ItineraryActivityDto(val title: String, val location: String, @SerializedName("suggested_time") val suggestedTime: String, val description: String, @SerializedName("estimated_cost") val estimatedCost: String, val transport: String, val notes: String = "", val category: String? = null)
data class ItineraryDayDto(@SerializedName("day_number") val dayNumber: Int, @SerializedName(value="trip_date", alternate=["date"]) val date: String, val locations: List<String>, @SerializedName(value="itinerary_activities", alternate=["activities"]) val activities: List<ItineraryActivityDto> = emptyList(), val notes: String = "")
data class ItineraryDto(val id: String = "", @SerializedName("trip_id") val tripId: String = "", val overview: String, val currency: String, val recommendations: List<String> = emptyList(), @SerializedName(value="itinerary_days", alternate=["days"]) val days: List<ItineraryDayDto> = emptyList())

interface EkataYanApiService {
    @GET("api/trips") suspend fun trips(): ApiEnvelope<List<TripDto>>
    @POST("api/trips") suspend fun createTrip(@Body body: TripRequest): ApiEnvelope<TripDto>
    @GET("api/trips/{id}") suspend fun trip(@Path("id") id: String): ApiEnvelope<TripDto>
    @GET("api/trips/{id}/details") suspend fun tripDetails(@Path("id") id: String): ApiEnvelope<TripDetailsDto>
    @PUT("api/trips/{id}") suspend fun updateTrip(@Path("id") id: String, @Body body: TripRequest): ApiEnvelope<TripDto>
    @DELETE("api/trips/{id}") suspend fun deleteTrip(@Path("id") id: String): ApiEnvelope<Map<String, Boolean>>
    @GET("api/trips/{id}/members") suspend fun members(@Path("id") id: String): ApiEnvelope<List<MemberDto>>
    @GET("api/trips/{id}/messages") suspend fun messages(@Path("id") id: String): ApiEnvelope<List<MessageDto>>
    @POST("api/trips/{id}/messages") suspend fun sendMessage(@Path("id") id: String, @Body body: MessageRequest): ApiEnvelope<MessageDto>
    @GET("api/trips/{id}/expenses") suspend fun expenses(@Path("id") id: String): ApiEnvelope<List<ExpenseDto>>
    @POST("api/trips/{id}/expenses") suspend fun createExpense(@Path("id") id: String, @Body body: ExpenseRequest): ApiEnvelope<ExpenseDto>
    @PUT("api/expenses/{id}") suspend fun updateExpense(@Path("id") id: String, @Body body: ExpenseRequest): ApiEnvelope<ExpenseDto>
    @DELETE("api/expenses/{id}") suspend fun deleteExpense(@Path("id") id: String): ApiEnvelope<Map<String, Boolean>>
    @POST("api/itineraries/generate") suspend fun generateItinerary(@Body body: GenerateItineraryRequest): ApiEnvelope<ItineraryDto>
    @POST("api/itineraries/preview") suspend fun previewItinerary(@Body body: PlannerPreviewRequest): ApiEnvelope<AiItineraryResponseDto>
    @POST("api/itineraries/modify") suspend fun modifyItinerary(@Body body: ModifyItineraryRequest): ApiEnvelope<AiItineraryResponseDto>
    @POST("api/itineraries/save") suspend fun savePlannedItinerary(@Body body: PlannedItinerarySaveRequest): ApiEnvelope<PlannedItinerarySaveDto>
    @GET("api/trips/{id}/itineraries") suspend fun itineraries(@Path("id") id: String): ApiEnvelope<List<ItineraryDto>>
    @GET("api/notifications") suspend fun notifications(): ApiEnvelope<List<NotificationDto>>
    @PUT("api/notifications/{id}/read") suspend fun markNotificationRead(@Path("id") id: String): ApiEnvelope<NotificationDto>
    @GET("api/weather") suspend fun weather(@Query("location") location: String, @Query("date") date: String): ApiEnvelope<WeatherDto>
    @Multipart @POST("api/storage/profile-picture") suspend fun uploadProfilePicture(@Part file: MultipartBody.Part): ApiEnvelope<JsonObject>
    @Multipart @POST("api/trips/{id}/images") suspend fun uploadTripImage(@Path("id") id: String, @Part file: MultipartBody.Part): ApiEnvelope<JsonObject>
}
