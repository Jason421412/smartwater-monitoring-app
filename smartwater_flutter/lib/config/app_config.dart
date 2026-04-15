import 'package:flutter/foundation.dart' show kIsWeb;

/// Application configuration constants.
/// All configurable values are centralized here.
class AppConfig {
  /// Backend server IP address.
  /// ─────────────────────────────────────────────────────────────────────────
  /// ⚠️  Change this to match your environment before running:
  ///   • Android Emulator  → '10.0.2.2'   (loopback alias for host machine)
  ///   • Real Device (LAN) → your computer's LAN IP, e.g. '192.168.1.x'
  ///   • Production        → your hosted backend domain (use HTTPS)
  /// ─────────────────────────────────────────────────────────────────────────
  /// Must match the Spring Boot server port configured in ApiClient.DEFAULT_BASE_URL
  static const String serverIP = '10.0.2.2'; // default: Android emulator loopback

  /// Backend API base URL
  /// Web  -> localhost (Spring Boot)
  /// Mobile (Emulator / Real Device) -> LAN IP (Spring Boot)
  static String get baseUrl {
    if (kIsWeb) {
      // Flutter Web -> Spring Boot
      return 'http://localhost:8080';
    }
    // Android Emulator / Real Device -> Spring Boot
    return 'http://$serverIP:8080';
  }

  /// ================================
  /// Polling & Timing Configuration
  /// ================================

  /// Polling interval for fetching latest data (in seconds)
  static const int pollingIntervalSeconds = 5;

  /// Offline threshold (in seconds)
  /// Device is considered offline if no data received within this duration
  static const int offlineThresholdSeconds = 60;

  /// Splash screen display duration (in seconds)
  static const int splashDurationSeconds = 2;

  /// ================================
  /// Water Quality Thresholds
  /// ================================

  /// Acceptable pH range
  static const double phMin = 6.5;
  static const double phMax = 8.5;

  /// Total Dissolved Solids threshold (ppm)
  static const double tdsThreshold = 300.0;

  /// ================================
  /// API Endpoints (via Spring Boot)
  /// ================================

  /// Get latest water quality reading
  static const String latestEndpoint = '/api/water/latest';

  /// Get historical water quality data
  static const String historyEndpoint = '/api/water/history';
}
