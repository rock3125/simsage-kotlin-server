# SimSage Kotlin Server example

written using Kotlin 1.3.10

Small SimSage Kotlin Server for HTTP/HTTPS access.

## installation
Use gradle for your build system
```
# fetch all dependencies and compile the app
gradle clean build
```
Don't forget to get your keys from SimSage 
visit https://simsage.nz/api.html for more details.

replace these IDs in simsage.settings with your values.
```
simsage.organisation.id = ?
simsage.kb.id = ?
simsage.security.id = ?
```

## run on default port 8080
```
```

## quick sanity check
post some text to the service using curl
```
curl -X POST --header "Content-Type: application/json" --data '{"query": "what are you?", "customerId": "12345"}' http://localhost:8080/query
```
