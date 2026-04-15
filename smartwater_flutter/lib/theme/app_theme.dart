import 'package:flutter/material.dart';

/// Application theme configuration for dark mode cyberpunk/tech HUD style.
class AppTheme {
  // Primary colors - Neon Cyberpunk Palette
  static const Color primaryColor = Color(0xFF00F0FF); // Cyber Cyan
  static const Color secondaryColor = Color(0xFF9D4EFF); // Brighter Neon Purple with glow
  static const Color accentColor = Color(0xFFFF003C); // Cyber Red/Pink

  // Status colors
  static const Color greatColor = Color(0xFF00FF9D); // Neon Green
  static const Color warningColor = Color(0xFFFF9900); // Neon Orange
  static const Color alertColor = Color(0xFFFF003C); // Critical Red
  static const Color offlineColor = Color(0xFF485870); // Muted Blue-Grey

  // Background colors
  static const Color backgroundColor = Color(0xFF050510); // Void Black
  static const Color surfaceColor = Color(0xFF0F1221); // Dark Blue-Grey
  static const Color cardColor = Color(0xFF131629); // Slightly lighter panel

  // Tech UI Colors
  static const Color gridLineColor = Color(0xFF1A2138);
  static const Color borderHighlight = Color(0x4000F0FF);
  static const Color glassBorder = Color(0xFF2A3045); // Added back for compatibility

  // Text colors
  static const Color textPrimary = Color(0xFFFFFFFF);
  static const Color textSecondary = Color(0xFF8F9BB3);
  static const Color textMuted = Color(0xFF53627C);

  // Gradients
  static const LinearGradient primaryGradient = LinearGradient(
    colors: [Color(0xFF00F0FF), Color(0xFF0080FF)],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );

  static const LinearGradient greatGradient = LinearGradient(
    colors: [Color(0xFF00FF9D), Color(0xFF00CC7A)],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );

  static const LinearGradient warningGradient = LinearGradient(
    colors: [Color(0xFFFF9900), Color(0xFFFF6600)],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );

  static const LinearGradient alertGradient = LinearGradient(
    colors: [Color(0xFFFF003C), Color(0xFFCC0030)],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );

  static const LinearGradient offlineGradient = LinearGradient(
    colors: [Color(0xFF485870), Color(0xFF2C3E50)],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );

  static const LinearGradient dangerGradient = LinearGradient(
    colors: [Color(0xFFFF003C), Color(0xFF800020)],
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
  );

  static const LinearGradient backgroundGradient = LinearGradient(
    colors: [Color(0xFF050510), Color(0xFF101225)],
    begin: Alignment.topCenter,
    end: Alignment.bottomCenter,
  );

  static const RadialGradient glowGradient = RadialGradient(
    center: Alignment.center,
    radius: 1.5,
    colors: [Color(0x1A00F0FF), Colors.transparent],
    stops: [0.0, 0.6],
  );

  /// Get the dark theme data
  static ThemeData get darkTheme {
    return ThemeData(
      useMaterial3: true,
      brightness: Brightness.dark,
      primaryColor: primaryColor,
      scaffoldBackgroundColor: backgroundColor,
      colorScheme: const ColorScheme.dark(
        primary: primaryColor,
        secondary: secondaryColor,
        surface: surfaceColor,
        error: alertColor,
      ),
      fontFamily: 'Roboto', // Use a clean sans-serif, preferably Roboto or similar
      appBarTheme: const AppBarTheme(
        backgroundColor: Colors.transparent,
        elevation: 0,
        centerTitle: true,
        titleTextStyle: TextStyle(
          color: textPrimary,
          fontSize: 24,
          fontWeight: FontWeight.bold,
          letterSpacing: 2.0, // Tech headers often have wide spacing
        ),
      ),
      // 这里修改为 CardThemeData 以解决参数类型不匹配的报错
      cardTheme: CardThemeData(
        color: cardColor,
        elevation: 0,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(4), // Tech style: minimal rounding
          side: const BorderSide(color: borderHighlight, width: 1),
        ),
      ),
      textTheme: const TextTheme(
        headlineLarge: TextStyle(
          color: textPrimary,
          fontSize: 40,
          fontWeight: FontWeight.w900,
          letterSpacing: -1.0,
        ),
        headlineMedium: TextStyle(
          color: textPrimary,
          fontSize: 28,
          fontWeight: FontWeight.bold,
          letterSpacing: 1.0,
        ),
        titleLarge: TextStyle(
          color: textPrimary,
          fontSize: 20,
          fontWeight: FontWeight.w600,
          letterSpacing: 1.2,
        ),
        titleMedium: TextStyle(
          color: textSecondary,
          fontSize: 16,
          fontWeight: FontWeight.w500,
          letterSpacing: 1.0,
        ),
        bodyLarge: TextStyle(
          color: textPrimary,
          fontSize: 16,
          height: 1.5,
        ),
        bodyMedium: TextStyle(
          color: textSecondary,
          fontSize: 14,
          height: 1.5,
        ),
        labelLarge: TextStyle(
          color: textPrimary,
          fontSize: 14,
          fontWeight: FontWeight.bold,
          letterSpacing: 2.0,
        ),
        labelSmall: TextStyle(
          color: textMuted,
          fontSize: 10,
          fontWeight: FontWeight.bold,
          letterSpacing: 1.5,
        ),
      ),
    );
  }

  /// Get status color based on status string
  static Color getStatusColor(String status) {
    switch (status.toLowerCase()) {
      case 'great':
      case 'excellent':
        return greatColor;
      case 'warning':
        return warningColor;
      case 'alert':
        return alertColor;
      case 'offline':
        return offlineColor;
      default:
        return textMuted;
    }
  }

  /// Get status gradient based on status string
  static LinearGradient getStatusGradient(String status) {
    switch (status.toLowerCase()) {
      case 'great':
      case 'excellent':
        return greatGradient;
      case 'warning':
        return warningGradient;
      case 'alert':
        return alertGradient;
      case 'offline':
        return offlineGradient;
      default:
        return LinearGradient(
          colors: [textMuted, textMuted.withOpacity(0.5)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        );
    }
  }
}