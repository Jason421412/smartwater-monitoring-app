/// Model class representing a single history data point.
class HistoryDataPoint {
  final String timestamp;
  final double? ph;
  final double? temperature;
  final double? tds;
  final double? turbidity;
  final double? phAvg;
  final double? phMax;
  final double? phMin;
  final double? temperatureAvg;
  final double? temperatureMax;
  final double? temperatureMin;
  final double? tdsAvg;
  final double? tdsMax;
  final double? tdsMin;
  final double? turbidityAvg;
  final double? turbidityMax;
  final double? turbidityMin;

  HistoryDataPoint({
    required this.timestamp,
    this.ph,
    this.temperature,
    this.tds,
    this.turbidity,
    this.phAvg,
    this.phMax,
    this.phMin,
    this.temperatureAvg,
    this.temperatureMax,
    this.temperatureMin,
    this.tdsAvg,
    this.tdsMax,
    this.tdsMin,
    this.turbidityAvg,
    this.turbidityMax,
    this.turbidityMin,
  });

  factory HistoryDataPoint.fromJson(Map<String, dynamic> json) {
    return HistoryDataPoint(
      timestamp: json['timestamp'] ?? '',
      ph: json['ph']?.toDouble(),
      temperature: json['temperature']?.toDouble(),
      tds: json['tds']?.toDouble(),
      turbidity: json['turbidity']?.toDouble(),
      phAvg: json['ph_avg']?.toDouble(),
      phMax: json['ph_max']?.toDouble(),
      phMin: json['ph_min']?.toDouble(),
      temperatureAvg: json['temperature_avg']?.toDouble(),
      temperatureMax: json['temperature_max']?.toDouble(),
      temperatureMin: json['temperature_min']?.toDouble(),
      tdsAvg: json['tds_avg']?.toDouble(),
      tdsMax: json['tds_max']?.toDouble(),
      tdsMin: json['tds_min']?.toDouble(),
      turbidityAvg: json['turbidity_avg']?.toDouble(),
      turbidityMax: json['turbidity_max']?.toDouble(),
      turbidityMin: json['turbidity_min']?.toDouble(),
    );
  }

  /// Get effective pH value (raw or average)
  double get effectivePh => ph ?? phAvg ?? 0.0;

  /// Get effective temperature value (raw or average)
  double get effectiveTemperature => temperature ?? temperatureAvg ?? 0.0;

  /// Get effective TDS value (raw or average)
  double get effectiveTds => tds ?? tdsAvg ?? 0.0;

  /// Get effective turbidity value (raw or average)
  double get effectiveTurbidity => turbidity ?? turbidityAvg ?? 0.0;
}

/// Model class representing history response from API.
class HistoryResponse {
  final String range;
  final int value;
  final List<HistoryDataPoint> data;
  final bool aggregated;

  HistoryResponse({
    required this.range,
    required this.value,
    required this.data,
    required this.aggregated,
  });

  factory HistoryResponse.fromJson(Map<String, dynamic> json) {
    return HistoryResponse(
      range: json['range'] ?? '',
      value: json['value'] ?? 0,
      data: (json['data'] as List<dynamic>?)
              ?.map((e) => HistoryDataPoint.fromJson(e))
              .toList() ??
          [],
      aggregated: json['aggregated'] ?? false,
    );
  }

  factory HistoryResponse.empty() {
    return HistoryResponse(
      range: '',
      value: 0,
      data: [],
      aggregated: false,
    );
  }
}
