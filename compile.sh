#!/bin/bash

echo "🔨 Compiling Spring Lite Framework Demo..."

# JAR 파일들을 classpath에 추가
CLASSPATH="lib/jetty-server-9.4.44.v20210927.jar:lib/jetty-servlet-9.4.44.v20210927.jar:lib/jetty-util-9.4.44.v20210927.jar:lib/jetty-util-ajax-9.4.44.v20210927.jar:lib/jetty-http-9.4.44.v20210927.jar:lib/jetty-io-9.4.44.v20210927.jar:lib/jetty-security-9.4.44.v20210927.jar:lib/servlet-api-3.1.0.jar:lib/jackson-core-2.15.2.jar:lib/jackson-databind-2.15.2.jar:lib/jackson-annotations-2.15.2.jar:lib/jetty-webapp-9.4.44.v20210927.jar:lib/jetty-xml-9.4.44.v20210927.jar:lib/jetty-jsp-9.4.44.v20210927.jar:lib/jetty-annotations-9.4.44.v20210927.jar:lib/jsp-api-2.3.3.jar:lib/el-api-3.0.0.jar"

# 소스 컴파일
javac -cp "$CLASSPATH" -d out/production/springlite $(find src -name "*.java")

if [ $? -eq 0 ]; then
    echo "✅ Compilation successful!"
else
    echo "❌ Compilation failed!"
    exit 1
fi 