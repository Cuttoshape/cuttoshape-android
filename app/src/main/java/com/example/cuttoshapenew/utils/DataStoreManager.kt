package com.example.cuttoshapenew.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.example.cuttoshapenew.apiclients.Business
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

object DataStoreManager {
    private val TOKEN_KEY = stringPreferencesKey("token")
    private val ROLE_KEY = stringPreferencesKey("role")
    private val USER_ID_KEY = stringPreferencesKey("user_id")
    private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
    private val USER_FIRST_NAME_KEY = stringPreferencesKey("user_first_name")
    private val USER_LAST_NAME_KEY = stringPreferencesKey("user_last_name")
    private val USER_ADDRESS_KEY = stringPreferencesKey("user_address")
    private val USER_CITY_KEY = stringPreferencesKey("user_city")
    private val USER_STATE_KEY = stringPreferencesKey("user_state")
    private val USER_ZIPCODE_KEY = stringPreferencesKey("user_zipcode")
    private val USER_COUNTRY_KEY = stringPreferencesKey("user_country")
    private val USER_BUSINESS_KEY = stringPreferencesKey("user_business")

    private val gson = Gson()
    suspend fun saveAuthData(context: Context, token: String, role: String, user: com.example.cuttoshapenew.apiclients.User) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[ROLE_KEY] = role
            prefs[USER_ID_KEY] = user.id.toString()
            prefs[USER_EMAIL_KEY] = user.email
            prefs[USER_FIRST_NAME_KEY] = user.firstName
            prefs[USER_LAST_NAME_KEY] = user.lastName
            prefs[USER_ADDRESS_KEY] = user.address?: ""
            prefs[USER_CITY_KEY] = user.city?: ""
            prefs[USER_STATE_KEY] = user.state?: ""
            prefs[USER_ZIPCODE_KEY] = user.zipCode?: ""
            prefs[USER_COUNTRY_KEY] = user.country?: ""
            prefs[USER_BUSINESS_KEY] = if (user.business != null) gson.toJson(user.business) else ""

        }
    }

    fun getToken(context: Context): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[TOKEN_KEY]
        }
    }


    fun getRole(context: Context): String? = runBlocking {
        context.dataStore.data.first()[ROLE_KEY].toString()
    }

    fun getUserEmail(context: Context): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[USER_EMAIL_KEY]
        }
    }

    fun getUserFirstName(context: Context): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[USER_FIRST_NAME_KEY]
        }
    }


    fun getUserLastName(context: Context): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[USER_LAST_NAME_KEY]
        }
    }

    fun getUserId(context: Context): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[USER_ID_KEY]
        }
    }

    fun getUserAddress(context: Context): Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USER_ADDRESS_KEY]
    }

    fun getUserCity(context: Context): Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USER_CITY_KEY]
    }

    fun getUserState(context: Context): Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USER_STATE_KEY]
    }

    fun getUserZipCode(context: Context): Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USER_ZIPCODE_KEY]
    }

    fun getUserCountry(context: Context): Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USER_COUNTRY_KEY]
    }

    fun getUserBusiness(context: Context): Flow<Business?> = context.dataStore.data.map { prefs ->
        val businessJson = prefs[USER_BUSINESS_KEY]
        if (!businessJson.isNullOrEmpty()) {
            gson.fromJson(businessJson, Business::class.java)

        } else {
            null
        }
    }

    suspend fun clearAuthData(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}