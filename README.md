# Usage
Once the application is started up, access it through http://localhost:58380/mounts

It loads the mount data in the background, once per hour. This means that page loads are instant (because they are not triggering the 8 URL loads that generate the data), but straight after starting the application, the table will be empty for a few seconds until the data is loaded.

# Pre-requesites
This application requires a local MySQL database and according to the values set in application.properties, you will need to set up this schema and user:

```sql
create database bale;
create user 'bale'@'localhost' identified by 'bale';
grant all privileges on bale.* to 'bale'@'localhost';
```

Then run the contents of https://github.com/DannyCorbett89/bale/blob/master/src/main/resources/setup.sql to set up the tables, and that should take care of everything the app needs
