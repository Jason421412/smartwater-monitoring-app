import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'theme/app_theme.dart';
import 'providers/water_quality_provider.dart';
import 'screens/splash_screen.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const AquaMonitorApp());
}

/// Main application widget for AquaMonitor - Smart Water Quality Monitoring System.
class AquaMonitorApp extends StatelessWidget {
  const AquaMonitorApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => WaterQualityProvider()),
      ],
      child: MaterialApp(
        title: 'AquaMonitor',
        debugShowCheckedModeBanner: false,
        theme: AppTheme.darkTheme,
        home: const SplashScreen(),
      ),
    );
  }
}
