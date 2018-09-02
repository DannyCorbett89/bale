# Usage
The backend is a Spring-Boot application, once that's started up it's accessible on port 8081

The frontend is a React application, start that up (in development mode) by running `npm start` from the `bale/bale-frontend/src` directory

To package the frontend up for production, run these commands from the `bale/bale-frontend/src` directory:
```
npm run build
serve -s build -l 80 &
```

It loads the mount data in the background, once per hour. This means that page loads are instant (because they are not triggering the 8 URL loads that generate the data), but straight after starting the application, the table will be empty for a few seconds until the data is loaded.

# Pre-requesites
## Backend
This application requires a local MySQL database and according to the values set in [application.properties](https://github.com/DannyCorbett89/bale/blob/master/src/main/resources/application.properties), you will need to set up this schema and user:

```sql
create database bale;
create user 'bale'@'localhost' identified by 'bale';
grant all privileges on bale.* to 'bale'@'localhost';
```

Then run the contents of https://github.com/DannyCorbett89/bale/blob/master/src/main/resources/setup.sql to set up the tables

## Frontend
Some React dependencies are needed for the frontend, run these commands to get them:
```
npm install react-youtube
npm install @material-ui/core
npm install @material-ui/icons
npm install --save react-router-dom
npm install react-device-detect --save
```
