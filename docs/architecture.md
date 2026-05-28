# Architecture

SmartWater Monitoring App is a mobile client repository containing a native Android app and a Flutter add-to-app module.

## Android Client

```text
SmartWaterMonitoringApp/
  app/src/main/java/com/smartwater/monitoring/
    *Activity.java          Android screens
    network/                Retrofit APIs, DTOs, token handling
    bluetooth/              Bluetooth connection and parsing
    adapter/                RecyclerView adapters
```

The Android client covers:

- dashboard and chart screens
- Bluetooth sensor connection
- login/registration/profile flows
- pollution report creation
- Google Maps location selection
- community feed interactions
- alert/report/profile screens

## Network Layer

The network package uses Retrofit and OkHttp:

- `ApiClient.java` builds API clients.
- API interfaces describe auth, water data, reports, alerts, community, follows, and Bluetooth-related endpoints.
- `JwtInterceptor.java` attaches saved bearer tokens.
- DTO classes define request and response shapes.

The backend services are external to this repository.

## Sensor Flow

```text
Bluetooth device
  -> BluetoothConnectionManager
  -> parse reading formats
  -> dashboard/report UI update
  -> optional API sync through network layer
```

Sensor parsing should be treated defensively because real device output can be malformed, missing fields, or disconnected mid-stream.

## Flutter Module

`smartwater_flutter/` contains a dashboard-style Flutter module using Provider and `fl_chart`. It can be run separately during Flutter development or embedded through the Android host app depending on local setup.

## Configuration Boundary

Local configuration should stay out of Git:

- Android SDK path
- Google Maps API key
- local backend IPs
- signing keys
- Firebase/Google service files

Use `SmartWaterMonitoringApp/local.properties.example` as the safe placeholder reference.

## Limitations

- This repository is the mobile client only.
- External backend and sensor services are not included.
- End-to-end behavior depends on reachable backend services and compatible Bluetooth hardware.
- Production use would need stronger token lifecycle handling, offline retry, input validation, backend authorization, and mobile release signing practices.
