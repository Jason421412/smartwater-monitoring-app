import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:fl_chart/fl_chart.dart';
import '../providers/water_quality_provider.dart';
import '../theme/app_theme.dart';
import '../widgets/glass_card.dart';
import '../models/history_data.dart';
import '../widgets/tech_background.dart';

/// History screen showing water quality trends over time.
class HistoryScreen extends StatefulWidget {
  const HistoryScreen({super.key});

  @override
  State<HistoryScreen> createState() => _HistoryScreenState();
}

class _HistoryScreenState extends State<HistoryScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<WaterQualityProvider>().fetchHistoryData();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Consumer<WaterQualityProvider>(
      builder: (context, provider, child) {
        return TechBackground(
          child: SafeArea(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Header
                  Text(
                    'HISTORICAL DATA',
                    style: Theme.of(context).textTheme.titleLarge?.copyWith(
                          color: AppTheme.primaryColor,
                          fontWeight: FontWeight.w900,
                          letterSpacing: 2.0,
                        ),
                  ),
                  const SizedBox(height: 4),
                  Row(
                    children: [
                      Container(
                        width: 8,
                        height: 8,
                        color: AppTheme.secondaryColor,
                      ),
                      const SizedBox(width: 8),
                      Text(
                        'TREND ANALYSIS SYSTEM',
                        style: Theme.of(context).textTheme.labelSmall?.copyWith(
                              color: AppTheme.textSecondary,
                              letterSpacing: 1.5,
                            ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 24),

                  // Time range selector
                  _buildTimeRangeSelector(provider),
                  const SizedBox(height: 16),

                  // Parameter selector
                  _buildParameterSelector(provider),
                  const SizedBox(height: 20),

                  // Chart
                  _buildChart(provider),
                  const SizedBox(height: 16),

                  // Data summary
                  if (provider.historyData.data.isNotEmpty)
                    _buildDataSummary(provider),
                ],
              ),
            ),
          ),
        );
      },
    );
  }

  Widget _buildTimeRangeSelector(WaterQualityProvider provider) {
    final timeRanges = [
      {'label': '24H', 'range': 'hour', 'value': 24},
      {'label': '7D', 'range': 'day', 'value': 7},
      {'label': '30D', 'range': 'day', 'value': 30},
      {'label': '3M', 'range': 'month', 'value': 3},
      {'label': '1Y', 'range': 'year', 'value': 1},
    ];

    return GlassCard(
      padding: const EdgeInsets.symmetric(vertical: 8),
      margin: EdgeInsets.zero,
      child: SingleChildScrollView(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 8),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: timeRanges.map((item) {
            final isSelected = provider.selectedRange == item['range'] &&
                provider.selectedValue == item['value'];
            return Padding(
              padding: const EdgeInsets.symmetric(horizontal: 4),
              child: _buildTimeRangeButton(
                label: item['label'] as String,
                isSelected: isSelected,
                onTap: () => provider.setTimeRange(
                  item['range'] as String,
                  item['value'] as int,
                ),
              ),
            );
          }).toList(),
        ),
      ),
    );
  }

  Widget _buildTimeRangeButton({
    required String label,
    required bool isSelected,
    required VoidCallback onTap,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        decoration: BoxDecoration(
          gradient: isSelected ? AppTheme.primaryGradient : null,
          color: isSelected ? null : Colors.transparent,
          borderRadius: BorderRadius.circular(12),
        ),
        child: Text(
          label,
          style: TextStyle(
            color: isSelected ? Colors.white : AppTheme.textSecondary,
            fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
            fontSize: 14,
          ),
        ),
      ),
    );
  }

  Widget _buildParameterSelector(WaterQualityProvider provider) {
    final params = [
      {'label': 'pH', 'value': 'ph', 'color': AppTheme.greatColor},
      {'label': 'Temperature', 'value': 'temperature', 'color': AppTheme.primaryColor},
      {'label': 'TDS', 'value': 'tds', 'color': AppTheme.accentColor},
      {'label': 'Turbidity', 'value': 'turbidity', 'color': AppTheme.secondaryColor},
    ];

    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      child: Row(
        children: params.map((param) {
          final isSelected = provider.selectedParam == param['value'];
          return Padding(
            padding: const EdgeInsets.only(right: 8),
            child: GestureDetector(
              onTap: () => provider.setSelectedParam(param['value'] as String),
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
                decoration: BoxDecoration(
                  color: isSelected
                      ? (param['color'] as Color).withOpacity(0.2)
                      : AppTheme.surfaceColor,
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(
                    color: isSelected
                        ? param['color'] as Color
                        : AppTheme.glassBorder,
                  ),
                ),
                child: Text(
                  param['label'] as String,
                  style: TextStyle(
                    color: isSelected
                        ? param['color'] as Color
                        : AppTheme.textSecondary,
                    fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                  ),
                ),
              ),
            ),
          );
        }).toList(),
      ),
    );
  }

  String _getRangeLabel(String range, int value) {
    switch (range) {
      case 'hour':
        return 'Last $value Hours';
      case 'day':
        return 'Last $value Days';
      case 'month':
        return 'Last $value Months';
      case 'year':
        return 'Last $value Year${value > 1 ? 's' : ''}';
      default:
        return '';
    }
  }

  Widget _buildChart(WaterQualityProvider provider) {
    if (provider.isHistoryLoading) {
      return GlassCard(
        padding: const EdgeInsets.all(40),
        margin: EdgeInsets.zero,
        child: const Center(
          child: CircularProgressIndicator(
            color: AppTheme.primaryColor,
          ),
        ),
      );
    }

    final data = provider.historyData.data;
    final rangeLabel = _getRangeLabel(provider.selectedRange, provider.selectedValue);
    
    // Need at least 2 data points to draw a meaningful line chart
    if (data.length < 2) {
      return GlassCard(
        padding: const EdgeInsets.all(40),
        margin: EdgeInsets.zero,
        child: Center(
          child: Column(
            children: [
              Icon(
                Icons.show_chart_rounded,
                size: 48,
                color: AppTheme.textMuted,
              ),
              const SizedBox(height: 16),
              Text(
                'No data available',
                style: TextStyle(color: AppTheme.textMuted),
              ),
              const SizedBox(height: 8),
              Text(
                rangeLabel,
                style: TextStyle(color: AppTheme.textMuted, fontSize: 12),
              ),
            ],
          ),
        ),
      );
    }

    return GlassCard(
      padding: const EdgeInsets.all(16),
      margin: EdgeInsets.zero,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                rangeLabel,
                style: const TextStyle(
                  color: AppTheme.textSecondary,
                  fontSize: 12,
                  fontWeight: FontWeight.w500,
                ),
              ),
              Text(
                '${data.length} data points',
                style: TextStyle(
                  color: AppTheme.textMuted,
                  fontSize: 11,
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          SizedBox(
            height: 220,
            child: InteractiveViewer(
              panEnabled: true,
              scaleEnabled: true,
              minScale: 1.0,
              maxScale: 4.0,
              child: _buildLineChart(data, provider.selectedParam),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildLineChart(List<HistoryDataPoint> data, String param) {
    final spots = <FlSpot>[];
    double minY = double.infinity;
    double maxY = double.negativeInfinity;

    for (int i = 0; i < data.length; i++) {
      double value;
      switch (param) {
        case 'ph':
          value = data[i].effectivePh;
          break;
        case 'temperature':
          value = data[i].effectiveTemperature;
          break;
        case 'tds':
          value = data[i].effectiveTds;
          break;
        case 'turbidity':
          value = data[i].effectiveTurbidity;
          break;
        default:
          value = 0;
      }

      if (value != 0) {
        spots.add(FlSpot(i.toDouble(), value));
        if (value < minY) minY = value;
        if (value > maxY) maxY = value;
      }
    }

    if (spots.isEmpty) {
      return const Center(
        child: Text('No data points', style: TextStyle(color: AppTheme.textMuted)),
      );
    }

    // Add padding to y-axis range, ensure non-zero range
    final yRange = maxY - minY;
    final yPadding = yRange > 0 ? yRange * 0.1 : 1.0;
    minY -= yPadding;
    maxY += yPadding;

    final Color lineColor;
    switch (param) {
      case 'ph':
        lineColor = AppTheme.greatColor;
        break;
      case 'temperature':
        lineColor = AppTheme.primaryColor;
        break;
      case 'tds':
        lineColor = AppTheme.accentColor;
        break;
      case 'turbidity':
        lineColor = AppTheme.secondaryColor;
        break;
      default:
        lineColor = AppTheme.primaryColor;
    }

    return LineChart(
      LineChartData(
        gridData: FlGridData(
          show: true,
          drawVerticalLine: false,
          horizontalInterval: ((maxY - minY) / 4).clamp(0.1, double.infinity),
          getDrawingHorizontalLine: (value) {
            return FlLine(
              color: AppTheme.glassBorder,
              strokeWidth: 1,
            );
          },
        ),
        titlesData: FlTitlesData(
          show: true,
          rightTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
          topTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
          bottomTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
          leftTitles: AxisTitles(
            sideTitles: SideTitles(
              showTitles: true,
              reservedSize: 40,
              getTitlesWidget: (value, meta) {
                return Text(
                  value.toStringAsFixed(1),
                  style: const TextStyle(
                    color: AppTheme.textMuted,
                    fontSize: 10,
                  ),
                );
              },
            ),
          ),
        ),
        borderData: FlBorderData(show: false),
        minX: 0,
        maxX: (spots.length - 1).toDouble(),
        minY: minY,
        maxY: maxY,
        lineBarsData: [
          LineChartBarData(
            spots: spots,
            isCurved: true,
            color: lineColor,
            barWidth: 2,
            isStrokeCapRound: true,
            dotData: FlDotData(
              show: spots.length < 50,
              getDotPainter: (spot, percent, barData, index) {
                return FlDotCirclePainter(
                  radius: 3,
                  color: lineColor,
                  strokeWidth: 1,
                  strokeColor: Colors.white,
                );
              },
            ),
            belowBarData: BarAreaData(
              show: true,
              gradient: LinearGradient(
                colors: [
                  lineColor.withOpacity(0.3),
                  lineColor.withOpacity(0.0),
                ],
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
              ),
            ),
          ),
        ],
        lineTouchData: LineTouchData(
          touchTooltipData: LineTouchTooltipData(
            tooltipBgColor: AppTheme.surfaceColor,
            getTooltipItems: (touchedSpots) {
              return touchedSpots.map((spot) {
                return LineTooltipItem(
                  spot.y.toStringAsFixed(2),
                  TextStyle(color: lineColor, fontWeight: FontWeight.bold),
                );
              }).toList();
            },
          ),
        ),
      ),
    );
  }

  Widget _buildDataSummary(WaterQualityProvider provider) {
    final data = provider.historyData.data;
    final param = provider.selectedParam;

    List<double> values = [];
    for (var point in data) {
      double value;
      switch (param) {
        case 'ph':
          value = point.effectivePh;
          break;
        case 'temperature':
          value = point.effectiveTemperature;
          break;
        case 'tds':
          value = point.effectiveTds;
          break;
        case 'turbidity':
          value = point.effectiveTurbidity;
          break;
        default:
          value = 0;
      }
      if (value != 0) values.add(value);
    }

    if (values.isEmpty) return const SizedBox();

    final avg = values.reduce((a, b) => a + b) / values.length;
    final max = values.reduce((a, b) => a > b ? a : b);
    final min = values.reduce((a, b) => a < b ? a : b);

    return GlassCard(
      padding: const EdgeInsets.all(16),
      margin: EdgeInsets.zero,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceAround,
        children: [
          _buildSummaryItem('Min', min.toStringAsFixed(2), AppTheme.primaryColor),
          _buildSummaryItem('Avg', avg.toStringAsFixed(2), AppTheme.greatColor),
          _buildSummaryItem('Max', max.toStringAsFixed(2), AppTheme.warningColor),
        ],
      ),
    );
  }

  Widget _buildSummaryItem(String label, String value, Color color) {
    return Column(
      children: [
        Text(
          label,
          style: TextStyle(
            color: AppTheme.textMuted,
            fontSize: 12,
          ),
        ),
        const SizedBox(height: 4),
        Text(
          value,
          style: TextStyle(
            color: color,
            fontSize: 18,
            fontWeight: FontWeight.bold,
          ),
        ),
      ],
    );
  }
}
