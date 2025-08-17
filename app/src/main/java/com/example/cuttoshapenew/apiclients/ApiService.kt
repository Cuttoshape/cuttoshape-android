package com.example.cuttoshapenew.apiclients

import com.example.cuttoshapenew.model.ProductResponse
import com.example.cuttoshapenew.model.Product
import com.example.cuttoshapenew.utils.DataStoreManager
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.MultipartBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import kotlinx.serialization.Serializable
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.GET
import retrofit2.http.DELETE
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

// Data class for the login request body
data class LoginRequest(
    val email: String,
    val password: String
)

// Data classes for the login response
data class LoginResponse(
    val token: String,
    val user: User
)

data class Business(
    val id: Int,
    val code: String,
    val name: String,
    val mobile: String,
    val logo: String?,
    val ownerId: Int,
    val status: String,
    val email: String,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val createdAt: String,
    val createdBy: String?,
    val updatedAt: String,
    val updatedBy: String?,
    val deletedAt: String?,
    val deletedBy: String?
)

data class User(
    val id: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val password: String? = null,
    val userType: String,
    val photoUrl: String?,
    val address: String?,
    val phone: String?,
    val city: String?,
    val state: String?,
    val zipCode: String?,
    val country: String?,
    val createdAt: String,
    val createdBy: String?,
    val updatedAt: String,
    val updatedBy: String?,
    val deletedAt: String?,
    val deletedBy: String?,
    val business: Business?,
    val bodyData: List<Any>,
    val shippingAddresses: List<Any>,
    val cartItems: List<CartItemResponse>
)

// Data class for the request body
data class ProductRequest(
    val filter: List<Any> = emptyList(),
    val page: Int = 1,
    val limit: Int = 10
)

// Data class for the sign-up request body
data class SignUpRequest(
    val fullname: String,
    val email: String,
    val password: String,
    val userType: String,
    val invitationCode: String
)

// Data class for the sign-up response
data class SignUpResponse(
    val message: String,
    val user: SignUpUser
)

data class SignUpUser(
    val createdAt: String,
    val updatedAt: String,
    val id: Int,
    val password: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val userType: String
)

data class UpdateProfileRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val address: String?,
    val city: String?,
    val state: String?,
    val zipCode: String?,
    val country: String?
)

data class UpdateProfileResponse(
    val id: Int,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val userType: String,
    val photoUrl: String?,
    val address: String?,
    val phone: String?,
    val city: String?,
    val state: String?,
    val zipCode: String?,
    val country: String?,
    val createdAt: String,
    val createdBy: String?,
    val updatedAt: String,
    val updatedBy: String?,
    val deletedAt: String?,
    val deletedBy: String?
)

data class CreateBusinessRequest(
    val name: String,
    val email: String,
    val address: String,
    val city: String,
    val state: String,
    val country: String,
    val zipCode: String,
    val mobile: String,
    val avatar: String?,
    val ownerId: Int,
    val status: String,
    val code: String
)

data class CreateBusinessResponse(
    val id: Int,
    val name: String,
    val email: String,
    val address: String,
    val city: String,
    val state: String,
    val country: String,
    val zipCode: String,
    val mobile: String,
    val avatar: String?,
    val ownerId: Int,
    val status: String,
    val code: String,
    val createdAt: String,
    val updatedAt: String
)

data class ProductOption(val name: String, val value: String, val cost: String)
data class NewProductRequest(
    val product: Product,
    val images: List<String> // Assuming images are URI strings for now
)
data class Product(
    val name: String,
    val businessId: Int?, // Placeholder, adjust as needed
    val productGenderType: String,
    val options: List<ProductOption>,
    val highPrice: String,
    val lowPrice: String,
    val rating: Int = 0, // Default value
    val createdBy: Int? // Placeholder, adjust as needed
)

data class NewProductResponse(
    val createdAt: String,
    val updatedAt: String,
    val code: String,
    val id: Int,
    val name: String,
    val businessId: Int,
    val productGenderType: String,
    val highPrice: String,
    val lowPrice: String,
    val createdBy: Int
)

data class CartItemRequest(
    val configurations: Map<String, String>,
    val color: String,
    val design: String,
    val fabric: String,
    val cost: Int,
    val productId: Int,
    val userDataId: String,
    val userId: Int?
)

data class UserData(
    val id: Int,
    val userId: Int,
    val name: String,
    val age: Int,
    val weight: Int,
    val height: Int,
    val genderType: String,
    val data: MeasurementData,
    val createdAt: String? = null,
    val createdBy: Int? = null,
    val updatedAt: String? = null,
    val updatedBy: Int? = null,
    val deletedAt: String? = null,
    val deletedBy: Int? = null
)

@Serializable
data class MeasurementData(
    val head: String,
    val chest: String,
    val waist: String,
    val height: String,
    val sholder: String,
    val armLength: String,
    val legLength: String? = null
)
data class Configuration(
    val color: String,
    val design: String,
    val fabric: String,
)

data class CartItemResponse(
    val configurations: Configuration,
    val color: String,
    val design: String,
    val fabric: String,
    val cost: Int,
    val code: Int,
    val createdAt: String,
    val createdBy: Int,
    val deletedAt: String,
    val deletedBy: Int,
    val id: Int,
    val orderId: Int,
    val product: Product?,
    val productId: Int,
    val updatedAt: String,
    val updatedBy: Int,
    val user: User?,
    val userData: UserData?,
    val userDataId: Int,
    val userId: Int,
)

data class CartResponse(
    val success: String,
    val data: List<CartItemResponse>?
)

data class AddCartResponse(
    val success: String,
    val data: CartItemResponse?
)

data class CartDeleteResponse(
    val success: String,
    val message: String
)

data class Order(
    val id: Int,
    val orderNumber: String,
    val createdAt: String,
    val buyerName: String,
    val itemCount: Int,
    val status: String,
    val quotations: List<Quotation>,
    val shippingAddress: ShippingAddress1
)

data class OrderFilterRequest(
    val isListing: Boolean,
    val isDetail: Boolean,
    val limit: Int,
    val orderFilter: List<Filter>,
    val page: Int,
    val quotationFilter: List<Any>
)

data class Filter(
    val field: String,
    val value: Int?,
    val condition: String
)

data class OrderFilterResponse(
    val totalItems: Int,
    val totalPages: Int,
    val currentPage: Int,
    val rows: List<Order>
)

data class OrderResponse(
    val totalItems: Int,
    val totalPages: Int,
    val currentPage: Int,
    val newRows: List<Order>
)

data class Quotation(
    val id: Int,
    val itemId: Int,
    val productImage: String,
    val productName: String,
    val sellerId: Int,
    val cartItemId: Int,
    val cost: Int,
    val status: String,
    val configurations: Map<String, String>,
    val userData: UserData,
    val createdAt: String,
    val updatedAt: String,
    val shippingDetails: ShippingAddress?,
    val shippingCost: Int
)

data class MeasurementResponse(
    val id: Any,
    val userId: Any,
    val name: String,
    val age: Int,
    val weight: Int,
    val height: String,
    val data: Data,
    val genderType: String,
    val createdAt: String,
    val createdBy: String?,
    val updatedAt: String,
    val updatedBy: String?,
    val deletedAt: String?,
    val deletedBy: String?
)

data class MeasurementRequest(
    val userId: Any,
    val name: String,
    val age: Int,
    val weight: Int,
    val height: String,
    val data: Data,
    val genderType: String,
)

data class Data (
    val Head: String,
    val Chest: String,
    val Waist: String,
    val Height: String,
    val Sholder: String,
    @SerializedName("Arm length")
    val armLength: String,
    @SerializedName("Leg length")
    val legLength: String
)

data class Measurement(
    val id: String,
    val userId: String,
    val value: String,
    val date: String
)

data class ShippingAddress(
    val id : String,
    val userId : String,
    val address : String,
    val city : String,
    val state : String,
    val zipCode : String,
    val country : String,
    val createdAt : String,
    val createdBy : String,
    val updatedAt : String,
    val updatedBy : String,
    val deletedAt : String,
    val deletedBy : String
)

data class ShippingAddress1(
    val id : String,
    val userId : String,
    val addressLine1 : String,
    val city : String,
    val state : String,
    val zipCode : String,
    val country : String,
    val createdAt : String,
    val createdBy : String,
    val updatedAt : String,
    val updatedBy : String,
    val deletedAt : String,
    val deletedBy : String
)

data class ShipAddressRequest(
    val address : String,
    val city : String,
    val country : String,
    val state : String,
    val userId : String,
    val zipCode : String,
)

data class MessageResponse(

    val id: String,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val userType: String,
    val photoUrl: String,
    val address: String,
    val phone: String,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val createdAt: String,
    val createdBy: String,
    val updatedAt: String,
    val updatedBy: String,
    val deletedAt: String,
    val deletedBy: String,
    val bodyData: List<MeasurementResponse>
)

data class ChatContent(
    val content : String,
    val productId: String
)

data class ChatDetails(
    val id : String,
    val senderId : String,
    val receiverId : String,
    val content: ChatContent,
    val type: String,
    val status: String,
    val createdAt: String,
    val createdBy: String,
    val updatedAt: String,
    val updatedBy: String,
    val deletedAt: String,
    val deletedBy: String
)

data class ChatResponse (
    val page : Int,
    val size : Int,
    val total : Int,
    val hasMore : Boolean,
    val rows: List<ChatDetails>
)

data class PaymentIntentRequest(
    val cartItems: List<CartItemResponse>,
    val quotationItems: List<Quotation>? = null,
    val userId: String,
    val shipping_options: String
)

data class PaymentIntentResponse(
    val paymentIntent: String,
    val ephemeralKey: String,
    val customer: String,
    val publishableKey: String
)

interface ApiService {
    @POST("product/all")
    suspend fun getProducts(@Body request: ProductRequest): ProductResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): SignUpResponse

    @PUT("user/{userId}")
    suspend fun updateProfile(
        @Path("userId") userId: Int,
        @Body request: UpdateProfileRequest
    ): UpdateProfileResponse

    @POST("business")
    suspend fun createBusiness(@Body request: CreateBusinessRequest): CreateBusinessResponse

    @POST("product")
    @Multipart
    suspend fun createProduct(
        @Part("product") productJson: RequestBody,
        @Part images: List<MultipartBody.Part>?
    ): NewProductResponse

    @POST("cart-item/{productId}")
    suspend fun addToCart(
        @Path("productId") productId: Int?,
        @Body request: CartItemRequest
    ): AddCartResponse

    @GET("product/{productId}")
    suspend fun getProductById(@Path("productId") productId: Int): Product

    @GET("cart-item/{userId}")
    suspend fun getCartItem(@Path("userId") userId: Int?): CartResponse

    @DELETE("cart-item/{id}")
    suspend fun deleteCartItem(@Path("id") id: Int): CartDeleteResponse

    @POST("order/filter")
    suspend fun getOrders(@Body request: OrderFilterRequest): OrderFilterResponse

    @POST("order/filter")
    suspend fun getOrderDetails(@Body request: OrderFilterRequest): OrderResponse

    @GET("body-data/userId/{userId}")
    suspend fun getMeasurements(@Path("userId") userId: String): List<MeasurementResponse>

    @POST("body-data")
    suspend fun addMeasurement(@Body measurement: MeasurementRequest): MeasurementResponse

    @PUT("measurements/{id}")
    suspend fun updateMeasurement(@Body measurement: MeasurementResponse): MeasurementResponse

    @DELETE("measurements/{id}")
    suspend fun deleteMeasurement(@Path("id") id: String)

    @GET("shipping-address/by-user-id/{id}")
    suspend fun getShippingAddress(@Path("id") id: String): List<ShippingAddress>

    @POST("shipping-address")
    suspend fun createShipAddress(@Body shipAddress: ShipAddressRequest): ShippingAddress

    @GET("connection")
    suspend fun getMessages(@Query("userId") id: String): List<MessageResponse>

    @POST("payment-sheet-intent")
    suspend fun getPaymentIntent(@Body paymentIntent: PaymentIntentRequest): PaymentIntentResponse

    @POST("post-payment-success")
    suspend fun createPaymentSuccess(@Body paymentIntent: PaymentIntentRequest): PaymentIntentResponse

    @GET("message/by-user-id")
    suspend fun getUserMessageChat(@Query("senderId") senderId: String, @Query("receiverId") receiverId: String, @Query("page") page: String, @Query("size") size: String ): ChatResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://backend.cuttoshape.com/" // Matches your endpoint

    fun getClient(context: android.content.Context): ApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val token = runBlocking { DataStoreManager.getToken(context).first() }
                val request = if (token != null) {
                    chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                } else {
                    chain.request()
                }
                chain.proceed(request)
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        return retrofit.create(ApiService::class.java)
    }
}