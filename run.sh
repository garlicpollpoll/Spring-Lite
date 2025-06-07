#!/bin/bash

echo "🚀 Starting Spring Lite Framework Demo..."

# JAR 파일들을 classpath에 추가 (JSP 의존성 제외)
CLASSPATH="out/production/springlite:lib/jetty-server-9.4.44.v20210927.jar:lib/jetty-servlet-9.4.44.v20210927.jar:lib/jetty-util-9.4.44.v20210927.jar:lib/jetty-util-ajax-9.4.44.v20210927.jar:lib/jetty-http-9.4.44.v20210927.jar:lib/jetty-io-9.4.44.v20210927.jar:lib/jetty-security-9.4.44.v20210927.jar:lib/servlet-api-4.0.1.jar:lib/jackson-core-2.13.0.jar:lib/jackson-databind-2.13.0.jar:lib/jackson-annotations-2.13.0.jar:lib/jetty-webapp-9.4.44.v20210927.jar:lib/jetty-annotations-9.4.44.v20210927.jar"

# 애플리케이션 실행
java -cp "$CLASSPATH" com.springlite.demo.Application 