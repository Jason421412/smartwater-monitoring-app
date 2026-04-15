import 'package:flutter/material.dart';
import 'dart:ui';
import '../theme/app_theme.dart';

/// A glassmorphism style card widget with blur effect.
class GlassCard extends StatelessWidget {
  final Widget child;
  final double borderRadius;
  final EdgeInsetsGeometry? padding;
  final EdgeInsetsGeometry? margin;
  final double blur;
  final Color? borderColor;
  final double borderWidth;
  final Gradient? gradient;

  const GlassCard({
    super.key,
    required this.child,
    this.borderRadius = 4, // Updated to match Tech theme
    this.padding,
    this.margin,
    this.blur = 10,
    this.borderColor,
    this.borderWidth = 1,
    this.gradient,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: margin ?? const EdgeInsets.all(8),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(borderRadius),
        child: BackdropFilter(
          filter: ImageFilter.blur(sigmaX: blur, sigmaY: blur),
          child: Container(
            padding: padding ?? const EdgeInsets.all(16),
            decoration: BoxDecoration(
              gradient: gradient ??
                  LinearGradient(
                    colors: [
                      AppTheme.cardColor.withOpacity(0.6),
                      AppTheme.cardColor.withOpacity(0.3),
                    ],
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                  ),
              borderRadius: BorderRadius.circular(borderRadius),
              border: Border.all(
                color: borderColor ?? AppTheme.borderHighlight.withOpacity(0.3),
                width: borderWidth,
              ),
              boxShadow: [
                 BoxShadow(
                  color: Colors.black.withOpacity(0.2),
                  blurRadius: 10,
                  spreadRadius: 0,
                ),
              ],
            ),
            child: child,
          ),
        ),
      ),
    );
  }
}

/// A glassmorphism card with status indicator.
class StatusGlassCard extends StatelessWidget {
  final Widget child;
  final String status;
  final double borderRadius;
  final EdgeInsetsGeometry? padding;
  final EdgeInsetsGeometry? margin;

  const StatusGlassCard({
    super.key,
    required this.child,
    required this.status,
    this.borderRadius = 4,
    this.padding,
    this.margin,
  });

  @override
  Widget build(BuildContext context) {
    Color statusColor = AppTheme.getStatusColor(status);
    
    return Container(
      margin: margin ?? const EdgeInsets.all(8),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(borderRadius),
        child: BackdropFilter(
          filter: ImageFilter.blur(sigmaX: 10, sigmaY: 10),
          child: Container(
            padding: padding ?? const EdgeInsets.all(16),
            decoration: BoxDecoration(
              gradient: LinearGradient(
                colors: [
                  statusColor.withOpacity(0.15),
                  statusColor.withOpacity(0.02),
                ],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
              borderRadius: BorderRadius.circular(borderRadius),
              border: Border.all(
                color: statusColor.withOpacity(0.3),
                width: 1,
              ),
              boxShadow: [
                BoxShadow(
                  color: statusColor.withOpacity(0.05),
                  blurRadius: 15,
                  spreadRadius: 0,
                ),
              ],
            ),
            child: child,
          ),
        ),
      ),
    );
  }
}
