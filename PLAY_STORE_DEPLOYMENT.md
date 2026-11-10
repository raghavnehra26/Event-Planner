# FocusForge: Habit & Deep Work Engine — Play Store Deployment & Monetization Playbook

---

## 1. App Store Optimization (ASO) Metadata & Copy

### App Title (30 Characters Limit)
`FocusForge: Habit & Deep Work` *(29 chars)*

### Short Description (80 Characters Limit)
`Distraction-free Pomodoro, habit streak engine & git-style heatmap tracker.` *(75 chars)*

### Full Description (Play Store SEO-Optimized - Max 4000 Characters)
```text
Forge laser-sharp focus and build unstoppable streaks with FocusForge — the zero-bloat, distraction-free productivity engine built for developers, students, and high-performers.

No forced accounts. No cloud servers draining your battery. No ads interrupting your deep work blocks. FocusForge is 100% offline-first and engineered for raw execution speed.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━
WHY HIGH-PERFORMERS CHOOSE FOCUSFORGE
━━━━━━━━━━━━━━━━━━━━━━━━━━━━

🔥 GIT-STYLE CONTRIBUTION HEATMAP
Visualize your daily momentum with high-contrast, GitHub-style contribution heatmaps. Track every habit, workout, and deep work session as a green square on your grid. See your consistency multiply week over week.

⏱️ PRECISION POMODORO & CUSTOM INTERVALS
Customizable deep work sessions (25m Focus, 5m Short Break, 15m Long Break, or custom 50m/90m ultra-deep sprints). Smooth circular sweep timer, subtle audio chimes, and tactile haptic feedback designed to keep you in flow state.

🔒 ZERO-BLOAT & STRICTLY OFFLINE-FIRST
Your habits, journals, and focus analytics stay on your device. We use a lightning-fast local Room database with zero telemetry, zero server delays, and zero data harvesting. 

📈 DEEP ANALYTICS & STATS
Inspect your total focus hours, completed blocks, consistency scores, and active daily streaks. Compare performance across categories: Coding, Fitness, Study, Mindfulness, and Productivity.

📦 DATA FREEDOM (EXPORT TO CSV & JSON)
You own 100% of your data. Export your entire habit history and focus logs in clean CSV or JSON format anytime with a single tap.

━━━━━━━━━━━━━━━━━━━━━━━━━━━━
FREE VS. FOCUSFORGE PRO
━━━━━━━━━━━━━━━━━━━━━━━━━━━━

FREE TIER:
• Track up to 3 active habits and workouts simultaneously
• Core Pomodoro focus timer with haptics & audio cues
• 7-day contribution heatmap history & statistics
• Daily streak reminders via smart local notifications

PRO TIER (LIFETIME OR MONTHLY):
• Unlimited habit, workout, and study trackers
• Full 365-day git-style contribution streak matrix
• 100% ad-free experience guaranteed
• One-tap CSV & JSON complete data export
• Custom timer interval presets & pro themes

━━━━━━━━━━━━━━━━━━━━━━━━━━━━
HOW FOCUSFORGE PROTECTS YOUR FLOW
━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Unlike clunky bloated apps that demand email signups and send spam notifications, FocusForge gets out of your way. Open the app, tap your habit, start your focus session, and conquer your day.

Download FocusForge now and forge your best work.
```

---

## 2. Configuration & ID Replacement Checklist

Before generating your production release build, replace the sample test identifiers with your real Google Play Console and AdMob keys:

### A. AdMob Identifiers
1. **AdMob App ID**
   - File: `app/src/main/AndroidManifest.xml`
   - Locate:
     ```xml
     <meta-data
         android:name="com.google.android.gms.ads.APPLICATION_ID"
         android:value="ca-app-pub-3940256099942544~3347511713" />
     ```
   - Replace with your production AdMob App ID (e.g., `ca-app-pub-XXXXXXXX~YYYYYYYY`).

2. **Banner & Rewarded Ad Unit IDs**
   - File: `app/src/main/java/com/focusforge/app/monetization/MonetizationConfig.kt`
   - Update:
     ```kotlin
     const val ADMOB_BANNER_AD_UNIT_ID = "ca-app-pub-XXXXXXXX/BANNER_ID"
     const val ADMOB_REWARDED_AD_UNIT_ID = "ca-app-pub-XXXXXXXX/REWARDED_ID"
     ```

### B. Google Play Billing Setup
1. **Product IDs in Google Play Console**
   - Go to: **Google Play Console > Monetize > In-app products**
     - Create In-app product: `focusforge_pro_lifetime` (e.g. Price: \$19.99 USD)
   - Go to: **Google Play Console > Monetize > Subscriptions**
     - Create Subscription: `focusforge_pro_monthly` (e.g. Price: \$3.99/month)
2. **Debug-only billing simulation**
   - The simulated purchase path is gated by `BuildConfig.DEBUG` in `BillingManager`; it is not used by a release variant.
   - The debug paywall labels simulated purchases and clarifies that no payment is taken.
   - Verify real Play Console product IDs and purchase/restore flows through a Play test track before releasing.

---

## 3. Signing Keystore & `.aab` Generation Guide

### Step 1: Generate a Production Keystore
Run the following command in PowerShell / Terminal:
```bash
keytool -genkey -v -keystore focusforge-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias focusforge-key
```
Store `focusforge-release-key.jks` in a secure location and back it up.

### Step 2: Configure Signing in `app/build.gradle.kts`
Create a `keystore.properties` in your project root (which is added to `.gitignore`):
```properties
storeFile=../focusforge-release-key.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=focusforge-key
keyPassword=YOUR_KEY_PASSWORD
```

Reference it in `app/build.gradle.kts`:
```kotlin
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        load(FileInputStream(keystorePropertiesFile))
    }
}

android {
    ...
    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

### Step 3: Build the Production Android App Bundle (.aab)
Execute:
```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat bundleRelease
```
The output signed artifact will be generated at:
`app/build/outputs/bundle/release/app-release.aab`

---

## 4. Google Play Console Release Checklist
1. **Target API Level**: Target SDK 34 / 35 (Meets latest Google Play 2026/2025 guidelines).
2. **App Content Declarations**:
   - **Ads**: Check "Yes, my app contains ads" (due to AdMob banner/rewarded).
   - **Privacy Policy**: Mention offline Room storage and Google Mobile Ads data policy.
   - **Target Age**: 13+ (Productivity, developer, students).
   - **Financial Features**: Check "Google Play In-app purchases".
3. **Upload Bundle**: Upload `app-release.aab` to Closed Testing (or Production track).
4. **Publish Release**: Roll out to 100% of users.
