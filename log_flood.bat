@echo off
setlocal EnableDelayedExpansion

set URL=http://localhost:8080/log

:: Define an array of 5 usernames
set USERS[0]=alice
set USERS[1]=bob
set USERS[2]=charlie
set USERS[3]=david
set USERS[4]=emma

echo Sending logs to %URL%...

for /L %%i in (1,1,10000) do (
    :: Generate a random index between 0-4
    set /A RAND_INDEX=!RANDOM! %% 5

    :: Set the user variable correctly
    for /F "tokens=2 delims==" %%U in ('set USERS[!RAND_INDEX!]') do set "USER=%%U"

    :: Alternate between login and logout
    set /A EVENT_TYPE=%%i %% 2
    if !EVENT_TYPE! == 0 (set "EVENT=login") else (set "EVENT=logout")

    :: Generate timestamp
    set "TIMESTAMP=!time:~0,2!!time:~3,2!!time:~6,2!"

    :: Format JSON log
    set "LOG_DATA={\"event\":\"!EVENT!\",\"user\":\"!USER!\",\"timestamp\":!TIMESTAMP!}"

    :: Send log to API
    start /B curl -X POST -H "Content-Type: application/json" -d "!LOG_DATA!" %URL%
)

echo Requests sent. Exiting...
exit
