# Usage
This is the backend part of the BaLe application. It is a Spring-Boot application, once that's started up it's accessible on port 8081

It loads the mount data in the background, once per hour. This means that page loads are instant (because they are not triggering the 8 URL loads that generate the data), but straight after starting the application, the table will be empty for a few seconds until the data is loaded.

The application uses an in-memory H2 database, so nothing needs to be set up before the app will run

To build this application for production, run the following commands:

```
mvn clean package
docker-compose up -d --build
docker run bale_bale
```

# Pre-requesites
This is deployed through docker, so that's the only thing that needs to be installed to run it