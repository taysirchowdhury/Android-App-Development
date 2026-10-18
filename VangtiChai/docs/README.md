# VangtiChai

CSE489 Mobile Application Development — Assignment 1.

VangtiChai is an Android application that accepts a non-negative Taka amount through a custom numeric keypad and calculates a greedy denomination breakdown using **500, 100, 50, 20, 10, 5, 2, and 1 Taka** notes.

## Features

- Custom digit keypad (`0`–`9`) and `CLEAR` button
- Live updates of the amount and note counts
- Portrait and landscape layouts for phone and tablet form factors
- Preservation of entered digits across Activity recreation on rotation
- Android XML/ConstraintLayout layouts with device-qualified resources

## Project structure

- `app/src/main/java/com/example/vangtichai/MainActivity.kt` — application behavior and calculations
- `app/src/main/res/layout/activity_main.xml` — phone portrait
- `app/src/main/res/layout-land/activity_main.xml` — phone landscape
- `app/src/main/res/layout-sw600dp/activity_main.xml` — tablet portrait
- `app/src/main/res/layout-sw600dp-land/activity_main.xml` — tablet landscape
- `app/src/main/res/values*/sizes.xml` — layout dimension and fraction resources
- `docs/AI_USAGE_DECLARATION.md` — disclosure of AI assistance

## Build and run

1. Clone or download this repository.
2. Open the **repository root** (the folder with `settings.gradle.kts`) in Android Studio.
3. Configure the local Android SDK when prompted. Android Studio generates a local `local.properties` file; it is intentionally not committed.
4. Allow Gradle sync to complete.
5. Select an Android emulator and run the `app` configuration.

## Test cases

Verify the following on your device(s):

| Entered amount | Expected denominations |
| --- | --- |
| `6` | `5 × 1`, `1 × 1` |
| `68` | `50 × 1`, `10 × 1`, `5 × 1`, `2 × 1`, `1 × 1` |
| `688` | One of each available denomination |
| `6880` | `500 × 13`, `100 × 3`, `50 × 1`, `20 × 1`, `10 × 1` |

Also test `CLEAR` and whether the amount and denomination counts remain correct after rotation.

**Tested device configurations:** Document device names, Android versions, orientations, and outcomes here after verifying them on the final code revision. Do not claim unperformed tests.

## AI disclosure

This assignment used substantial generative-AI assistance in source generation and debugging. See [AI Usage Declaration](docs/AI_USAGE_DECLARATION.md).

## Academic use

This repository is shared for coursework/documentation purposes. Public visibility does not automatically grant permission to reuse the code as a submitted assignment. Check the applicable course academic-integrity rules.
