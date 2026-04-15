import 'package:flutter_test/flutter_test.dart';

import 'package:smart_water_monitor/main.dart';

void main() {
  testWidgets('AquaMonitorApp builds', (WidgetTester tester) async {
    await tester.pumpWidget(const AquaMonitorApp());
    await tester.pump(); // let first frame render
    expect(find.byType(AquaMonitorApp), findsOneWidget);
  });
}
