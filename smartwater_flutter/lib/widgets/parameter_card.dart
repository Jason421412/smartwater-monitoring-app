import 'package:flutter/material.dart';
import '../theme/app_theme.dart';

/// A card widget displaying a single water quality parameter with premium static design.
class ParameterCard extends StatelessWidget {
  final String title;
  final String value;
  final String unit;
  final IconData icon;
  final String status;
  final Color? iconColor;

  const ParameterCard({
    super.key,
    required this.title,
    required this.value,
    required this.unit,
    required this.icon,
    required this.status,
    this.iconColor,
  });

  @override
  Widget build(BuildContext context) {
    final statusColor = AppTheme.getStatusColor(status);
    final effectiveIconColor = iconColor ?? statusColor;
    final isWarning = status == 'warning';

    return Container(
      margin: const EdgeInsets.all(6),
      decoration: BoxDecoration(
        color: AppTheme.cardColor.withOpacity(0.5),
        borderRadius: BorderRadius.circular(2), // 极小圆角，硬朗风格
        border: Border.all(
          color: effectiveIconColor.withOpacity(isWarning ? 0.6 : 0.15),
          width: 1,
        ),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.3),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Stack(
        children: [
          // Colored background overlay
          Positioned.fill(
            child: Container(
              decoration: BoxDecoration(
                borderRadius: BorderRadius.circular(2),
                gradient: LinearGradient(
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                  colors: [
                    effectiveIconColor.withOpacity(0.08),
                    effectiveIconColor.withOpacity(0.02),
                  ],
                ),
              ),
            ),
          ),
          // 装饰性背景网格或扫描线效果（可选，保持简洁则略过）
          
          // 四角科技感装饰
          _buildCornerAccent(top: 0, left: 0, color: effectiveIconColor),
          _buildCornerAccent(top: 0, right: 0, color: effectiveIconColor),
          _buildCornerAccent(bottom: 0, left: 0, color: effectiveIconColor),
          _buildCornerAccent(bottom: 0, right: 0, color: effectiveIconColor),

          // 内容区域
          Padding(
            padding: const EdgeInsets.fromLTRB(14, 12, 14, 10),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisSize: MainAxisSize.max,
              children: [
                // 顶部：图标 + 标题
                Row(
                  children: [
                    _buildTechIcon(effectiveIconColor),
                    const SizedBox(width: 10),
                    Expanded(
                      child: Text(
                        title.toUpperCase(),
                        style: Theme.of(context).textTheme.labelSmall?.copyWith(
                              color: AppTheme.textMuted.withOpacity(0.7),
                              fontWeight: FontWeight.bold,
                              letterSpacing: 1.5,
                              fontSize: 9,
                            ),
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                    // 状态指示灯
                    if (isWarning)
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 5, vertical: 2),
                        decoration: BoxDecoration(
                          color: AppTheme.warningColor.withOpacity(0.2),
                          border: Border.all(color: AppTheme.warningColor.withOpacity(0.5)),
                          borderRadius: BorderRadius.circular(2),
                        ),
                        child: Text(
                          "WARN",
                          style: TextStyle(
                            color: AppTheme.warningColor,
                            fontSize: 8,
                            fontWeight: FontWeight.bold,
                            letterSpacing: 1,
                          ),
                        ),
                      )
                    else
                      Container(
                        width: 6,
                        height: 6,
                        decoration: BoxDecoration(
                          color: effectiveIconColor,
                          shape: BoxShape.rectangle,
                          boxShadow: [
                            BoxShadow(
                              color: effectiveIconColor.withOpacity(0.8),
                              blurRadius: 4,
                            ),
                          ],
                        ),
                      ),
                  ],
                ),
                
                const Spacer(),
                
                // 数值显示区 - 居中放大，确保一行显示
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 12),
                  child: FittedBox(
                    fit: BoxFit.scaleDown,
                    child: Row(
                      crossAxisAlignment: CrossAxisAlignment.baseline,
                      textBaseline: TextBaseline.alphabetic,
                      mainAxisAlignment: MainAxisAlignment.center,
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        _AnimatedValueDisplay(
                          value: value,
                          style: Theme.of(context).textTheme.headlineLarge?.copyWith(
                                color: Colors.white,
                                fontWeight: FontWeight.w700,
                                fontSize: 42,
                                letterSpacing: 0,
                                height: 1.0,
                              ),
                        ),
                        const SizedBox(width: 6),
                        Text(
                          unit,
                          style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                                color: effectiveIconColor.withOpacity(0.7),
                                fontWeight: FontWeight.bold,
                                fontSize: 14,
                              ),
                        ),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  /// 构建科技感图标容器
  Widget _buildTechIcon(Color color) {
    return Container(
      width: 42,
      height: 42,
      decoration: BoxDecoration(
        color: color.withOpacity(0.1),
        borderRadius: BorderRadius.circular(4),
        border: Border.all(color: color.withOpacity(0.3), width: 1),
      ),
      child: Stack(
        alignment: Alignment.center,
        children: [
          // 内部十字准星装饰
          Center(
            child: Container(
              width: 10,
              height: 1,
              color: color.withOpacity(0.3),
            ),
          ),
          Center(
            child: Container(
              width: 1,
              height: 10,
              color: color.withOpacity(0.3),
            ),
          ),
          // 图标主体
          ShaderMask(
            shaderCallback: (Rect bounds) {
              return LinearGradient(
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
                colors: [Colors.white, color],
                stops: const [0.2, 1.0],
              ).createShader(bounds);
            },
            child: Icon(
              icon,
              color: Colors.white,
              size: 22,
            ),
          ),
        ],
      ),
    );
  }

  /// 构建角落装饰
  Widget _buildCornerAccent({
    double? top,
    double? bottom,
    double? left,
    double? right,
    required Color color,
  }) {
    return Positioned(
      top: top,
      bottom: bottom,
      left: left,
      right: right,
      child: Container(
        width: 8,
        height: 8,
        decoration: BoxDecoration(
          border: Border(
            top: top != null ? BorderSide(color: color.withOpacity(0.6), width: 2) : BorderSide.none,
            bottom: bottom != null ? BorderSide(color: color.withOpacity(0.6), width: 2) : BorderSide.none,
            left: left != null ? BorderSide(color: color.withOpacity(0.6), width: 2) : BorderSide.none,
            right: right != null ? BorderSide(color: color.withOpacity(0.6), width: 2) : BorderSide.none,
          ),
        ),
      ),
    );
  }
}

class _AnimatedValueDisplay extends StatelessWidget {
  final String value;
  final TextStyle? style;

  const _AnimatedValueDisplay({
    required this.value,
    this.style,
  });

  @override
  Widget build(BuildContext context) {
    // Try to parse the value to a double
    final double? numericValue = double.tryParse(value);

    // If not numeric (e.g. "-"), just return static text
    if (numericValue == null) {
      return Text(
        value,
        style: style,
        textAlign: TextAlign.center,
      );
    }

    // Determine decimal places based on input string
    final int decimalPlaces = value.contains('.') 
        ? value.split('.')[1].length 
        : 0;

    return TweenAnimationBuilder<double>(
      tween: Tween<double>(begin: numericValue, end: numericValue),
      duration: const Duration(milliseconds: 800),
      curve: Curves.easeOutQuart,
      builder: (context, val, child) {
        return Text(
          val.toStringAsFixed(decimalPlaces),
          style: style,
          textAlign: TextAlign.center,
        );
      },
    );
  }
}
