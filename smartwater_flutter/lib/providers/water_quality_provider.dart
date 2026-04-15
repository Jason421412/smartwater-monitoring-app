import 'dart:async';
import 'package:flutter/foundation.dart';
import '../config/app_config.dart';
import '../models/water_quality_data.dart';
import '../models/history_data.dart';
import '../services/api_service.dart';

/// Provider for managing water quality data state.
class WaterQualityProvider with ChangeNotifier {
  final ApiService _apiService;
  Timer? _pollingTimer;
  
  WaterQualityData _currentData = WaterQualityData.empty();
  HistoryResponse _historyData = HistoryResponse.empty();
  bool _isLoading = false;
  bool _isHistoryLoading = false;
  String? _error;
  int _consecutiveFailures = 0;
  String _selectedRange = 'hour';
  int _selectedValue = 24;
  String _selectedParam = 'ph';

  WaterQualityProvider({ApiService? apiService})
      : _apiService = apiService ?? ApiService();

  // Getters
  WaterQualityData get currentData => _currentData;
  HistoryResponse get historyData => _historyData;
  bool get isLoading => _isLoading;
  bool get isHistoryLoading => _isHistoryLoading;
  String? get error => _error;
  String get selectedRange => _selectedRange;
  int get selectedValue => _selectedValue;
  String get selectedParam => _selectedParam;

  /// Start polling for latest data
  void startPolling() {
    // Fetch immediately
    fetchLatestData();
    
    // Setup periodic polling
    _pollingTimer?.cancel();
    _pollingTimer = Timer.periodic(
      Duration(seconds: AppConfig.pollingIntervalSeconds),
      (_) => fetchLatestData(),
    );
  }

  /// Stop polling
  void stopPolling() {
    _pollingTimer?.cancel();
    _pollingTimer = null;
  }

  /// Fetch the latest water quality data
  Future<void> fetchLatestData() async {
    _isLoading = true;
    notifyListeners();
    
    try {
      _error = null;
      final data = await _apiService.fetchLatestData();
      // Only update if we got valid data (not empty/offline from network error)
      if (data.deviceId.isNotEmpty) {
        _currentData = data;
        _consecutiveFailures = 0;
      } else {
        // API returned empty data, increment failure counter
        _consecutiveFailures++;
        // Only show offline after multiple consecutive failures
        if (_consecutiveFailures >= 3) {
          _currentData = data;
        }
      }
    } catch (e) {
      _error = e.toString();
      _consecutiveFailures++;
      // Only show offline after multiple consecutive failures
      if (_consecutiveFailures >= 3) {
        _currentData = WaterQualityData.empty();
      }
    }
    
    _isLoading = false;
    notifyListeners();
  }

  /// Fetch historical data with current settings
  Future<void> fetchHistoryData() async {
    _isHistoryLoading = true;
    _error = null;
    notifyListeners();

    try {
      final data = await _apiService.fetchHistoryData(
        range: _selectedRange,
        value: _selectedValue,
        params: _selectedParam,
      );
      _historyData = data;
    } catch (e) {
      _error = e.toString();
    }

    _isHistoryLoading = false;
    notifyListeners();
  }

  /// Update selected time range
  void setTimeRange(String range, int value) {
    _selectedRange = range;
    _selectedValue = value;
    fetchHistoryData();
  }

  /// Update selected parameter
  void setSelectedParam(String param) {
    _selectedParam = param;
    notifyListeners();
  }

  @override
  void dispose() {
    stopPolling();
    _apiService.dispose();
    super.dispose();
  }
}
