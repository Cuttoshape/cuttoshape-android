package com.example.cuttoshapenew.model

import java.time.ZonedDateTime

data class ProductResponse(
    val totalItems: Int,
    val totalPages: Int,
    val currentPage: Int,
    val products: List<Product>
)

data class Product(
    val id: Int,
    val businessId: Int,
    val code: String,
    val name: String,
    val lowPrice: Double,
    val highPrice: Double,
    val description: String,
    val productGenderType: String,
    val ratings: Double?,
    val createdAt: String,
    val createdBy: Int?,
    val updatedAt: String,
    val updatedBy: Int?,
    val deletedAt: ZonedDateTime?,
    val deletedBy: Int?,
    val business: Business,
    val documents: List<Document>,
    val options: List<Option>
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
    val createdBy: Int?,
    val updatedAt: String,
    val updatedBy: Int?,
    val deletedAt: ZonedDateTime?,
    val deletedBy: Int?,
    val owner: Owner
)

data class Owner(
    val id: Int,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val userType: String,
    val photoUrl: String?,
    val address: String,
    val phone: String?,
    val city: String,
    val state: String,
    val zipCode: String,
    val country: String,
    val createdAt: String,
    val createdBy: Int?,
    val updatedAt: String,
    val updatedBy: Int?,
    val deletedAt: ZonedDateTime?,
    val deletedBy: Int?
)

data class Document(
    val id: Int,
    val entityId: Int,
    val entityType: String,
    val url: String,
    val createdAt: String,
    val createdBy: Int?,
    val updatedAt: String,
    val updatedBy: Int?,
    val deletedAt: ZonedDateTime?,
    val deletedBy: Int?
)

data class Option(
    val id: Int,
    val productId: Int,
    val name: String,
    val value: String,
    val cost: String,
    val createdAt: String,
    val createdBy: Int?,
    val updatedAt: String,
    val updatedBy: Int?,
    val deletedAt: ZonedDateTime?,
    val deletedBy: Int?,
    val quotationId: Int?
)