#!/bin/bash
# Freeplane 环境检查脚本
# 用于验证新电脑上的开发环境是否正确配置

echo "======================================"
echo "  Freeplane 开发环境检查"
echo "======================================"
echo ""

# 检查 Java
echo "📌 检查 Java..."
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    echo "✅ Java 已安装: $JAVA_VERSION"
else
    echo "❌ Java 未安装"
    echo "   下载地址: https://adoptium.net/temurin/releases/?version=21"
    exit 1
fi

# 检查 JAVA_HOME
echo ""
echo "📌 检查 JAVA_HOME..."
if [ -n "$JAVA_HOME" ]; then
    echo "✅ JAVA_HOME 已设置: $JAVA_HOME"
else
    echo "⚠️  JAVA_HOME 未设置"
    echo "   请设置环境变量指向 JDK 21"
fi

# 检查 Git
echo ""
echo "📌 检查 Git..."
if command -v git &> /dev/null; then
    GIT_VERSION=$(git --version)
    echo "✅ Git 已安装: $GIT_VERSION"
else
    echo "❌ Git 未安装"
    echo "   下载地址: https://git-scm.com/downloads"
    exit 1
fi

# 检查 Gradle Wrapper
echo ""
echo "📌 检查 Gradle Wrapper..."
if [ -f "./gradlew" ]; then
    echo "✅ Gradle Wrapper 已存在"
else
    echo "❌ Gradle Wrapper 不存在"
    echo "   请从项目仓库克隆"
    exit 1
fi

# 测试 Gradle
echo ""
echo "📌 测试 Gradle..."
if ./gradlew -v &> /dev/null; then
    GRADLE_VERSION=$(./gradlew -v | grep "Gradle" | head -n 1)
    echo "✅ Gradle 可用: $GRADLE_VERSION"
else
    echo "❌ Gradle 测试失败"
    exit 1
fi

# 检查本地依赖
echo ""
echo "📌 检查本地依赖..."
if [ -d "./freeplane/lib" ]; then
    LIB_COUNT=$(ls -1 ./freeplane/lib/*.jar 2>/dev/null | wc -l)
    echo "✅ freeplane/lib/ 存在 ($LIB_COUNT 个 JAR 文件)"
else
    echo "⚠️  freeplane/lib/ 不存在"
fi

# 总结
echo ""
echo "======================================"
if [ $EXIT_CODE -eq 0 ]; then
    echo "✅ 环境检查通过！"
    echo ""
    echo "下一步："
    echo "  1. 构建项目: ./gradlew build"
    echo "  2. 运行程序: BIN/freeplane.sh"
else
    echo "❌ 环境检查失败"
    echo ""
    echo "请修复上述问题后重试"
fi
echo "======================================"

exit $EXIT_CODE
