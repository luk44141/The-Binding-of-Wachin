@echo off
setlocal
cd /d "%~dp0"
echo ========================================
echo   The Binding of Wachin - MAIN
echo ========================================
echo.
if not exist "pom.xml" (
  echo ERROR: No se encontro pom.xml.
  echo Deja este archivo .bat en la carpeta the-binding-of-wachin, junto a pom.xml.
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
where java >nul 2>nul
if errorlevel 1 (
  echo ERROR: Java no esta instalado o no esta agregado al PATH.
  pause
  exit /b 1
)
echo Iniciando el juego...
echo Si Maven informa que falta Java 25, instala/configura JDK 25.
echo.
call mvn javafx:run
if errorlevel 1 (
  echo.
  echo El juego no pudo iniciarse. Revisa el error anterior.
  pause
)
endlocal
