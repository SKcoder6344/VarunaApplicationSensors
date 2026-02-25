> Forked from [sajaljain7925/VarunaApplicationSensors](https://github.com/sajaljain7925/VarunaApplicationSensors) — extended with hardware sensor integration.
# Varuna Water Quality Monitoring App

## Quick Start (3 Steps)

### Step 1: Open in Android Studio
1. Open Android Studio
2. File → Open → Select this `VarunaApp_COMPLETE` folder
3. Wait for Gradle sync (2-5 minutes first time)

### Step 2: Add Firebase Config
1. Go to https://console.firebase.google.com
2. Select your project → Project Settings
3. Download `google-services.json`
4. Replace the placeholder file at: `app/google-services.json`

### Step 3: Build & Run
1. Build → Make Project (Ctrl+F9)
2. Run → Run 'app' (Shift+F10)
3. Select your device/emulator

---

## What's Included

### ✅ Complete Android App (74 files)
- 6 Activities (Splash, Login, Signup, Main, Chatbot, Education)
- 8 Fragments (Dashboard, Water Quality, Disease Risk, Reports, Alerts, Profile, Help, Admin)
- ML Engine with trained Random Forest model (853 KB)
- Firebase integration (Auth, Firestore, Storage, Messaging)
- PDF report generation
- Real-time alerts
- Disease risk prediction
- Charts & analytics

### ✅ Pre-trained ML Model
- Location: `app/src/main/assets/varuna_model.json`
- 5 Random Forest classifiers
- WQI prediction (0-100 score)
- Disease risk (Cholera, Typhoid, Diarrhea)
- No external ML library needed - pure Kotlin inference

### ✅ Gradle Configuration (All Fixed)
- Gradle: 8.6
- AGP: 8.3.2
- Kotlin: 1.9.22
- Zero dependency conflicts
- No Retrofit, no TensorFlow POM issues

---

## Project Structure

```
VarunaApp_COMPLETE/
├── app/
│   ├── src/main/
│   │   ├── java/com/varuna/app/
│   │   │   ├── activities/      # 6 activity files
│   │   │   ├── fragments/       # 8 fragment files
│   │   │   ├── ml/              # WaterQualityPredictor.kt
│   │   │   ├── utils/           # PDF, Alerts, Notifications
│   │   │   ├── adapters/        # RecyclerView adapters
│   │   │   ├── models/          # Data models
│   │   │   └── firebase/        # FCM service
│   │   ├── assets/
│   │   │   └── varuna_model.json  # 853 KB trained ML model
│   │   └── res/                 # 21 layouts, 16 drawables, themes
│   ├── build.gradle             # Module-level config
│   └── google-services.json     # REPLACE THIS with your Firebase config
├── gradle/wrapper/
│   └── gradle-wrapper.properties
├── build.gradle                 # Root-level config
├── settings.gradle
├── gradle.properties
├── firestore.rules              # Deploy these to Firebase
└── UPDATE_GUIDES/               # Reference docs if needed

```

---

## Features

### Water Quality Module
- 7-parameter input (pH, TDS, Turbidity, Hardness, Temperature, Chloride, DO)
- ML-based WQI calculation (0-100 score)
- WHO/BIS compliance check
- Parameter-specific purification suggestions
- Emergency guidelines for unsafe water

### Disease Risk Module
- Cholera risk prediction
- Typhoid risk prediction
- Diarrhea risk prediction
- Risk levels: Low / Medium / High
- Prevention guidelines

### Dashboard
- Real-time WQI status
- Statistics cards (Safe/Moderate/Unsafe counts)
- Interactive line chart (MPAndroidChart)
- Disease risk summary
- Recent alert logs

### Reports
- Generate PDF reports with all data
- Share via email/WhatsApp
- Download to device
- History of previous assessments

### Admin Panel
- System statistics
- Emergency alert dispatch
- Pending help request management
- User role: admin/health_officer

### Chatbot
- Water quality advisor
- WHO/BIS standards reference
- Purification method explanations
- Disease prevention tips
- ORS recipe for diarrhea

---

## Technical Details

### Dependencies (All Gradle 8 Compatible)
```gradle
AndroidX: Core, AppCompat, Material, Navigation, Lifecycle
Firebase: Auth, Firestore, Storage, Messaging, Analytics (BOM 32.7.4)
Charts: MPAndroidChart v3.1.0 (JitPack)
Coroutines: 1.7.3
Image Loading: Glide 4.16.0
UI: CircleImageView, Lottie animations
```

### ML Implementation
- Pure Kotlin Random Forest interpreter
- No TensorFlow Lite dependency
- Reads JSON model file at runtime
- Fallback to mathematical WQI if model unavailable
- Zero dependency conflicts

### Firebase Setup Required
1. Enable Authentication (Email/Password)
2. Enable Firestore Database
3. Enable Cloud Storage
4. Enable Cloud Messaging (FCM)
5. Deploy security rules from `firestore.rules`

---

## Troubleshooting

### If Gradle sync fails
1. File → Invalidate Caches → Invalidate and Restart
2. Build → Clean Project
3. File → Sync Project with Gradle Files

### If you get "Unsupported class file major version"
→ Install JDK 17:
- File → Project Structure → SDK Location → JDK Location
- Download JDK 17 from: https://adoptium.net/

### If Firebase doesn't work
→ Replace `app/google-services.json` with your real one from Firebase Console

### If ML model doesn't load
→ The model is already at `app/src/main/assets/varuna_model.json` (853 KB)
→ Check Android Studio's file explorer to verify it's there

---

## Need to Update Files Manually?

If you have an existing project with custom changes:
1. See `UPDATE_GUIDES/MANUAL_UPDATE_GUIDE.md` for complete instructions
2. See `UPDATE_GUIDES/QUICK_CHECKLIST.txt` for quick reference
3. Copy individual files from `UPDATE_GUIDES/IndividualFiles/`

---

## Version Info

| Component | Version |
|-----------|---------|
| Gradle | 8.6 |
| Android Gradle Plugin | 8.3.2 |
| Kotlin | 1.9.22 |
| Compile SDK | 34 |
| Target SDK | 34 |
| Min SDK | 24 (Android 7.0+) |
| JDK Required | 17 |
| Firebase BOM | 32.7.4 |

---

## Project Statistics

- 74 files total
- 6 Activities
- 8 Fragments
- 3 Utilities (PDF, Alerts, Notifications)
- 3 Adapters
- 5 Data Models
- 21 XML Layouts
- 16 Vector Drawables
- 1 ML Model (853 KB, 5 Random Forests)
- Firebase integration
- Zero external ML dependencies
- Zero Gradle conflicts

---

## Support

For issues, check:
1. `UPDATE_GUIDES/` folder for manual update instructions
2. Firebase Console for correct `google-services.json`
3. Android Studio logs for specific errors

Built with ❤️ for water quality monitoring
