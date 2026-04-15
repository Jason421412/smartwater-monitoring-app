import 'package:flutter/material.dart';
import 'dart:math' as math;
import '../theme/app_theme.dart';

/// A banner widget displaying the current water quality status with animated effects.
class StatusBanner extends StatefulWidget {
  final String status;
  final String deviceId;

  const StatusBanner({
    super.key,
    required this.status,
    required this.deviceId,
  });

  @override
  State<StatusBanner> createState() => _StatusBannerState();
}

class _StatusBannerState extends State<StatusBanner>
    with TickerProviderStateMixin {
  late AnimationController _shimmerController;
  late AnimationController _glowController;
  late Animation<double> _glowAnimation;

  @override
  void initState() {
    super.initState();
    _shimmerController = AnimationController(
      duration: const Duration(milliseconds: 2500),
      vsync: this,
    )..repeat();

    _glowController = AnimationController(
      duration: const Duration(milliseconds: 1500),
      vsync: this,
    )..repeat(reverse: true);

    _glowAnimation = Tween<double>(begin: 0.4, end: 0.8).animate(
      CurvedAnimation(parent: _glowController, curve: Curves.easeInOut),
    );
  }

  @override
  void dispose() {
    _shimmerController.dispose();
    _glowController.dispose();
    super.dispose();
  }

  String get _statusText {
    switch (widget.status.toLowerCase()) {
      case 'great':
      case 'excellent':
        return 'GREAT';
      case 'warning':
        return 'WARNING';
      case 'offline':
        return 'OFFLINE';
      default:
        return 'UNKNOWN';
    }
  }

  String get _statusSubtext {
    switch (widget.status.toLowerCase()) {
      case 'great':
      case 'excellent':
        return 'Water quality is optimal';
      case 'warning':
        return 'Parameters need attention';
      case 'offline':
        return 'Device not responding';
      default:
        return '';
    }
  }

  IconData get _statusIcon {
    switch (widget.status.toLowerCase()) {
      case 'great':
      case 'excellent':
        return Icons.verified_rounded;
      case 'warning':
        return Icons.warning_amber_rounded;
      case 'offline':
        return Icons.cloud_off_rounded;
      default:
        return Icons.help_outline_rounded;
    }
  }

  @override
  Widget build(BuildContext context) {
    final statusColor = AppTheme.getStatusColor(widget.status);
    final gradient = AppTheme.getStatusGradient(widget.status);

    return AnimatedBuilder(
      animation: _glowAnimation,
      builder: (context, child) {
        return Container(
          margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          decoration: BoxDecoration(
            color: AppTheme.cardColor.withOpacity(0.5),
            borderRadius: BorderRadius.circular(4),
            border: Border.all(
              color: statusColor.withOpacity(0.5),
              width: 1,
            ),
            boxShadow: [
              BoxShadow(
                color: statusColor.withOpacity(_glowAnimation.value * 0.2),
                blurRadius: 20,
                spreadRadius: -2,
              ),
            ],
          ),
          child: ClipRRect(
            borderRadius: BorderRadius.circular(4),
            child: Stack(
              children: [
                // Background Tech Pattern (Stripes)
                Positioned.fill(
                  child: CustomPaint(
                    painter: _StripesPainter(
                      color: statusColor.withOpacity(0.05),
                    ),
                  ),
                ),
                
                // Main Content
                Container(
                  padding: const EdgeInsets.all(20),
                  child: Row(
                    children: [
                      // Animated icon container
                      _AnimatedIconContainer(
                        icon: _statusIcon,
                        color: statusColor,
                        gradient: gradient,
                      ),
                      const SizedBox(width: 20),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            // Status text with shimmer effect
                            _ShimmerText(
                              text: _statusText,
                              color: statusColor,
                              controller: _shimmerController,
                            ),
                            const SizedBox(height: 6),
                            Text(
                              _statusSubtext.toUpperCase(),
                              style: Theme.of(context).textTheme.labelSmall?.copyWith(
                                    color: AppTheme.textSecondary,
                                    letterSpacing: 1.0,
                                    fontSize: 10,
                                  ),
                            ),
                            const SizedBox(height: 10),
                            Container(
                              padding: const EdgeInsets.symmetric(
                                horizontal: 8,
                                vertical: 4,
                              ),
                              decoration: BoxDecoration(
                                color: AppTheme.surfaceColor,
                                border: Border.all(
                                  color: AppTheme.gridLineColor,
                                ),
                                borderRadius: BorderRadius.circular(2),
                              ),
                              child: Row(
                                mainAxisSize: MainAxisSize.min,
                                children: [
                                  Icon(
                                    Icons.hub_outlined, // More techy than device_hub_rounded
                                    size: 12,
                                    color: AppTheme.textMuted,
                                  ),
                                  const SizedBox(width: 6),
                                  Text(
                                    widget.deviceId.isEmpty
                                        ? 'NO_DEVICE'
                                        : widget.deviceId,
                                    style: Theme.of(context)
                                        .textTheme
                                        .labelSmall
                                        ?.copyWith(
                                          color: AppTheme.textMuted,
                                          fontFamily: 'monospace',
                                          letterSpacing: 1.0,
                                        ),
                                  ),
                                ],
                              ),
                            ),
                          ],
                        ),
                      ),
                      // Signal bars indicator
                      if (widget.status != 'offline')
                        _SignalBars(color: statusColor),
                    ],
                  ),
                ),
                // Corner Accents
                Positioned(
                  top: 0,
                  right: 0,
                  child: _CornerTriangle(color: statusColor),
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}

class _StripesPainter extends CustomPainter {
  final Color color;
  
  _StripesPainter({required this.color});
  
  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color
      ..strokeWidth = 2;
      
    for (double i = -size.height; i < size.width; i += 10) {
      canvas.drawLine(
        Offset(i, size.height),
        Offset(i + size.height, 0),
        paint,
      );
    }
  }
  
  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

class _CornerTriangle extends StatelessWidget {
  final Color color;
  
  const _CornerTriangle({required this.color});
  
  @override
  Widget build(BuildContext context) {
    return CustomPaint(
      size: const Size(20, 20),
      painter: _TrianglePainter(color: color.withOpacity(0.8)),
    );
  }
}

class _TrianglePainter extends CustomPainter {
  final Color color;
  
  _TrianglePainter({required this.color});
  
  @override
  void paint(Canvas canvas, Size size) {
    final path = Path()
      ..moveTo(size.width, 0)
      ..lineTo(size.width, size.height)
      ..lineTo(0, 0)
      ..close();
      
    canvas.drawPath(path, Paint()..color = color);
  }
  
  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

/// Animated icon container with rotating border
class _AnimatedIconContainer extends StatefulWidget {
  final IconData icon;
  final Color color;
  final LinearGradient gradient;

  const _AnimatedIconContainer({
    required this.icon,
    required this.color,
    required this.gradient,
  });

  @override
  State<_AnimatedIconContainer> createState() => _AnimatedIconContainerState();
}

class _AnimatedIconContainerState extends State<_AnimatedIconContainer>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(seconds: 4),
      vsync: this,
    )..repeat();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _controller,
      builder: (context, child) {
        return Container(
          padding: const EdgeInsets.all(3),
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            gradient: SweepGradient(
              colors: [
                widget.color.withOpacity(0.8),
                widget.color.withOpacity(0.1),
                widget.color.withOpacity(0.8),
              ],
              stops: const [0.0, 0.5, 1.0],
              transform: GradientRotation(_controller.value * 2 * math.pi),
            ),
          ),
          child: Container(
            padding: const EdgeInsets.all(14),
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: AppTheme.surfaceColor,
              boxShadow: [
                BoxShadow(
                  color: widget.color.withOpacity(0.3),
                  blurRadius: 12,
                  spreadRadius: -2,
                ),
              ],
            ),
            child: Icon(
              widget.icon,
              color: widget.color,
              size: 28,
            ),
          ),
        );
      },
    );
  }
}

/// Shimmer text effect
class _ShimmerText extends StatelessWidget {
  final String text;
  final Color color;
  final AnimationController controller;

  const _ShimmerText({
    required this.text,
    required this.color,
    required this.controller,
  });

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: controller,
      builder: (context, child) {
        return ShaderMask(
          shaderCallback: (bounds) {
            return LinearGradient(
              colors: [
                color,
                Colors.white,
                color,
              ],
              stops: [
                (controller.value - 0.3).clamp(0.0, 1.0),
                controller.value,
                (controller.value + 0.3).clamp(0.0, 1.0),
              ],
              begin: Alignment.centerLeft,
              end: Alignment.centerRight,
            ).createShader(bounds);
          },
          child: Text(
            text,
            style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                  letterSpacing: 2,
                ),
          ),
        );
      },
    );
  }
}

/// Signal bars indicator
class _SignalBars extends StatefulWidget {
  final Color color;

  const _SignalBars({required this.color});

  @override
  State<_SignalBars> createState() => _SignalBarsState();
}

class _SignalBarsState extends State<_SignalBars>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 1200),
      vsync: this,
    )..repeat();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _controller,
      builder: (context, child) {
        return Row(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.end,
          children: List.generate(4, (index) {
            final delay = index * 0.15;
            final progress = ((_controller.value - delay) % 1.0).clamp(0.0, 1.0);
            final opacity = 0.3 + (0.7 * math.sin(progress * math.pi));
            
            return Container(
              margin: const EdgeInsets.symmetric(horizontal: 2),
              width: 4,
              height: 8.0 + (index * 5.0),
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(2),
                color: widget.color.withOpacity(opacity),
                boxShadow: [
                  BoxShadow(
                    color: widget.color.withOpacity(opacity * 0.5),
                    blurRadius: 4,
                  ),
                ],
              ),
            );
          }),
        );
      },
    );
  }
}

/// Shimmer overlay effect
class _ShimmerOverlay extends StatelessWidget {
  final AnimationController controller;
  final Color color;

  const _ShimmerOverlay({required this.controller, required this.color});

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: controller,
      builder: (context, child) {
        return Positioned.fill(
          child: IgnorePointer(
            child: Container(
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  colors: [
                    Colors.transparent,
                    color.withOpacity(0.15),
                    Colors.transparent,
                  ],
                  stops: [
                    (controller.value - 0.3).clamp(0.0, 1.0),
                    controller.value,
                    (controller.value + 0.3).clamp(0.0, 1.0),
                  ],
                  begin: Alignment.centerLeft,
                  end: Alignment.centerRight,
                ),
              ),
            ),
          ),
        );
      },
    );
  }
}

/// Animated pulse indicator widget.
class _PulseIndicator extends StatefulWidget {
  final Color color;

  const _PulseIndicator({required this.color});

  @override
  State<_PulseIndicator> createState() => _PulseIndicatorState();
}

class _PulseIndicatorState extends State<_PulseIndicator>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _animation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 1500),
      vsync: this,
    )..repeat(reverse: true);
    _animation = Tween<double>(begin: 0.5, end: 1.0).animate(
      CurvedAnimation(parent: _controller, curve: Curves.easeInOut),
    );
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _animation,
      builder: (context, child) {
        return Container(
          width: 12,
          height: 12,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            color: widget.color,
            boxShadow: [
              BoxShadow(
                color: widget.color.withOpacity(_animation.value * 0.5),
                blurRadius: 10 * _animation.value,
                spreadRadius: 2 * _animation.value,
              ),
            ],
          ),
        );
      },
    );
  }
}
