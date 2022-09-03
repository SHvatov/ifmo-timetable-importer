# ifmo-timetable-importer
Imports IFMO timetable from https://my.itmo.ru/schedule to my personal Trello.

### Flow
Using access token, a request to `/api/schedule/schedule/personal` is made in order to retrieve relevant timetable data. Then, based on the response, multiple requests to `/1/cards` are made in order to create cards on my Trello board. 

### Stack
Java 17, Spring Boot, Spring Web Flux, Lombok

### How to run
1. Set values in `applictaion.yaml` or use corresponding env. variables
2. Build application via maven - `./mvnw clean install`
3. Run application - `java -jar ./target/timetable-importer-0.0.1-SNAPSHOT.jar`
