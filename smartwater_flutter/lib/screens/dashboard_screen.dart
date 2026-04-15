import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';

import '../providers/water_quality_provider.dart';
import '../theme/app_theme.dart';
import '../widgets/status_banner.dart';
import '../widgets/parameter_card.dart';
import '../widgets/glass_card.dart';
import '../widgets/tech_background.dart';

class DashboardScreen extends StatefulWidget {
  const DashboardScreen({super.key});

  @override
  State<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends State<DashboardScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<WaterQualityProvider>().startPolling();
    });
  }

  // 如果你的 provider 有 stopPolling()，建议加上更省资源
  // @override
  // void dispose() {
  //   context.read<WaterQualityProvider>().stopPolling();
  //   super.dispose();
  // }

  void _backToNative() {
    // ✅ 直接关闭 FlutterActivity，回到 Android 原生页面
    SystemNavigator.pop();
  }

  @override
  Widget build(BuildContext context) {
    return Consumer<WaterQualityProvider>(
      builder: (context, provider, child) {
        final data = provider.currentData;
        final status = data.status;

        return PopScope(
          canPop: false, // ✅ 拦截系统返回键/手势
          onPopInvoked: (didPop) {
            _backToNative();
          },
          child: Scaffold(
            extendBodyBehindAppBar: true,
            appBar: AppBar(
              backgroundColor: Colors.transparent,
              elevation: 0,
              leading: IconButton(
                tooltip: 'Back to Native Dashboard',
                icon: const Icon(Icons.arrow_back),
                onPressed: _backToNative, // ✅ 关键：不要 Navigator.pop
              ),
              title: Text(
                'Advanced Dashboard',
                style: Theme.of(context).textTheme.titleMedium,
              ),
            ),
            body: TechBackground(
              child: SafeArea(
                child: RefreshIndicator(
                  onRefresh: () => provider.fetchLatestData(),
                  color: AppTheme.primaryColor,
                  backgroundColor: AppTheme.surfaceColor,
                  child: SingleChildScrollView(
                    physics: const AlwaysScrollableScrollPhysics(),
                    child: Padding(
                      padding: const EdgeInsets.symmetric(vertical: 16),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Padding(
                            padding: const EdgeInsets.symmetric(horizontal: 20),
                            child: Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text(
                                      'SYSTEM MONITOR',
                                      style: Theme.of(context)
                                          .textTheme
                                          .titleLarge
                                          ?.copyWith(
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
                                          color: AppTheme.accentColor,
                                        ),
                                        const SizedBox(width: 8),
                                        Text(
                                          'REAL-TIME DATA FEED',
                                          style: Theme.of(context)
                                              .textTheme
                                              .labelSmall
                                              ?.copyWith(
                                            color: AppTheme.textSecondary,
                                            letterSpacing: 1.5,
                                          ),
                                        ),
                                      ],
                                    ),
                                  ],
                                ),
                                _buildLiveIndicator(provider),
                              ],
                            ),
                          ),
                          const SizedBox(height: 24),

                          StatusBanner(
                            status: status,
                            deviceId: data.deviceId,
                          ),
                          const SizedBox(height: 24),

                          Padding(
                            padding: const EdgeInsets.symmetric(horizontal: 10),
                            child: GridView.count(
                              crossAxisCount: 2,
                              shrinkWrap: true,
                              physics: const NeverScrollableScrollPhysics(),
                              childAspectRatio: 1.1,
                              mainAxisSpacing: 10,
                              crossAxisSpacing: 10,
                              children: [
                                ParameterCard(
                                  title: 'TEMP',
                                  value: data.isOnline
                                      ? data.temperature.toStringAsFixed(1)
                                      : '--',
                                  unit: '°C',
                                  icon: Icons.thermostat_outlined,
                                  status: status,
                                  iconColor: data.isOnline
                                      ? AppTheme.primaryColor
                                      : AppTheme.offlineColor,
                                ),
                                ParameterCard(
                                  title: 'pH',
                                  value: data.isOnline
                                      ? data.ph.toStringAsFixed(2)
                                      : '--',
                                  unit: '',
                                  icon: Icons.science_outlined,
                                  status: data.isOnline
                                      ? _getPhStatus(data.ph)
                                      : 'offline',
                                  iconColor: data.isOnline
                                      ? _getPhColor(data.ph)
                                      : AppTheme.offlineColor,
                                ),
                                ParameterCard(
                                  title: 'Turbidity',
                                  value: data.isOnline
                                      ? data.turbidity.toStringAsFixed(1)
                                      : '--',
                                  unit: 'NTU',
                                  icon: Icons.water_drop_outlined,
                                  status: status,
                                  iconColor: data.isOnline
                                      ? AppTheme.secondaryColor
                                      : AppTheme.offlineColor,
                                ),
                                ParameterCard(
                                  title: 'TDS',
                                  value: data.isOnline
                                      ? data.tds.toStringAsFixed(0)
                                      : '--',
                                  unit: 'ppm',
                                  icon: Icons.bubble_chart_outlined,
                                  status: data.isOnline
                                      ? _getTdsStatus(data.tds)
                                      : 'offline',
                                  iconColor: data.isOnline
                                      ? _getTdsColor(data.tds)
                                      : AppTheme.offlineColor,
                                ),
                              ],
                            ),
                          ),
                          const SizedBox(height: 16),

                          _buildLastUpdatedInfo(data.timestamp),
                          const SizedBox(height: 16),
                        ],
                      ),
                    ),
                  ),
                ),
              ),
            ),
          ),
        );
      },
    );
  }

  Widget _buildLiveIndicator(WaterQualityProvider provider) {
    final isOnline = provider.currentData.isOnline;

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      decoration: BoxDecoration(
        color: isOnline
            ? AppTheme.greatColor.withOpacity(0.2)
            : AppTheme.offlineColor.withOpacity(0.2),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(
          color: isOnline
              ? AppTheme.greatColor.withOpacity(0.5)
              : AppTheme.offlineColor.withOpacity(0.5),
        ),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          _PulsingDot(isOnline: isOnline),
          const SizedBox(width: 8),
          Text(
            isOnline ? 'LIVE' : 'OFFLINE',
            style: TextStyle(
              color: isOnline ? AppTheme.greatColor : AppTheme.offlineColor,
              fontWeight: FontWeight.bold,
              fontSize: 12,
              letterSpacing: 1,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildLastUpdatedInfo(String timestamp) {
    String displayTime = 'Never';
    if (timestamp.isNotEmpty) {
      try {
        final dt = DateTime.parse(timestamp);
        displayTime =
        '${dt.hour.toString().padLeft(2, '0')}:${dt.minute.toString().padLeft(2, '0')}:${dt.second.toString().padLeft(2, '0')}';
      } catch (_) {
        displayTime = timestamp;
      }
    }

    return Center(
      child: GlassCard(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        margin: const EdgeInsets.symmetric(horizontal: 20),
        borderRadius: 12,
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(Icons.access_time_rounded, size: 16, color: AppTheme.textMuted),
            const SizedBox(width: 8),
            Text(
              'Last Updated: $displayTime',
              style: TextStyle(color: AppTheme.textMuted, fontSize: 12),
            ),
          ],
        ),
      ),
    );
  }

  String _getPhStatus(double ph) => (ph >= 6.5 && ph <= 8.5) ? 'great' : 'warning';
  Color _getPhColor(double ph) => (ph >= 6.5 && ph <= 8.5) ? AppTheme.greatColor : AppTheme.warningColor;

  String _getTdsStatus(double tds) => (tds < 300) ? 'great' : 'warning';
  Color _getTdsColor(double tds) => (tds < 300) ? AppTheme.accentColor : AppTheme.warningColor;
}

class _PulsingDot extends StatefulWidget {
  final bool isOnline;
  const _PulsingDot({required this.isOnline});

  @override
  State<_PulsingDot> createState() => _PulsingDotState();
}

class _PulsingDotState extends State<_PulsingDot> with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _animation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(duration: const Duration(milliseconds: 1000), vsync: this);
    _animation = Tween<double>(begin: 0.4, end: 1.0).animate(
      CurvedAnimation(parent: _controller, curve: Curves.easeInOut),
    );
    if (widget.isOnline) _controller.repeat(reverse: true);
  }

  @override
  void didUpdateWidget(_PulsingDot oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.isOnline && !_controller.isAnimating) {
      _controller.repeat(reverse: true);
    } else if (!widget.isOnline && _controller.isAnimating) {
      _controller.stop();
      _controller.value = 1.0;
    }
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final color = widget.isOnline ? AppTheme.greatColor : AppTheme.offlineColor;

    return AnimatedBuilder(
      animation: _animation,
      builder: (context, child) {
        return Container(
          width: 8,
          height: 8,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            color: color,
            boxShadow: widget.isOnline
                ? [
              BoxShadow(
                color: color.withOpacity(_animation.value * 0.6),
                blurRadius: 6 * _animation.value,
                spreadRadius: 2 * _animation.value,
              ),
            ]
                : null,
          ),
        );
      },
    );
  }
}
