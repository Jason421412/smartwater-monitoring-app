/// Model class representing water quality data from the backend.
class WaterQualityData {
  final String deviceId;
  final double temperature;
  final double ph;
  final double turbidity;
  final double tds;
  final int alarmStatus;
  final String timestamp;
  final bool isOnline;
  final String status;

  WaterQualityData({
    required this.deviceId,
    required this.temperature,
    required this.ph,
    required this.turbidity,
    required this.tds,
    required this.alarmStatus,
    required this.timestamp,
    required this.isOnline,
    required this.status,
  });

  /// Create instance from JSON response
  factory WaterQualityData.fromJson(Map<String, dynamic> json) {
    return WaterQualityData(
      deviceId: json['device_id'] ?? '',
      temperature: (json['temperature'] ?? 0).toDouble(),
      ph: (json['ph'] ?? 0).toDouble(),
      turbidity: (json['turbidity'] ?? 0).toDouble(),
      tds: (json['tds'] ?? 0).toDouble(),
      alarmStatus: json['alarm_status'] ?? 0,
      timestamp: json['timestamp'] ?? '',
      isOnline: json['is_online'] ?? false,
      status: json['status'] ?? 'offline',
    );
  }

  /// Create empty/default instance for initial state
  factory WaterQualityData.empty() {
    return WaterQualityData(
      deviceId: '',
      temperature: 0.0,
      ph: 0.0,
      turbidity: 0.0,
      tds: 0.0,
      alarmStatus: 0,
      timestamp: '',
      isOnline: false,
      status: 'offline',
    );
  }

  /// Check if data represents great water quality
  bool get isGreat => status == 'great' || status == 'excellent';

  /// Check if data represents warning status
  bool get isWarning => status == 'warning';

  /// Check if device is offline
  bool get isOffline => status == 'offline' || !isOnline;
}
