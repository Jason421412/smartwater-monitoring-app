import 'package:flutter/material.dart';
import 'dart:async';
import 'dart:math' as math;
import '../config/app_config.dart';
import '../theme/app_theme.dart';
import '../widgets/tech_background.dart';
import 'home_screen.dart';

/// Splash screen displayed when the app launches with a Tech Boot sequence.
class SplashScreen extends StatefulWidget {
  const SplashScreen({super.key});

  @override
  State<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends State<SplashScreen>
    with TickerProviderStateMixin {
  late AnimationController _progressController;
  late Animation<double> _progressAnimation;
  
  String _loadingText = "INITIALIZING CORE SYSTEMS...";
  int _loadingStep = 0;
  Timer? _textTimer;

  final List<String> _bootSequence = [
    "LOADING KERNEL...",
    "ESTABLISHING SECURE CONNECTION...",
    "CALIBRATING SENSORS...",
    "LOADING UI MODULES...",
    "SYSTEM READY."
  ];

  @override
  void initState() {
    super.initState();

    // Progress bar animation
    _progressController = AnimationController(
      duration: const Duration(milliseconds: 2500),
      vsync: this,
    );

    _progressAnimation = Tween<double>(begin: 0.0, end: 1.0).animate(
      CurvedAnimation(
        parent: _progressController,
        curve: Curves.easeInOutExpo,
      ),
    );

    _progressController.forward();

    // Text sequence timer
    _textTimer = Timer.periodic(const Duration(milliseconds: 400), (timer) {
      if (_loadingStep < _bootSequence.length) {
        setState(() {
          _loadingText = _bootSequence[_loadingStep];
          _loadingStep++;
        });
      } else {
        timer.cancel();
      }
    });

    // Navigate after delay
    Future.delayed(
      const Duration(milliseconds: 3000), // Slightly longer for effect
      () {
        if (mounted) {
          Navigator.of(context).pushReplacement(
            PageRouteBuilder(
              pageBuilder: (context, animation, secondaryAnimation) =>
                  const HomeScreen(),
              transitionsBuilder:
                  (context, animation, secondaryAnimation, child) {
                return FadeTransition(opacity: animation, child: child);
              },
              transitionDuration: const Duration(milliseconds: 800),
            ),
          );
        }
      },
    );
  }

  @override
  void dispose() {
    _progressController.dispose();
    _textTimer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: TechBackground(
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              // Logo Stack
              Stack(
                alignment: Alignment.center,
                children: [
                  // Spinning outer ring
                  _RotatingTechRing(
                    color: AppTheme.secondaryColor,
                    size: 160,
                    duration: const Duration(seconds: 10),
                    clockwise: true,
                  ),
                  // Spinning inner ring
                  _RotatingTechRing(
                    color: AppTheme.primaryColor,
                    size: 120,
                    duration: const Duration(seconds: 5),
                    clockwise: false,
                  ),
                  // Center Icon
                  ShaderMask(
                    shaderCallback: (bounds) => AppTheme.primaryGradient.createShader(bounds),
                    child: const Icon(
                      Icons.water_drop, // Simple solid icon for mask
                      size: 60,
                      color: Colors.white,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 60),
              
              // App Title (Glitchy Text Effect)
              Column(
                children: [
                  Text(
                    'AQUA MONITOR',
                    style: Theme.of(context).textTheme.headlineLarge?.copyWith(
                      fontFamily: 'monospace',
                      letterSpacing: 4.0,
                      color: Colors.white,
                      shadows: [
                        Shadow(
                          color: AppTheme.primaryColor.withOpacity(0.8),
                          blurRadius: 10,
                          offset: const Offset(0, 0),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'INTELLIGENT WATER ANALYSIS SYSTEM',
                    style: Theme.of(context).textTheme.labelSmall?.copyWith(
                      color: AppTheme.textSecondary,
                      letterSpacing: 2.0,
                    ),
                  ),
                ],
              ),
              
              const SizedBox(height: 80),

              // Loading Bar & Text
              SizedBox(
                width: 250,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // Boot Text
                    Text(
                      _loadingText,
                      style: const TextStyle(
                        color: AppTheme.primaryColor,
                        fontFamily: 'monospace',
                        fontSize: 10,
                      ),
                    ),
                    const SizedBox(height: 8),
                    // Progress Bar Container
                    AnimatedBuilder(
                      animation: _progressAnimation,
                      builder: (context, child) {
                        return Container(
                          height: 4,
                          width: double.infinity,
                          decoration: BoxDecoration(
                            color: AppTheme.surfaceColor,
                            borderRadius: BorderRadius.circular(2),
                          ),
                          child: FractionallySizedBox(
                            alignment: Alignment.centerLeft,
                            widthFactor: _progressAnimation.value,
                            child: Container(
                              decoration: BoxDecoration(
                                gradient: AppTheme.primaryGradient,
                                borderRadius: BorderRadius.circular(2),
                                boxShadow: [
                                  BoxShadow(
                                    color: AppTheme.primaryColor.withOpacity(0.5),
                                    blurRadius: 6,
                                    spreadRadius: 1,
                                  ),
                                ],
                              ),
                            ),
                          ),
                        );
                      },
                    ),
                    const SizedBox(height: 4),
                    // Percentage
                    AnimatedBuilder(
                      animation: _progressAnimation,
                      builder: (context, child) {
                        final percent = (_progressAnimation.value * 100).toInt();
                        return Align(
                          alignment: Alignment.centerRight,
                          child: Text(
                            '$percent%',
                            style: TextStyle(
                              color: AppTheme.textMuted,
                              fontFamily: 'monospace',
                              fontSize: 10,
                            ),
                          ),
                        );
                      },
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _RotatingTechRing extends StatefulWidget {
  final Color color;
  final double size;
  final Duration duration;
  final bool clockwise;

  const _RotatingTechRing({
    required this.color,
    required this.size,
    required this.duration,
    required this.clockwise,
  });

  @override
  State<_RotatingTechRing> createState() => _RotatingTechRingState();
}

class _RotatingTechRingState extends State<_RotatingTechRing>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: widget.duration,
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
        final angle = _controller.value * 2 * math.pi;
        return Transform.rotate(
          angle: widget.clockwise ? angle : -angle,
          child: CustomPaint(
            size: Size(widget.size, widget.size),
            painter: _TechRingPainter(color: widget.color),
          ),
        );
      },
    );
  }
}

class _TechRingPainter extends CustomPainter {
  final Color color;

  _TechRingPainter({required this.color});

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final radius = size.width / 2;
    final paint = Paint()
      ..color = color.withOpacity(0.3)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.5;

    // Draw main circle segments
    final rect = Rect.fromCircle(center: center, radius: radius);
    canvas.drawArc(rect, 0, math.pi / 2, false, paint);
    canvas.drawArc(rect, math.pi, math.pi / 2, false, paint);

    // Draw detail dots
    final dotPaint = Paint()
      ..color = color
      ..style = PaintingStyle.fill;

    canvas.drawCircle(
      center + Offset(radius * math.cos(math.pi / 4), radius * math.sin(math.pi / 4)),
      3,
      dotPaint,
    );
    canvas.drawCircle(
      center + Offset(radius * math.cos(math.pi * 1.25), radius * math.sin(math.pi * 1.25)),
      3,
      dotPaint,
    );
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}
