# SmartWater Monitoring App

An Android client with a Flutter add-to-app module for real-time IoT water quality monitoring, historical charts, GPS pollution reporting, and community discussion.

## Why I Built This

Water quality monitoring is a useful example of software that connects the physical world with mobile and backend systems. This project explores the client-side engineering needed for that workflow: ingesting sensor data, showing readable real-time status, syncing with backend APIs, and letting users report pollution events from the field.

The repository contains the Android client and embedded Flutter module. The Spring Boot REST API and FastAPI sensor service are external components and are not included in this repo.

## Features

- Real-time dashboard for pH and temperature readings.
- Historical water-quality charts with multiple time ranges.
- Bluetooth Classic RFCOMM/SPP sensor connection flow.
- Multi-format sensor parser for key-value, CSV, and JSON-like readings.
- REST API integration through Retrofit and OkHttp.
- JWT bearer-token injection using an OkHttp interceptor.
- Login, registration, profile update, and token persistence flow.
- GPS-based pollution report form with camera/gallery attachment support.
- Google Maps location picker for manual report coordinates.
- Community feed with posts, replies, likes, reposts, bookmarks, profiles, and search.
- Flutter add-to-app dashboard module using Provider and `fl_chart`.
- Screenshot assets for dashboard, Bluetooth, community feed, reporting, and alerts.

## Tech Stack

- **Android:** Java, Android SDK, Material Components, ConstraintLayout
- **Networking:** Retrofit 2, OkHttp, Gson
- **Authentication:** JWT bearer-token flow with `SharedPreferences` token storage
- **IoT / Device:** Android Bluetooth APIs, RFCOMM/SPP socket connection
- **Maps / Location:** Google Maps SDK for Android, Google Play Services Location
- **Charts:** MPAndroidChart on Android, `fl_chart` in Flutter
- **Flutter module:** Flutter, Dart, Provider
- **External backend:** Spring Boot REST API, FastAPI sensor service, PostgreSQL, Redis

## Architecture / System Design

```text
Android UI activities
  -> Retrofit API interfaces
  -> OkHttp client + JwtInterceptor
  -> External Spring Boot REST API
  -> External database/cache layer

Bluetooth sensor
  -> BluetoothConnectionManager
  -> parser and UI callback
  -> dashboard update / backend sync

FlutterActivity
  -> Flutter dashboard module
  -> Provider state management
  -> API service layer
```

- **Frontend:** The Android app is organized around activity screens for dashboard, Bluetooth, reports, alerts, community feed, authentication, and profiles.
- **Backend/API:** `network/ApiClient.java` builds Retrofit clients and exposes separate API interfaces for auth, water data, community, reports, alerts, Bluetooth, and follows.
- **Authentication:** `JwtInterceptor.java` attaches the saved bearer token to outgoing API calls. `TokenStore.java` handles token persistence.
- **Sensor integration:** `BluetoothConnectionManager.java` manages RFCOMM connection setup, read loops, and sensor parsing before dispatching parsed readings back to the UI.
- **Storage:** The included client stores tokens and selected settings locally. Long-term water data, reports, posts, and user records are expected to live in the external backend.
- **Flutter integration:** `smartwater_flutter/` provides an add-to-app dashboard module with its own models, provider, API service, screens, theme, and widgets.
- **Deployment:** This repo is a mobile client. To run end-to-end, the external backend services must also be running and reachable from the emulator or device.

More detail: [docs/architecture.md](docs/architecture.md).

## My Contributions

- Built and documented the Android client structure for dashboard, Bluetooth, reporting, community, and authentication flows.
- Implemented Retrofit API interfaces and a reusable API client layer.
- Added JWT request injection through an OkHttp interceptor.
- Implemented Bluetooth sensor connection and parsing logic.
- Built dashboard and historical chart UI for water-quality readings.
- Added GPS and Google Maps location selection for pollution reports.
- Integrated a Flutter add-to-app dashboard module with Provider-based state handling.
- Added screenshots and setup documentation for portfolio review.

## What I Learned

- How mobile clients coordinate device APIs, backend APIs, authentication state, and UI refresh cycles.
- Why IoT apps need robust parsing and validation around sensor input formats.
- How Retrofit, OkHttp interceptors, DTOs, and token storage fit together in a mobile API client.
- How to separate included client-side work from external backend services in documentation.
- How Flutter add-to-app can be used to introduce a second UI layer into a native Android project.

## Screenshots / Demo

![Dashboard](docs/screenshots/dashboard.png)
![Bluetooth](docs/screenshots/bluetooth.png)
![Community feed](docs/screenshots/community.png)
![Pollution report map](docs/screenshots/report_map.png)
![Alerts](docs/screenshots/alerts.png)

Evidence to add later:

- Short demo video showing Bluetooth connection, dashboard refresh, report creation, and community feed.
- Architecture diagram showing Android client, Flutter module, sensor source, and external backend services.
- Backend repository links if those services are public and safe to share.

## Setup

### Android Client

1. Clone the repository.

   ```bash
   git clone https://github.com/Jason421412/smartwater-monitoring-app.git
   cd smartwater-monitoring-app
   ```

2. Open the Android project in Android Studio.

   ```text
   SmartWaterMonitoringApp/
   ```

3. Create a local properties file from the safe example.

   ```bash
   cp SmartWaterMonitoringApp/local.properties.example SmartWaterMonitoringApp/local.properties
   ```

4. Update `SmartWaterMonitoringApp/local.properties` with your local Android SDK path and your own Google Maps API key.

   ```properties
   sdk.dir=/path/to/your/Android/Sdk
   MAPS_API_KEY=your_google_maps_api_key_here
   ```

5. Run the app on an emulator or Android device with API 24+.

6. Make sure the external backend is reachable:

   - Android emulator host loopback: `http://10.0.2.2:8080/`
   - Physical device: use your machine's LAN IP address.

### Flutter Module

Only needed if you want to modify or run the Flutter module directly.

```bash
cd smartwater_flutter
flutter pub get
flutter run
```

If using a real device, update the server IP in:

```text
smartwater_flutter/lib/config/app_config.dart
```

## Future Improvements

- Add unit tests for Bluetooth parsing and API response mapping.
- Add instrumentation tests for login, dashboard, and report submission flows.
- Add offline cache and retry queue for poor network conditions.
- Add stronger error handling around Bluetooth disconnects and malformed sensor data.
- Move server URL configuration into a safer build/runtime configuration flow.
- Add CI for Android and Flutter checks.
- Publish or link backend repositories with sanitized environment examples.
- Add role-based access control and moderation flows on the backend side.
