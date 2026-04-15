import 'dart:convert';
import 'package:http/http.dart' as http;
import '../config/app_config.dart';
import '../models/water_quality_data.dart';
import '../models/history_data.dart';

/// Service class for handling API communication with the backend.
class ApiService {
  final String baseUrl;
  final http.Client _client;

  ApiService({String? baseUrl, http.Client? client})
      : baseUrl = baseUrl ?? AppConfig.baseUrl,
        _client = client ?? http.Client();

  /// Fetch the latest water quality data from the backend.
  Future<WaterQualityData> fetchLatestData({String? deviceId}) async {
    try {
      String url = '$baseUrl${AppConfig.latestEndpoint}';
      if (deviceId != null) {
        url += '?device_id=$deviceId';
      }

      final response = await _client.get(
        Uri.parse(url),
        headers: {'Content-Type': 'application/json'},
      ).timeout(const Duration(seconds: 10));

      if (response.statusCode == 200) {
        final json = jsonDecode(response.body);
        return WaterQualityData.fromJson(json);
      } else if (response.statusCode == 404) {
        return WaterQualityData.empty();
      } else {
        throw Exception('Failed to fetch data: ${response.statusCode}');
      }
    } catch (e) {
      // Return offline status on any error
      return WaterQualityData.empty();
    }
  }

  /// Fetch historical water quality data.
  /// [range] - Time range type: 'hour', 'day', 'month', 'year'
  /// [value] - Time range value (e.g., 24 for last 24 hours)
  /// [params] - Parameters to fetch (default: 'ph,temperature')
  Future<HistoryResponse> fetchHistoryData({
    required String range,
    required int value,
    String params = 'ph,temperature',
    String? deviceId,
  }) async {
    try {
      String url =
          '$baseUrl${AppConfig.historyEndpoint}?range=$range&value=$value&params=$params';
      if (deviceId != null) {
        url += '&device_id=$deviceId';
      }

      final response = await _client.get(
        Uri.parse(url),
        headers: {'Content-Type': 'application/json'},
      ).timeout(const Duration(seconds: 30));

      if (response.statusCode == 200) {
        final json = jsonDecode(response.body);
        return HistoryResponse.fromJson(json);
      } else {
        throw Exception('Failed to fetch history: ${response.statusCode}');
      }
    } catch (e) {
      return HistoryResponse.empty();
    }
  }

  /// Dispose the HTTP client
  void dispose() {
    _client.close();
  }
}
