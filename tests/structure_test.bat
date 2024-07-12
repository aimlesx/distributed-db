@echo off
set CLASSPATH=%CLASSPATH%;../out/.

echo Zostanie utworzona przykladowa baza skladajaca sie z 3 wezlow.
PAUSE

CALL:node 10000 1:10
CALL:node 10001 2:20 localhost:10000
CALL:node 10002 3:30 localhost:10001

echo Teraz cala baza zostanie wylaczona poprzez terminacje kazdego wezla po kolei.
PAUSE

CALL:client localhost:10000 terminate
timeout /t 1 /nobreak > NUL
CALL:client localhost:10001 terminate
timeout /t 1 /nobreak > NUL
CALL:client localhost:10002 terminate

:: FUNCTIONS
GOTO:eof

:: <port> <key>:<value> [<IP address>:<port>, ...]
:node
    SETLOCAL
    :loop
    if NOT [%~3] == [] (
        set NEIGHBOURS=%NEIGHBOURS% -connect %~3
        SHIFT /3
        GOTO:loop
    )
    start "%~1" java DatabaseNode -tcpport %~1 -record %~2 %NEIGHBOURS%
    ENDLOCAL
    timeout /t 1 /nobreak > NUL
GOTO:eof

:client
    java Client -gateway %~1 -operation %~2 %~3
GOTO:eof