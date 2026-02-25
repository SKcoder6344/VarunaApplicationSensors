# Firebase → Local Storage Conversion Guide

## What Was Changed

### Files Added (4 new files)
1. `app/src/main/java/com/varuna/app/database/Entities.kt` - Room database entities
2. `app/src/main/java/com/varuna/app/database/Daos.kt` - Database access objects
3. `app/src/main/java/com/varuna/app/database/VarunaDatabase.kt` - Room database singleton
4. `app/src/main/java/com/varuna/app/database/VarunaRepository.kt` - Repository pattern
5. `app/src/main/java/com/varuna/app/utils/SessionManager.kt` - Session management

### Files Modified
- `app/build.gradle` - Removed Firebase dependencies, added Room
- `build.gradle` (root) - Removed google-services plugin
- All Activities and Fragments need to use `VarunaRepository` instead of Firebase APIs

### Files to Remove
- `google-services.json` - Not needed
- `firestore.rules` - Not applicable

---

## How to Update Your Activities/Fragments

### Before (Firebase):
```kotlin
// Login with Firebase
FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
    .addOnSuccessListener { result ->
        // Success
    }
```

### After (Local):
```kotlin
// Login with Room
val repository = VarunaRepository(requireContext())
lifecycleScope.launch {
    val user = repository.loginUser(email, password)
    if (user != null) {
        SessionManager(requireContext()).saveUserSession(
            user.id, user.name, user.email, user.village, user.role
        )
        // Success
    }
}
```

---

## Migration Steps for Each Component

### 1. SplashActivity
**Change:**
```kotlin
// Before: Check Firebase auth
val currentUser = FirebaseAuth.getInstance().currentUser

// After: Check session
val sessionManager = SessionManager(this)
if (sessionManager.isLoggedIn()) {
    startActivity(Intent(this, MainActivity::class.java))
} else {
    startActivity(Intent(this, LoginActivity::class.java))
}
```

### 2. LoginActivity
**Change:**
```kotlin
// Before: Firebase Auth
FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)

// After: Room + SessionManager
lifecycleScope.launch {
    val repository = VarunaRepository(this@LoginActivity)
    val user = repository.loginUser(email, password)
    if (user != null) {
        SessionManager(this@LoginActivity).saveUserSession(...)
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
    } else {
        Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
    }
}
```

### 3. SignupActivity
**Change:**
```kotlin
// Before: Firebase Auth
FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
    .addOnSuccessListener { result ->
        val user = hashMapOf(
            "name" to name,
            "email" to email,
            "village" to village,
            "role" to role
        )
        FirebaseFirestore.getInstance().collection("users")
            .document(result.user!!.uid).set(user)
    }

// After: Room
lifecycleScope.launch {
    val repository = VarunaRepository(this@SignupActivity)
    val userId = repository.registerUser(name, email, password, village, role)
    if (userId > 0) {
        Toast.makeText(this@SignupActivity, "Account created!", Toast.LENGTH_SHORT).show()
        finish()
    }
}
```

### 4. WaterQualityFragment
**Change:**
```kotlin
// Before: Save to Firestore
val result = hashMapOf(
    "ph" to ph,
    "tds" to tds,
    // ...
)
FirebaseFirestore.getInstance().collection("water_quality_results")
    .add(result)

// After: Save to Room
lifecycleScope.launch {
    val repository = VarunaRepository(requireContext())
    val sessionManager = SessionManager(requireContext())
    repository.saveWaterQualityResult(
        userId = sessionManager.getUserId(),
        villageName = sessionManager.getUserVillage(),
        ph = ph,
        tds = tds,
        // ...
    )
}
```

### 5. DashboardFragment
**Change:**
```kotlin
// Before: Load from Firestore
FirebaseFirestore.getInstance().collection("water_quality_results")
    .whereEqualTo("userId", currentUserId)
    .orderBy("timestamp", Query.Direction.DESCENDING)
    .limit(10)
    .get()
    .addOnSuccessListener { documents ->
        // Process results
    }

// After: Load from Room
val repository = VarunaRepository(requireContext())
val sessionManager = SessionManager(requireContext())
repository.getUserWaterQualityResults(sessionManager.getUserId())
    .observe(viewLifecycleOwner) { results ->
        // Process results
    }
```

### 6. AlertsFragment
**Change:**
```kotlin
// Before: Load from Firestore
FirebaseFirestore.getInstance().collection("alerts")
    .whereEqualTo("userId", userId)
    .orderBy("timestamp")
    .addSnapshotListener { snapshot, error ->
        // Real-time updates
    }

// After: Load from Room (LiveData auto-updates)
val repository = VarunaRepository(requireContext())
repository.getUserAlerts(sessionManager.getUserId())
    .observe(viewLifecycleOwner) { alerts ->
        alertAdapter.submitList(alerts)
    }
```

### 7. ProfileFragment
**Change:**
```kotlin
// Before: Update Firestore
val updates = hashMapOf<String, Any>(
    "name" to newName,
    "village" to newVillage
)
FirebaseFirestore.getInstance().collection("users")
    .document(userId)
    .update(updates)

// After: Update Room + Session
lifecycleScope.launch {
    val repository = VarunaRepository(requireContext())
    val user = repository.getUserById(sessionManager.getUserId())
    if (user != null) {
        val updatedUser = user.copy(name = newName, village = newVillage)
        repository.updateUser(updatedUser)
        sessionManager.updateUserInfo(newName, newVillage)
    }
}
```

### 8. AdminFragment
**Change:**
```kotlin
// Before: Load all from Firestore
FirebaseFirestore.getInstance().collection("water_quality_results")
    .get()

// After: Load all from Room
val repository = VarunaRepository(requireContext())
if (sessionManager.isAdmin()) {
    repository.getAllWaterQualityResults()
        .observe(viewLifecycleOwner) { results ->
            // Show all results
        }
}
```

---

## Complete Code Examples

### LoginActivity.kt (Complete)
```kotlin
package com.varuna.app.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.varuna.app.database.VarunaRepository
import com.varuna.app.databinding.ActivityLoginBinding
import com.varuna.app.utils.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit as binding: ActivityLoginBinding
    private lateinit var repository: VarunaRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = VarunaRepository(this)
        sessionManager = SessionManager(this)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val user = repository.loginUser(email, password)
                if (user != null) {
                    sessionManager.saveUserSession(
                        user.id,
                        user.name,
                        user.email,
                        user.village,
                        user.role
                    )
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}
```

---

## Testing Checklist

After conversion, test:

- [ ] Login with admin@varuna.com / admin123
- [ ] Create new user account
- [ ] Submit water quality assessment
- [ ] View dashboard with local data
- [ ] Check disease risk prediction
- [ ] Generate PDF report
- [ ] Create alert
- [ ] Submit help request
- [ ] Admin panel access (for admin role)
- [ ] Logout and re-login

---

## Database Location

The SQLite database is stored at:
```
/data/data/com.varuna.app/databases/varuna_database
```

To view with Android Studio:
1. View → Tool Windows → App Inspection
2. Select "Database Inspector"
3. View all tables and data

---

## Notes

- All Firebase code needs to be replaced
- Use lifecycleScope.launch for all database operations
- Session is stored in EncryptedSharedPreferences
- Default admin account is created on first app launch
- No internet permission needed
