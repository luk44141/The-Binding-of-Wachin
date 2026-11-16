@echo off
setlocal
title The Binding of Wachin - DEVELOP

REM Ejecuta el juego desde la carpeta del proyecto Maven.
cd /d "%~dp0the-binding-of-wachin"

if not exist "pom.xml" (
    echo ERROR: No se encontro pom.xml en:
    echo %CD%
    echo Verifica que el .bat este junto a la carpeta the-binding-of-wachin.
    pause
    exit /b 1
)

where java >nul 2>nul
if errorlevel 1 (
    echo ERROR: Java no esta instalado o no esta agregado al PATH.
    echo Este proyecto requiere JDK 25 porque pom.xml configura maven.compiler.release en 25.
    pause
    exit /b 1
)

where mvn >nul 2>nul
if errorlevel 1 (
    echo ERROR: Maven no esta instalado o no esta agregado al PATH.
    echo Instala Apache Maven y volve a ejecutar este archivo.
    pause
    exit /b 1
)

echo Iniciando The Binding of Wachin...
echo La primera ejecucion puede tardar mientras Maven descarga dependencias.
echo.
mvn javafx:run
set "CODIGO=%ERRORLEVEL%"
if not "%CODIGO%"=="0" (
    echo.
    echo El juego termino con un error. Codigo: %CODIGO%
    echo Revisa que tengas JDK 25 y Maven configurados.
)
pause
exit /b %CODIGO%
