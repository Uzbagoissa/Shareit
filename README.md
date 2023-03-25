# shareit
# Cервис для шеринга - обеспечивает пользователям возможность рассказывать, какими вещами они готовы поделиться, находить нужную вещь и брать её в аренду на какое-то время.
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=java&logoColor=white) 
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white) 
![Apache Maven](https://img.shields.io/badge/Apache%20Maven-C71A36?style=for-the-badge&logo=Apache%20Maven&logoColor=white) 
![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=Hibernate&logoColor=white) 
![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white) 

![](pictures/shareit.png)

## Таблица BOOKINGS:

1. id - PRIMARY KEY AUTO INCREMENT
2. start_time
3. end_time
4. item_id - FOREIGN KEY (ITEMS)
5. booker_id
6. status 

## Таблица USERS:

1. id - PRIMARY KEY AUTO INCREMENT
2. name
3. email

## Таблица ITEMS:

1. id - PRIMARY KEY AUTO INCREMENT
2. user_id - FOREIGN KEY (USERS)
3. name
4. description
5. available
6. request

## Таблица COMMENTS:

1. id - PRIMARY KEY AUTO INCREMENT
2. texts
3. item_id - FOREIGN KEY (ITEMS)
4. author_id
5. created

## Таблица REQUESTS:

1. id - PRIMARY KEY AUTO INCREMENT
2. description
3. requester_id - FOREIGN KEY (USERS)
4. created
