package com.mushscope.data.pref

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreference private constructor(private val dataStore: DataStore<Preferences>) {
    suspend fun saveSession(user: UserModel) {
        dataStore.edit { preferences ->
            preferences[EMAIL_KEY] = user.email
            preferences[TOKEN_KEY] = user.token
            preferences[IS_LOGIN_KEY] = true
            preferences[NAME_KEY] = user.name ?: ""
            preferences[PHOTO_URL_KEY] = user.photoUrl ?: ""
        }
    }

    fun getSession(): Flow<UserModel> = dataStore.data
        .map { preferences ->
            UserModel(
                email = preferences[EMAIL_KEY] ?: "",
                token = preferences[TOKEN_KEY] ?: "",
                isLogin = preferences[IS_LOGIN_KEY] ?: false,
                name = preferences[NAME_KEY] ?: "",
                photoUrl = preferences[PHOTO_URL_KEY] ?: ""
            )
        }

    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserPreference? = null

        private val EMAIL_KEY = stringPreferencesKey("email")
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val IS_LOGIN_KEY = booleanPreferencesKey("isLogin")
        private val NAME_KEY = stringPreferencesKey("name")
        private val PHOTO_URL_KEY = stringPreferencesKey("photoUrl")

        fun getInstance(dataStore: DataStore<Preferences>): UserPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = UserPreference(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}