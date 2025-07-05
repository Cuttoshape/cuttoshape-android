package com.example.cuttoshapenew.apiclients

import com.example.cuttoshapenew.model.ProductResponse
import com.example.cuttoshapenew.model.Product
import com.example.cuttoshapenew.utils.DataStoreManager
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
import retrofit2.http.Multipart
import retrofit2.http.PUT
import retrofit2.http.GET
import retrofit2.http.Part
import retrofit2.http.Path

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
    val cartItems: List<Any>
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
    val businessId: Int, // Placeholder, adjust as needed
    val productGenderType: String,
    val options: List<ProductOption>,
    val highPrice: String,
    val lowPrice: String,
    val rating: Int = 0, // Default value
    val createdBy: Int // Placeholder, adjust as needed
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

    @GET("product/{productId}")
    suspend fun getProductById(@Path("productId") productId: Int): Product
}

object RetrofitClient {
    private const val BASE_URL = "https://backend.cuttoshape.com/" // Replace with your API base URL

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