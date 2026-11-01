# CSE489 Assignment 2 — Android Application

An Android application developed in Kotlin and XML to demonstrate Jetpack Fragments, navigation drawers, broadcast receivers, touch gestures, and in-app media playback.

## Features

- **Navigation drawer:** Switch between Broadcast Receiver, Image Scale, Video, and Audio using Jetpack Navigation and Fragments.
- **Custom broadcast:** Enter a text message and send it to a custom `BroadcastReceiver`, which displays the received message.
- **System battery receiver:** Read the device's battery percentage and charging status through Android's `ACTION_BATTERY_CHANGED` broadcast.
- **Image Scale:** Download a photograph over HTTPS and use pinch-to-zoom and drag gestures. The image is fitted to the screen initially and can be enlarged interactively.
- **Video player:** Play or pause a bundled MP4 video, with a seek bar and playback time indicators.
- **Audio player:** Play, pause, and seek through a bundled MP3 file. Playback progress resets when the audio finishes.

## Technologies

- Kotlin and Android XML layouts
- AndroidX Fragments, DrawerLayout, and Jetpack Navigation
- Android `BroadcastReceiver` and `BatteryManager`
- `ScaleGestureDetector` and image matrices
- Android `MediaPlayer` and `TextureView`
- Gradle (Kotlin DSL)

## Project structure

| Path | Purpose |
| --- | --- |
| `app/src/main/java/com/example/cse489assignment2/MainActivity.kt` | Navigation drawer and main screen layout |
| `app/src/main/java/com/example/cse489assignment2/ui/` | Broadcast selector, image, video, and audio Fragments |
| `app/src/main/java/com/example/cse489assignment2/CustomInputActivity.kt` | Custom broadcast message input |
| `app/src/main/java/com/example/cse489assignment2/CustomReceiverActivity.kt` | Custom broadcast reception and message display |
| `app/src/main/java/com/example/cse489assignment2/BatteryActivity.kt` | System battery updates |
| `app/src/main/res/layout/` | Android XML layouts |
| `app/src/main/res/layout-land/` | Landscape-specific layouts |
| `app/src/main/res/navigation/nav_graph.xml` | Fragment navigation graph |
| `app/src/main/res/menu/drawer_menu.xml` | Navigation drawer entries |
| `app/src/main/res/raw/` | Bundled audio and video assets |
| `docs/` | AI usage declaration and media credits |

## Build and run

1. Clone or download the repository.
2. In Android Studio, open the **`CSE489_Assignment2`** folder containing `settings.gradle.kts`.
3. Configure the Android SDK and complete Gradle sync. The project compiles against Android API 37 and targets API 35.
4. Select a compatible Android emulator or physical Android device.
5. Run the **`app`** configuration.

An internet connection is required for the Image Scale feature. The bundled audio and video files work offline.

## Basic testing

- Open each destination from the navigation drawer.
- Select **Custom**, enter a message, send it, and confirm the received text.
- Select **System battery** and confirm the displayed percentage; on an emulator, change the simulated battery level to check updates.
- Open **Image Scale**, verify the download, then pinch to zoom and drag the image.
- Play, pause, and seek through the video and audio. Check both portrait and landscape orientations.

## Documentation

- [AI Usage Declaration](docs/AI_USAGE_DECLARATION.md)
- [Media Credits](docs/MEDIA_CREDITS.md)
