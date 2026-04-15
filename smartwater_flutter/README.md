# smartwater_flutter

Flutter **Add-to-App** module embedded in the SmartWater Android app via `FlutterActivity`.

This is not a standalone Flutter app. It is a Flutter module that is compiled and embedded into the native Android app (`SmartWaterMonitoringApp/`) using Flutter's [Add-to-App](https://docs.flutter.dev/add-to-app) pattern.

## What This Module Contains

| Path | Purpose |
|---|---|
| `lib/config/app_config.dart` | Server IP, polling intervals, water quality thresholds |
| `lib/models/` | `WaterQualityData`, `HistoryResponse` data models |
| `lib/providers/` | `WaterQualityProvider` — state management via `Provider` |
| `lib/screens/` | Dashboard, History, Home, Splash screens |
| `lib/services/api_service.dart` | HTTP client for Spring Boot backend |
| `lib/theme/` | Design tokens (glassmorphism colour palette) |
| `lib/widgets/` | `GlassCard`, `ParameterCard`, `StatusBanner`, `TechBackground` |

## Configuration

Before running, set your server IP in `lib/config/app_config.dart`:

```dart
static const String serverIP = '10.0.2.2'; // emulator default — change for real device
```

## Running Standalone (for Flutter development)

```bash
flutter pub get
flutter run
```

> See the [root README](../README.md) for full project documentation and setup instructions.
