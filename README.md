# votex

## build
```
mvn package
```
or
```
mvnw.cmd package
```

## run

Mandatory options are: -a or -c, --question and --participant.

Run in auto mode - launch browser to get cookies, vote 5 times:
```
votex -a -n 5 --question=... --participant...
```

Run with custom cookies, vote 100 times, do not check score, do not use public proxies:
```
votex -c "a=b;c=d..." -n 100 -no-score --no-proxy --question=... --participant...
```
