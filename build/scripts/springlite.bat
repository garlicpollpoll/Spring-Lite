@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem
@rem SPDX-License-Identifier: Apache-2.0
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  springlite startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and SPRINGLITE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS=

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH. 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo. 1>&2
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME% 1>&2
echo. 1>&2
echo Please set the JAVA_HOME variable in your environment to match the 1>&2
echo location of your Java installation. 1>&2

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\lib\springlite-1.0.0.jar;%APP_HOME%\lib\apache-jsp-9.4.44.v20210927.jar;%APP_HOME%\lib\jetty-annotations-9.4.44.v20210927.jar;%APP_HOME%\lib\jetty-plus-9.4.44.v20210927.jar;%APP_HOME%\lib\jetty-webapp-9.4.44.v20210927.jar;%APP_HOME%\lib\jetty-servlet-9.4.44.v20210927.jar;%APP_HOME%\lib\jetty-security-9.4.44.v20210927.jar;%APP_HOME%\lib\jetty-server-9.4.44.v20210927.jar;%APP_HOME%\lib\apache-jstl-9.4.44.v20210927.jar;%APP_HOME%\lib\javax.servlet.jsp-api-2.3.3.jar;%APP_HOME%\lib\javax.el-api-3.0.0.jar;%APP_HOME%\lib\javax.el-3.0.0.jar;%APP_HOME%\lib\javax.servlet-api-4.0.1.jar;%APP_HOME%\lib\jackson-core-2.15.2.jar;%APP_HOME%\lib\jackson-annotations-2.15.2.jar;%APP_HOME%\lib\jackson-databind-2.15.2.jar;%APP_HOME%\lib\h2-2.2.224.jar;%APP_HOME%\lib\slf4j-simple-1.7.36.jar;%APP_HOME%\lib\jetty-http-9.4.44.v20210927.jar;%APP_HOME%\lib\jetty-io-9.4.44.v20210927.jar;%APP_HOME%\lib\jetty-util-ajax-9.4.44.v20210927.jar;%APP_HOME%\lib\jetty-xml-9.4.44.v20210927.jar;%APP_HOME%\lib\javax.annotation-api-1.3.2.jar;%APP_HOME%\lib\asm-commons-9.2.jar;%APP_HOME%\lib\asm-analysis-9.2.jar;%APP_HOME%\lib\asm-tree-9.2.jar;%APP_HOME%\lib\asm-9.2.jar;%APP_HOME%\lib\jetty-jndi-9.4.44.v20210927.jar;%APP_HOME%\lib\jetty-util-9.4.44.v20210927.jar;%APP_HOME%\lib\apache-jsp-8.5.70.jar;%APP_HOME%\lib\jetty-schemas-3.1.2.jar;%APP_HOME%\lib\taglibs-standard-spec-1.2.5.jar;%APP_HOME%\lib\taglibs-standard-impl-1.2.5.jar;%APP_HOME%\lib\slf4j-api-1.7.36.jar;%APP_HOME%\lib\apache-el-8.5.70.jar;%APP_HOME%\lib\ecj-3.19.0.jar


@rem Execute springlite
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %SPRINGLITE_OPTS%  -classpath "%CLASSPATH%" com.springlite.demo.Application %*

:end
@rem End local scope for the variables with windows NT shell
if %ERRORLEVEL% equ 0 goto mainEnd

:fail
rem Set variable SPRINGLITE_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd.exe /c_ return code!
set EXIT_CODE=%ERRORLEVEL%
if %EXIT_CODE% equ 0 set EXIT_CODE=1
if not ""=="%SPRINGLITE_EXIT_CONSOLE%" exit %EXIT_CODE%
exit /b %EXIT_CODE%

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
