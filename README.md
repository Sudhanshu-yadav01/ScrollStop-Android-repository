# ScrollStop

**A digital wellbeing app for more intentional scrolling.**

ScrollStop is an Android app built with Kotlin that helps users become more
aware of their YouTube and Instagram usage. It displays a floating timer while
supported apps are in the foreground and records daily usage statistics and
scrolling activity on the device.

## Features

- Track time spent in YouTube and Instagram.
- Count detected scrolling events for YouTube and Instagram.
- Detect YouTube Shorts-style content changes.
- Display a floating timer over other apps.
- Show daily usage statistics in a dashboard.
- Store usage data locally with Room.

## Tech stack

- Kotlin
- Android SDK
- Jetpack Compose and Material 3
- Room database
- Kotlin coroutines and Flow
- Gradle Kotlin DSL

## Requirements

- Android Studio with Android SDK support for API 37
- Android device or emulator running Android 7.0 (API 24) or newer
- JDK 11

## Getting started

1. Clone this repository.
2. Open the project in Android Studio.
3. Allow Gradle to sync.
4. Run the `app` configuration on an Android device or emulator.
5. Grant the permissions described below when prompted.

You can also build the debug APK from the project root:

```text
./gradlew assembleDebug
```

On Windows, use `gradlew.bat assembleDebug`.

## Permissions

ScrollStop uses Android system services that require explicit user approval:

- **Accessibility service**: observes supported app window and scroll events.
- **Usage access**: identifies the app currently in the foreground.
- **Display over other apps**: shows the floating timer.

These permissions are optional until the corresponding tracking or timer
features are enabled. Android may show additional system warnings for
accessibility and overlay access.

## Privacy

The current implementation stores daily usage totals in a local Room database
on the device. It does not declare the `INTERNET` permission and does not
include a network client, analytics SDK, or sign-in flow. The accessibility
service reads supported app UI events and foreground-app information to
calculate the local counters; it is not intended to collect message content or
credentials.

This privacy description reflects the current source code and should be
reviewed again before a production release if network, analytics, backup, or
account features are added.

## Project status

ScrollStop is an early-stage project. Supported Android versions, detection
behavior, and the user interface may change as the app is developed.

## Contributing

Bug reports, suggestions, and pull requests are welcome:

1. Fork the repository.
2. Create a feature branch.
3. Make and test your changes.
4. Open a pull request with a clear description.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE).
