This application keeps track of which mounts and minions each player in an FC needs. When given the URL of an FC's lodestone page, this application will:
- Load the list of all FC members
- Load the list of all Mounts and Minions that each FC member has (this is done every hour to keep up to date)
- Use the data from [the lodestone](https://eu.finalfantasyxiv.com/lodestone/playguide/db) to calculate which mounts and minions each player needs
- Return this information in an endpoint for a frontend to display

## Pre-requisites
The server running this application needs the following installed:
- Java 8
  - JAVA_HOME variable needs to be set so that Maven will work
- MySQL database
- Your hostname provider must be https://uk.godaddy.com
  - This only applies if you want the application to automatically keep your server's IP up to date with the hostname provider whenever it changes. [DomainNameController](src/main/java/com/dc/bale/hidden/controller2/DomainNameController.java) will handle this but currently it is only written to work with the GoDaddy API. If you use any other provider, you will need to handle this yourself
  
## Usage
### Set up the database
1. In MySQL, create an empty database
2. Run the [tables.sql](src/main/resources/tables.sql) file into your new database
2. Edit the [config.sql](src/main/resources/config.sql) file like this before running it into your database:
   - Change the value of `freeCompanyUrl` to your FC's Lodestone URL
   - If you want your domain name to be kept up to date with your server's dynamic IP:
     - Change the value of `domainName` to the website you are hosting this on
     - Uncomment and set `domainKey` and `domainSecret` to the values given to you from your GoDaddy account
     
### Build and run the application
1. Edit [application.properties](src/main/resources/application.properties):
   - These properties must match your database setup:
       ```
       spring.datasource.url
       spring.datasource.username
       spring.datasource.password
       logging.file=/var/log/bale.log
       ```
   - Change `server.port` to the number you want your backend to run on. 80 is not recommended as it's best to run the frontend on that
   - Change `logging.file` to a path you want your log file in. You can leave it as-is if running linux
2. Run `mvnw clean package`, this should create a jar file
3. Copy the jar file to your server and run it like this: `java -jar /opt/apps/bale-0.1.jar &`. The `&` is important if you're running this in an ssh session as it runs it in a separate process, so won't terminate when you disconnect from ssh
