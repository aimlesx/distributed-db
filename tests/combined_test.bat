@echo off
set CLASSPATH=%CLASSPATH%;../out/.

:: DATABASE GRAPH DEFINITION
CALL:node 10000 1:10
CALL:node 10001 2:20 localhost:10000
CALL:node 10002 3:30 localhost:10001
CALL:node 10004 0:-10 localhost:10000
CALL:node 10003 4:40 localhost:10000 localhost:10002
CALL:node 10005 5:100 localhost:10000

echo Click to conduct tests...
PAUSE > NUL
echo.
CALL:client localhost:10000 get-max
CALL:client localhost:10003 get-max
CALL:client localhost:10004 get-max
CALL:client localhost:10000 get-min
CALL:client localhost:10003 get-min
CALL:client localhost:10004 get-min

CALL:client localhost:10000 set-value 5:-20
CALL:client localhost:10002 get-min

CALL:client localhost:10004 get-value 5
CALL:client localhost:10003 find-key 2

CALL:client localhost:10000 new-record 7:700
CALL:client localhost:10002 get-max

echo.
echo Operations that should fail:
CALL:client localhost:10000 get-value 666
CALL:client localhost:10000 find-key 666
CALL:client localhost:10000 set-value 666:111
echo.

echo Click to terminate database...
PAUSE > NUL
CALL:client localhost:10000 terminate
CALL:client localhost:10001 terminate
CALL:client localhost:10002 terminate
CALL:client localhost:10003 terminate
CALL:client localhost:10004 terminate
CALL:client localhost:10005 terminate

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

:: <IP address>:<port> <operation> [operation arguments]
:client
    java Client -gateway %~1 -operation %~2 %~3
GOTO:eof