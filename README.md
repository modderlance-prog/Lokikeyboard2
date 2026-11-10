# Loki Keyboard for Android

A native Android Input Method Editor (IME) with a green-and-gold theme, Loki-style message rewriting, speech dictation, and Android text-to-speech.

## Build and install

1. Install **Android Studio** on a computer.
2. Open this folder in Android Studio (`File > Open`).
3. Let Gradle sync. If prompted, install Android SDK Platform 35 and a compatible JDK (Android Studio's bundled JDK is fine).
4. Choose **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. Android Studio creates the debug APK under `app/build/outputs/apk/debug/app-debug.apk`.
6. Transfer that APK to your Android phone and open it. Allow installation from that source if Android asks.
7. Open **Loki Keyboard**, optionally save an OpenAI API key, then tap:
   - **ENABLE LOKI KEYBOARD** in Android settings.
   - **SELECT LOKI KEYBOARD** and choose Loki Keyboard.
   - **GRANT MICROPHONE PERMISSION** for voice dictation.
8. In any text field, switch keyboards using the keyboard selector.

## Features

- Native Android keyboard with letter, space, backspace, punctuation and enter keys.
- **LOKI** rewrites the current typed/dictated text in a regal, witty, mischievous style.
- **MIC** uses Android speech recognition to dictate into the active text field.
- **SPEAK** reads the current keyboard buffer using Android TextToSpeech.
- Without an API key, an offline phrase-based rewrite is used.
- With an OpenAI API key, the keyboard uses `gpt-4o-mini` for more natural rewrites.

## Important notes

- This ZIP contains the complete Android Studio project source, not a precompiled APK. An APK must be built with Android Studio/Android SDK.
- AI rewriting sends the text you choose to transform to the OpenAI API. Speech recognition may use your phone's configured recognition provider. Review their privacy terms before use.
- The optional API key is stored in the app's private SharedPreferences on the device. Do not share the key or the APK with it embedded.
- This is an original Loki-inspired writing style, not an official Marvel product or an imitation of the actor's voice. Spoken output uses your phone's installed text-to-speech voice; it is not Tom Hiddleston's voice.
- Some Android keyboards may restrict microphone use until the permission is granted from the setup app.


## Build the APK online (no Android Studio required on your phone)

This project includes a GitHub Actions workflow:

1. Create a new GitHub repository and upload the contents of this project ZIP.
2. Open the repository's **Actions** tab.
3. Select **Build Loki Keyboard APK** and press **Run workflow** (or push to `main`).
4. When the run finishes, open it and download the `LokiKeyboard-debug-apk` artifact.
5. Extract the artifact ZIP to get `app-debug.apk`, then transfer it to your Android phone and install it.

The workflow compiles the Android app using GitHub's hosted build machine. A GitHub account and internet access are required.
