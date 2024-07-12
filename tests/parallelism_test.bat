@echo off
set CLASSPATH=%CLASSPATH%;../out/.

echo Zostanie utworzona dosc duza baza skladajaca sie z 10 wezlow.
echo Z powodow czysto praktycznych procesy bazy zostana utworzone w tle.
echo Operacja potrwa okolo 10 sekund.
PAUSE

CALL:node 10000 1:10
CALL:node 10001 2:20 localhost:10000
CALL:node 10002 3:30 localhost:10001 localhost:10000
CALL:node 10003 4:40 localhost:10002 localhost:10000
CALL:node 10004 5:50 localhost:10003 localhost:10000
CALL:node 10005 6:60 localhost:10004 localhost:10000
CALL:node 10006 7:70 localhost:10005 localhost:10000
CALL:node 10007 8:80 localhost:10006 localhost:10000
CALL:node 10008 9:90 localhost:10007 localhost:10000
CALL:node 10009 10:100 localhost:10008 localhost:10000

echo.
echo Teraz zostanie przeprowadzony test polegajacy na wielokrotnym wyslaniu dwoch tych samych zapytan.
echo Jezeli zapytania sa przetwarzane rownolegle, to kolejnosc wykonania operacji powinna byc niedeterministyczna.
echo.
echo Operacja A: get-value 600
echo Wymaga przeszukania calej bazy i zwroci ERROR poniewaz nie ma takiego klucza w tej bazie.
echo.
echo Operacja B: new-record 0:0
echo Operacja wymaga jedynie dzialania na jednym wezle, co sprawia ze powinna sie ona wykonac bardzo szybko.
echo Odpowiedzia bedzie zawsze OK.
echo.
PAUSE

FOR /L %%a IN (1,1,10) DO (
CALL:client localhost:10000 get-value 600
CALL:client localhost:10000 new-record 0:0
)

timeout /t 5 /nobreak > NUL
echo.
echo Teraz cala baza danych zostanie wylaczona. Potrwa to okolo 10 sekund.
PAUSE

CALL:client localhost:10000 terminate
timeout /t 1 /nobreak > NUL
CALL:client localhost:10001 terminate
timeout /t 1 /nobreak > NUL
CALL:client localhost:10002 terminate
timeout /t 1 /nobreak > NUL
CALL:client localhost:10003 terminate
timeout /t 1 /nobreak > NUL
CALL:client localhost:10004 terminate
timeout /t 1 /nobreak > NUL
CALL:client localhost:10005 terminate
timeout /t 1 /nobreak > NUL
CALL:client localhost:10006 terminate
timeout /t 1 /nobreak > NUL
CALL:client localhost:10007 terminate
timeout /t 1 /nobreak > NUL
CALL:client localhost:10008 terminate
timeout /t 1 /nobreak > NUL
CALL:client localhost:10009 terminate
timeout /t 1 /nobreak > NUL

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
    start "%~1" /B java DatabaseNode -tcpport %~1 -record %~2 %NEIGHBOURS% > NUL
    ENDLOCAL
    timeout /t 1 /nobreak > NUL
GOTO:eof

:client
    start /B java Client -uniform-output -gateway %~1 -operation %~2 %~3
GOTO:eof