# JARVIS 2.0

Your Personal AI Phone Manager & Creator.

## Project identity
- **App name:** JARVIS 2.0
- **Application ID:** `com.jarvis.pakage`
- **Platform:** Android
- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Theme:** Adaptive light / dark

## Phase 1 — UI Foundation
- Android Gradle project configuration
- Jetpack Compose Material 3 and adaptive theme
- Home dashboard, quick actions and bottom navigation
- Tools, Video Studio and Settings screens

## Phase 2 — AI Brain
- Gemini 2.5 Flash primary provider
- Groq Llama 3.3 70B fallback
- Chat messages and loading state
- Network requests run off the main thread
- API keys read from ignored local.properties

### Configure AI keys
In the project root, add these lines to your local `local.properties` file (keep any existing `sdk.dir` line):

```properties
GEMINI_API_KEY=your_gemini_api_key
GROQ_API_KEY=your_groq_api_key
```

At least one key is required for live AI replies. If Gemini fails, JARVIS tries Groq. Provider quotas and rate limits still apply; neither service is unlimited.

**Security note:** local.properties is excluded from Git, but keys compiled into an Android APK can be extracted. Use this direct-key setup only for private testing. A backend proxy with rate limits and secret storage is required before distributing the app.

## Build
Open the project in Android Studio, or build with Gradle:

```bash
gradle --no-daemon :app:assembleDebug
```

The project uses Android Gradle Plugin 8.13.2, Kotlin 2.2.21, compile SDK 36 and minimum SDK 24. GitHub Actions builds and uploads a debug APK artifact after a successful run.

## Roadmap
1. UI & App Foundation
2. AI Brain (Gemini + fallback provider)
3. Phone Manager
4. Google & Productivity
5. Automation Agent
6. Daily Reel Maker
7. Video Studio
8. Security & Privacy
9. Integration & Release

> Never commit API keys, OAuth secrets, or personal data.
