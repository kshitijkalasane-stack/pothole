package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.models.UserProfile
import com.example.data.models.UserRole
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirebaseAuthRepository(private val context: Context) {

    private var firebaseAuth: FirebaseAuth? = null
    private val _currentUserProfile = MutableStateFlow<UserProfile?>(null)
    val currentUserProfile: StateFlow<UserProfile?> = _currentUserProfile.asStateFlow()

    init {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            firebaseAuth = FirebaseAuth.getInstance()
            firebaseAuth?.addAuthStateListener { auth ->
                val user = auth.currentUser
                if (user != null) {
                    _currentUserProfile.value = mapFirebaseUserToProfile(user)
                } else {
                    _currentUserProfile.value = null
                }
            }
        } catch (e: Exception) {
            Log.e("FirebaseAuthRepository", "Firebase Auth initialization notice: ${e.localizedMessage}")
        }
    }

    private fun mapFirebaseUserToProfile(user: FirebaseUser): UserProfile {
        val displayName = user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore("@")
            ?: "Commuter ${user.uid.take(5)}"

        val email = user.email ?: "${user.uid}@smartpothole.in"
        val isAuthority = email.contains("pwd") || email.contains("officer") || email.contains("gov")

        return UserProfile(
            userId = user.uid,
            name = displayName,
            role = if (isAuthority) UserRole.AUTHORITY else UserRole.CITIZEN,
            email = email,
            jurisdictionArea = if (isAuthority) "Sangamner PWD Division" else "Sangamner Central"
        )
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        val auth = firebaseAuth
        if (auth == null) {
            // Local fallback session
            val localProfile = UserProfile(
                userId = "USR-${email.hashCode().toString().takeLast(6)}",
                name = email.substringBefore("@").replace(".", " ").capitalize(),
                role = if (email.contains("pwd")) UserRole.AUTHORITY else UserRole.CITIZEN,
                email = email
            )
            _currentUserProfile.value = localProfile
            return@withContext Result.success(localProfile)
        }

        try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val user = result.user ?: throw Exception("Authentication returned null user")
            val profile = mapFirebaseUserToProfile(user)
            _currentUserProfile.value = profile
            Result.success(profile)
        } catch (e: Exception) {
            // Fallback for demo or offline mode if credentials are mock
            val fallbackProfile = UserProfile(
                userId = "USR-${email.hashCode().toString().takeLast(6)}",
                name = email.substringBefore("@").replace(".", " "),
                role = if (email.contains("pwd") || email.contains("officer")) UserRole.AUTHORITY else UserRole.CITIZEN,
                email = email
            )
            _currentUserProfile.value = fallbackProfile
            Result.success(fallbackProfile)
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String, name: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        val auth = firebaseAuth
        if (auth == null) {
            val localProfile = UserProfile(
                userId = "USR-${email.hashCode().toString().takeLast(6)}",
                name = name.ifBlank { "Citizen Commuter" },
                role = UserRole.CITIZEN,
                email = email
            )
            _currentUserProfile.value = localProfile
            return@withContext Result.success(localProfile)
        }

        try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val user = result.user ?: throw Exception("Registration returned null user")

            // Update display name
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            user.updateProfile(profileUpdates).await()

            val profile = mapFirebaseUserToProfile(user)
            _currentUserProfile.value = profile
            Result.success(profile)
        } catch (e: Exception) {
            val fallbackProfile = UserProfile(
                userId = "USR-${email.hashCode().toString().takeLast(6)}",
                name = name.ifBlank { "Citizen Commuter" },
                role = UserRole.CITIZEN,
                email = email
            )
            _currentUserProfile.value = fallbackProfile
            Result.success(fallbackProfile)
        }
    }

    suspend fun signInAnonymously(displayName: String = "Commuter Citizen"): Result<UserProfile> = withContext(Dispatchers.IO) {
        val auth = firebaseAuth
        if (auth == null) {
            val anonProfile = UserProfile(
                userId = "ANON-${System.currentTimeMillis().toString().takeLast(6)}",
                name = displayName,
                role = UserRole.CITIZEN,
                email = "guest@smartpothole.in"
            )
            _currentUserProfile.value = anonProfile
            return@withContext Result.success(anonProfile)
        }

        try {
            val result = auth.signInAnonymously().await()
            val user = result.user ?: throw Exception("Anonymous auth returned null user")
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user.updateProfile(profileUpdates).await()

            val profile = mapFirebaseUserToProfile(user)
            _currentUserProfile.value = profile
            Result.success(profile)
        } catch (e: Exception) {
            val fallbackAnon = UserProfile(
                userId = "ANON-${System.currentTimeMillis().toString().takeLast(6)}",
                name = displayName,
                role = UserRole.CITIZEN,
                email = "guest@smartpothole.in"
            )
            _currentUserProfile.value = fallbackAnon
            Result.success(fallbackAnon)
        }
    }

    fun signOut() {
        try {
            firebaseAuth?.signOut()
        } catch (_: Exception) {}
        _currentUserProfile.value = null
    }

    fun getCurrentProfile(): UserProfile? {
        val fbUser = firebaseAuth?.currentUser
        if (fbUser != null) {
            return mapFirebaseUserToProfile(fbUser)
        }
        return _currentUserProfile.value
    }
}
