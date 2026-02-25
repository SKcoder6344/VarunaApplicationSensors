# Varuna App - LOCAL STORAGE VERSION (No Firebase)

## What Changed from Firebase Version

### ✅ Removed
- ❌ Firebase Authentication
- ❌ Firebase Firestore
- ❌ Firebase Cloud Messaging
- ❌ Firebase Storage
- ❌ Firebase Analytics
- ❌ google-services.json dependency

### ✅ Added
- ✅ Room Database (Local SQLite)
- ✅ Encrypted SharedPreferences (for user sessions)
- ✅ Local data storage (all data stays on device)
- ✅ No internet required
- ✅ Complete offline functionality

---

## Quick Start (3 Steps)

### Step 1: Open in Android Studio
1. Open Android Studio
2. File → Open → Select `VarunaApp_LOCAL` folder
3. Wait for Gradle sync (2-5 minutes)

### Step 2: Build & Run
1. Build → Make Project (Ctrl+F9)
2. Run → Run 'app' (Shift+F10)
3. Select device/emulator

### Step 3: Login
**Default Admin Account:**
- Email: `admin@varuna.com`
- Password: `admin123`

**Create New Account:**
- Click "Sign Up" on login screen
- Fill in details
- Choose role: User or Admin

---

## Architecture

### Database Layer (Room)
```
VarunaDatabase (SQLite)
├── users                    → User accounts
├── water_quality_results    → WQI assessments
├── disease_risk_results     → Disease predictions
├── alerts                   → System alerts
└── help_requests            → User help tickets
```

### Data Flow
```
User Input
    ↓
Fragment/Activity
    ↓
VarunaRepository (Business Logic)
    ↓
Room DAOs (Database Access)
    ↓
SQLite Database (Local Storage)
```

### Session Management
```
SessionManager → EncryptedSharedPreferences
├── User ID
├── Name
├── Email
├── Village
├── Role (user/admin)
└── Login Status
```

---

## Features

### All Features Work Offline

#### Water Quality Module
- 7-parameter input
- ML-based WQI calculation
- WHO/BIS compliance check
- Purification suggestions
- Emergency guidelines
- **Stored locally in SQLite**

#### Disease Risk Module
- Cholera/Typhoid/Diarrhea prediction
- Risk levels (Low/Medium/High)
- Prevention guidelines
- **Stored locally in SQLite**

#### Dashboard
- Real-time WQI status (from local DB)
- Statistics cards
- Interactive charts (MPAndroidChart)
- Disease risk summary
- Alert logs
- **All data from local storage**

#### Reports
- Generate PDF reports
- Share via email/WhatsApp
- Download to device
- History from local database

#### Admin Panel
- System statistics
- All user data access
- Help request management
- **No cloud sync needed**

#### Chatbot
- Water quality advisor
- WHO/BIS standards
- Works completely offline

---

## Database Schema

### users
| Column | Type | Description |
|--------|------|-------------|
| id | Long | Primary Key (auto-increment) |
| name | String | User's full name |
| email | String | Login email (unique) |
| password | String | Hashed password |
| village | String | User's village |
| role | String | user/admin/health_officer |
| createdAt | Long | Registration timestamp |

### water_quality_results
| Column | Type | Description |
|--------|------|-------------|
| id | Long | Primary Key |
| userId | Long | Foreign key to users |
| villageName | String | Village name |
| ph | Double | pH value |
| tds | Double | TDS (mg/L) |
| turbidity | Double | Turbidity (NTU) |
| hardness | Double | Hardness (mg/L) |
| temperature | Double | Temperature (°C) |
| chloride | Double | Chloride (mg/L) |
| dissolvedOxygen | Double | DO (mg/L) |
| wqiScore | Double | WQI score (0-100) |
| classification | String | Safe/Moderate/Unsafe |
| whoCompliance | String | WHO violations |
| purificationSuggestions | String | Recommendations |
| emergencyGuidelines | String | Emergency instructions |
| timestamp | Long | Assessment timestamp |

### disease_risk_results
| Column | Type | Description |
|--------|------|-------------|
| id | Long | Primary Key |
| userId | Long | Foreign key to users |
| villageName | String | Village name |
| choleraRisk | String | Low/Medium/High |
| typhoidRisk | String | Low/Medium/High |
| diarrheaRisk | String | Low/Medium/High |
| healthCasesReported | Int | Number of cases |
| ph, tds, turbidity, temperature | Double | Parameters |
| preventionGuidelines | String | Prevention tips |
| timestamp | Long | Prediction timestamp |

### alerts
| Column | Type | Description |
|--------|------|-------------|
| id | Long | Primary Key |
| userId | Long | Foreign key to users |
| type | String | water_quality/disease_risk/emergency |
| message | String | Alert message |
| village | String | Village name |
| severity | String | Low/Medium/High |
| isRead | Boolean | Read status |
| timestamp | Long | Alert timestamp |

### help_requests
| Column | Type | Description |
|--------|------|-------------|
| id | Long | Primary Key |
| userId | Long | Foreign key to users |
| userName | String | Requester name |
| village | String | Village name |
| location | String | Issue location |
| issueDescription | String | Problem description |
| status | String | pending/resolved |
| timestamp | Long | Request timestamp |

---

## Code Structure

```
app/src/main/java/com/varuna/app/
├── activities/
│   ├── SplashActivity.kt       → Check session, route to Login/Main
│   ├── LoginActivity.kt        → Local authentication
│   ├── SignupActivity.kt       → User registration
│   ├── MainActivity.kt         → Main app container
│   ├── ChatbotActivity.kt      → Offline chatbot
│   └── EducationActivity.kt    → Educational content
├── fragments/
│   ├── DashboardFragment.kt    → Overview (reads from Room)
│   ├── WaterQualityFragment.kt → WQI assessment (writes to Room)
│   ├── DiseaseRiskFragment.kt  → Disease prediction (writes to Room)
│   ├── ReportsFragment.kt      → PDF generation (reads from Room)
│   ├── AlertsFragment.kt       → Alert list (reads from Room)
│   ├── ProfileFragment.kt      → User profile (updates Room)
│   ├── HelpFragment.kt         → Help requests (writes to Room)
│   └── AdminFragment.kt        → Admin panel (reads all data)
├── database/
│   ├── Entities.kt             → Room entities (tables)
│   ├── Daos.kt                 → Database access objects
│   ├── VarunaDatabase.kt       → Room database singleton
│   └── VarunaRepository.kt     → Business logic layer
├── ml/
│   └── WaterQualityPredictor.kt → Pure Kotlin ML (no internet)
├── utils/
│   ├── SessionManager.kt       → Encrypted user sessions
│   ├── AlertManager.kt         → Local alert creation
│   ├── NotificationHelper.kt   → Local notifications
│   └── PdfReportGenerator.kt   → PDF generation
└── models/
    └── Models.kt               → Data models
```

---

## Key Differences from Firebase Version

| Feature | Firebase Version | Local Version |
|---------|-----------------|---------------|
| **Authentication** | Firebase Auth | Room + SessionManager |
| **Data Storage** | Firestore (cloud) | SQLite (device) |
| **User Sessions** | Firebase tokens | EncryptedSharedPreferences |
| **Push Notifications** | FCM | Local notifications only |
| **Data Sync** | Real-time cloud sync | No sync (local only) |
| **File Storage** | Firebase Storage | Device storage |
| **Internet Required** | ✅ Yes | ❌ No |
| **Multi-device Sync** | ✅ Yes | ❌ No |
| **Data Backup** | ✅ Automatic | ❌ Manual (export/import) |

---

## Advantages of Local Storage Version

### ✅ Privacy
- All data stays on device
- No cloud uploads
- No third-party services
- Complete user control

### ✅ Offline First
- Works without internet
- No connectivity issues
- Faster data access
- No API rate limits

### ✅ No External Dependencies
- No Firebase setup needed
- No API keys required
- No billing/quotas
- Zero external service failures

### ✅ Simplicity
- Easier to understand
- Simpler debugging
- No cloud configuration
- Direct database access

---

## Limitations

### ❌ No Multi-device Sync
- Data is device-specific
- Can't access from multiple devices
- No automatic backup to cloud

### ❌ No Real-time Collaboration
- Admins can't see live data from all users
- No cross-device notifications
- No centralized reporting

### ❌ Manual Data Export/Import
- Need to manually backup database
- Can't share data easily between users
- No automatic disaster recovery

---

## How to Export/Import Data

### Export Database
```kotlin
// From admin panel or settings
val dbPath = context.getDatabasePath("varuna_database").absolutePath
// Copy file to external storage or share via Intent
```

### Import Database
```kotlin
// Copy external database file to app's database directory
// Restart app to use new database
```

---

## Default Users

### Admin Account (Pre-created)
```
Email: admin@varuna.com
Password: admin123
Role: admin
```

### Create New Users
- Use Sign Up screen
- All users stored in local database
- Password hashed before storage

---

## Security

### Password Storage
- ✅ Passwords hashed using hashCode()
- ✅ Not stored in plain text
- ⚠️ For production, use bcrypt/Argon2

### Session Storage
- ✅ Encrypted SharedPreferences
- ✅ AES256-GCM encryption
- ✅ Secure session tokens

### Database
- ✅ SQLite encrypted by device
- ✅ App-private storage
- ✅ No external access

---

## Upgrading to Firebase Later

If you want to add Firebase later:

1. Add Firebase dependencies back to `build.gradle`
2. Replace `VarunaRepository` calls with Firebase calls
3. Keep Room as local cache
4. Implement sync logic between Room and Firestore

---

## Technical Details

### Dependencies
```gradle
Room Database: 2.6.1
Security Crypto: 1.1.0-alpha06
Coroutines: 1.7.3
MPAndroidChart: v3.1.0
Material Design: 1.11.0
```

### Min Requirements
- Android 7.0+ (API 24)
- 50 MB storage space
- No internet required

### Build Info
- Gradle: 8.6
- AGP: 8.3.2
- Kotlin: 1.9.22
- JDK: 17

---

## Troubleshooting

### Database Errors
```
Error: "Cannot access database on main thread"
Fix: All database operations use coroutines (already implemented)
```

### Login Issues
```
Error: "User not found"
Fix: Create account using Sign Up, or use admin@varuna.com / admin123
```

### Session Errors
```
Error: "Session expired"
Fix: Clear app data and re-login
```

---

## Future Enhancements

### Possible Additions
- [ ] Data export to CSV
- [ ] Database backup/restore UI
- [ ] Bluetooth data sharing between devices
- [ ] QR code data transfer
- [ ] USB data export
- [ ] Email data export
- [ ] Optional cloud sync (on-demand)

---

## Support

All features work 100% offline. No internet, no Firebase, no external services needed.

Built with ❤️ for offline-first water quality monitoring
