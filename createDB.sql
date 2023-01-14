create database myrecipes_db;
create user 'recipesUser'@'localhost' IDENTIFIED BY 'password123';
GRANT CREATE, ALTER, DROP, INSERT, UPDATE, DELETE, SELECT, REFERENCES, RELOAD on *.* TO 'recipesUser'@'localhost' WITH GRANT OPTION;