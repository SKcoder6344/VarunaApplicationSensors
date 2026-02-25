# Varuna App - Manual File Update Guide

## Critical Files to Update (in order)

### 1. `gradle/wrapper/gradle-wrapper.properties`

**Location**: `YourProject/gradle/wrapper/gradle-wrapper.properties`

**Replace entire file with**:
```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.6-bin.zip
networkTimeout=10000
validateDistributionUrl=true
```

---

### 2. `build.gradle` (root - project level)

**Location**: `YourProject/build.gradle`

**Replace entire file with**:
```groovy
// Root build.gradle
plugins {
    id 'com.android.application' version '8.3.2' apply false
    id 'org.jetbrains.kotlin.android' version '1.9.22' apply false
    id 'com.google.gms.google-services' version '4.4.1' apply false
}
```

---

### 3. `settings.gradle`

**Location**: `YourProject/settings.gradle`

**Replace entire file with**:
```groovy
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
rootProject.name = "Varuna"
include ':app'
```

---

### 4. `app/build.gradle` (module level)

**Location**: `YourProject/app/build.gradle`

**Replace entire file with**:
```groovy
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    id 'com.google.gms.google-services'
}

android {
    namespace 'com.varuna.app'
    compileSdk 34

    defaultConfig {
        applicationId "com.varuna.app"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0"
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = '17'
    }

    buildFeatures {
        viewBinding true
    }

    packaging {
        resources {
            excludes += [
                'META-INF/DEPENDENCIES',
                'META-INF/LICENSE',
                'META-INF/LICENSE.txt',
                'META-INF/NOTICE',
                'META-INF/NOTICE.txt',
                'META-INF/*.kotlin_module',
                'META-INF/AL2.0',
                'META-INF/LGPL2.1'
            ]
        }
    }
}

dependencies {
    // ── AndroidX Core ───────────────────────────────────────────────────────
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'
    implementation 'androidx.navigation:navigation-fragment-ktx:2.7.7'
    implementation 'androidx.navigation:navigation-ui-ktx:2.7.7'
    implementation 'androidx.drawerlayout:drawerlayout:1.2.0'
    implementation 'androidx.cardview:cardview:1.0.0'
    implementation 'androidx.swiperefreshlayout:swiperefreshlayout:1.1.0'
    implementation 'androidx.activity:activity-ktx:1.8.2'
    implementation 'androidx.fragment:fragment-ktx:1.6.2'

    // ── Firebase ────────────────────────────────────────────────────────────
    implementation platform('com.google.firebase:firebase-bom:32.7.4')
    implementation 'com.google.firebase:firebase-auth-ktx'
    implementation 'com.google.firebase:firebase-firestore-ktx'
    implementation 'com.google.firebase:firebase-storage-ktx'
    implementation 'com.google.firebase:firebase-messaging-ktx'
    implementation 'com.google.firebase:firebase-analytics-ktx'

    // ── Coroutines ──────────────────────────────────────────────────────────
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3'

    // ── Charts ──────────────────────────────────────────────────────────────
    implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'

    // ── Image Loading ────────────────────────────────────────────────────────
    implementation 'com.github.bumptech.glide:glide:4.16.0'

    // ── UI ───────────────────────────────────────────────────────────────────
    implementation 'de.hdodenhof:circleimageview:3.1.0'
    implementation 'com.airbnb.android:lottie:6.1.0'

    // ── Testing ──────────────────────────────────────────────────────────────
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
}
```

**IMPORTANT**: Remove any lines with `retrofit`, `tensorflow-lite`, `shimmer`, or `itext7` if present

---

### 5. `gradle.properties`

**Location**: `YourProject/gradle.properties`

**Replace entire file with**:
```properties
# Project-wide Gradle settings
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true

# AndroidX
android.useAndroidX=true

# Kotlin
kotlin.code.style=official

# Disable Jetifier (not needed if using AndroidX only)
android.enableJetifier=false

# Suppress version catalog warnings
android.suppressUnsupportedOptionWarnings=android.suppressUnsupportedOptionWarnings
```

---

### 6. `app/proguard-rules.pro`

**Location**: `YourProject/app/proguard-rules.pro`

**Create this file if it doesn't exist**:
```proguard
# Varuna App ProGuard Rules
# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep data model classes
-keep class com.varuna.app.models.** { *; }

# Keep Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { *; }

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }
```

---

## After Updating All Files

### Step 1: Invalidate Caches
```
Android Studio → File → Invalidate Caches → Invalidate and Restart
```

### Step 2: Clean Project
```
Build → Clean Project
```

### Step 3: Sync Gradle
```
File → Sync Project with Gradle Files
```

### Step 4: Rebuild
```
Build → Rebuild Project
```

---

## Troubleshooting

### If you get "Unsupported class file major version"
→ Your JDK is too old. Install **JDK 17** and set it in Android Studio:
```
File → Project Structure → SDK Location → JDK Location → [Select JDK 17]
```

### If you get "Could not resolve com.android.application:8.3.2"
→ Your internet is blocking Maven repos. Add this to `settings.gradle` repositories:
```groovy
maven { url = uri("https://dl.google.com/dl/android/maven2/") }
```

### If you get "Failed to download Gradle 8.6"
→ Download manually:
1. Go to https://services.gradle.org/distributions/gradle-8.6-bin.zip
2. Download the file
3. Place it in `C:\Users\SAJAL JAIN\.gradle\wrapper\dists\gradle-8.6-bin\[random-folder]\`
4. Restart Android Studio

---

## Version Summary

| Component | Version |
|-----------|---------|
| Gradle | 8.6 |
| AGP (Android Gradle Plugin) | 8.3.2 |
| Kotlin | 1.9.22 |
| compileSdk | 34 |
| targetSdk | 34 |
| minSdk | 24 |
| JDK | 17 |
| Firebase BOM | 32.7.4 |

---

## Need Help?

If manual updates fail, **just use the zip file** (`VarunaApp_v6_FINAL.zip`) — it contains ALL these files pre-configured correctly.
