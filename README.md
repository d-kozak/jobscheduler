# Job scheduler

Simple JavaFX application which performs CRUD operations over tables in database.

It uses  [afterburner](https://github.com/AdamBien/afterburner.fx) for dependency injection, which allows easy separation of concerns between different layers of the app.

It it meant as an example project showing how to use 
afterburner, lombok, log4j.

## Prequisities
* Java
* JavaFX
* Maven

## Build and run
Before you run it, you need to update the application.properties with credentials and url for your database.

```
mvn clean install
```
