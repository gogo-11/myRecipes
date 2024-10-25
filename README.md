# My Recipes - A Java Web App

This project allows you to manage and discover various recipes. 
It's built using Spring Boot, Thymeleaf, Hibernate, and managed with Maven.

## Table of Contents
- [Technologies used](#technologies-used)
- [Installation](#installation)
- [Usage](#usage)

## Technologies used
- Java (JDK 17+)
- Spring Boot
- Hibernate (JPA)
- Thymeleaf
- Maven
- MySQL

## Installation
### Prerequisites
- **Java Development Kit (JDK)**: Make sure you have JDK 17 or higher installed. You can check your version by running `java -version` in your terminal.
- **Maven**: Ensure Maven is installed and available in your system’s PATH. You can verify this by running `mvn -version`.
- **MySQL**: Install MySQL Server and MySQL Workbench (optional, for easier database management).

### Steps
1. Clone the repository.

```bash
git clone git@github.com:gogo-11/myRecipes.git
```
2. Build the project using Maven
```
mvn clean install
```

This command will compile the project and package it into a JAR file located in the `target/` directory

3. Configure your database settings in the `application.properties` file (located in src/main/resources). Replace the placeholders with your MySQL credentials:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/your_app_db_name
spring.datasource.username=username
spring.datasource.password=password

server.port=8081

app.base-url=http://localhost:8081

spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL5InnoDBDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.generate-ddl=true
spring.jpa.show-sql=true

spring.thymeleaf.template-loader-path: classpath:/templates
spring.thymeleaf.suffix: .html
spring.thymeleaf.cache: false

logging.level.org.springframework.security=DEBUG

# Email Configuration
spring.mail.host=smtp.gmail.com
# you can use any email provider, this is an example with Gmail
spring.mail.port=587
spring.mail.username=yourmail@gmail.com
spring.mail.password=password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
```
## Usage
1. Ensure your MySQL server is running, and the database your_app_db_name exists. If it doesn't exist, you can create it in MySQL Workbench or using the command line:
```sql
CREATE DATABASE myrecipesdb;
```
2. Run the application:
```bash
java -jar target/myRecipes-0.0.1-SNAPSHOT.jar
```
Please replace the `target/myRecipes-0.0.1-SNAPSHOT.jar` with the actual path 
and file title of the JAR you need to run in Windows.

3. Open a web browser and access the application at [http://localhost:8081](http://localhost:8081).
