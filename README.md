# Alfa-case5-backend


*API пользователя:*
---
POST/api/v1/auth/resetpassword

Если правильно введен исходный пароль, заменяет пароль на новый, иначе возвращает текст ошибки*

curl  --request POST -H "Content-Type: application/json" --data '{"oldPassword":"tea_1teaaaa", "newPassword":"teaFFan13"}' http://localhost:8080/api/v1/auth/resetpassword

---
POST /api/v1/auth/register

Если соблюдены условия создания аккаунта, создает пользователя, иначе возвращает текст ошибки*

curl  --request POST -H "Content-Type: application/json" --data '{"password":"tea_1teaaaa","username":"001", "email":"randemail@gmail.com", "birthdate":"16.10.2009", "cityId":"669", "status":"1" }' http://localhost:8080/api/v1/auth/register

---
POST /api/v1/auth/login

Пользователь вводит имя и пароль, возвращает успех если пользователю удалось зайти в аккаунт, иначе возвращает текст ошибки*

curl  --request POST -H "Content-Type: application/json" --data '{"password":"tea_1teaaaa","username":"001"}' http://localhost:8080/api/v1/auth/login

---
POST /api/v1/auth/changeemail

Пользователь отправляет новую почту, возвращает успех если пользователю удалось сменить почту, иначе возвращает текст ошибки*

curl  --request POST -H "Content-Type: application/json" --data '{"email":"test@gmail.com"}'  http://localhost:8080/api/v1/auth/changeemail

---
GET api/v1/site/user/{userId}/city

Пользователь отправляет id, возвращает название города и регион

Может возвращать null при некорректном userId

curl  --request GET  http://localhost:8080/api/v1/site/user/1/city

---
POST /api/v1/auth/changeparams

Пользователь отправляет поля, которые хотел бы изменить

Если все было введено корректно обновляет данные и возвращет успех

Иначе возвращает текст ошибки, данные пользователя также не будут обновлены

curl  --request POST -H "Content-Type: application/json" --data '{ "firstName":"111", "lastName":"kvq", "birthdate":"16.01.2000", "cityId":"-1", "status":"4" }' http://localhost:8080/api/v1/auth/changeparams

---
POST /api/v1/site/searchLocation/{cityName}

Пользователь отправляет название города, возвращает список городов с таким названием если такие есть, иначе возвращает пустой список

curl  --request GET   http://localhost:8080/api/v1/site/searchLocation/моск

---
GET /api/v1/auth/logout

Удаляет cookie пользователя если имеется, иначе возвращает текст ошибки*

curl  --request GET  http://localhost:8080/api/v1/auth/logout

---
GET /api/v1/site/getAllCities

Возвращает список всех городов

curl  --request GET   http://localhost:8080/api/v1/site/getAllCities

---
POST /api/text/v1/addScore

Добавляет запись о решении пользователя, обновляет суммарный счет если максимум изменился, может возвращать ошибку если cookie недействителен

curl  --request POST -H "Content-Type: application/json" --data '{"caseId":"4","rating":"150", "solutionText":"тест тест тест", "solutionResponse":"_ответ ии"}' http://localhost:8080/api/text/v1/addScore

---
POST /api/text/v1/processViolation

Добавляет 1 на счетчик негативных решений пользователя; 

Если у пользователя N предупреждений о токсичности, удаляет аккаунт, может возвращать ошибку если cookie недействителен или если профиль пользователя удален

curl  --request POST  http://localhost:8080/api/text/v1/processViolation

---
GET /api/text/v1/checkCookie

Проверяет cookie на валидность, может возвращать ошибку если cookie недействителен

curl  --request GET  http://localhost:8080/api/text/v1/checkCookie

---
GET  /api/text/v1/getChatSequence/{caseId}

Возвращает чат с ИИ-ассистентом в виде списка пар строк

curl  --request GET  http://localhost:8080/api/text/v1/getChatSequence/1

[
["запрос1", "ответ1"],
["запрос2", "ответ2"]
]

---

**Примеры текстов ошибок:*

{"success":true,"errorText":""}

{"success":false,"errorText":"Incorrect password"}

{"success":false,"errorText":"Password cannot be empty"}

{"success":false,"errorText":"Password cannot be longer than 30 characters"}

{"success":false,"errorText":"Password cannot be shorter than 8 characters"}

{"success":false,"errorText":"Password must contain at least 1 digit"}

{"success":false,"errorText":"Password must contain at least 1 special character"}

{"success":false,"errorText":"Username cannot be longer than 20 characters"}

{"success":false,"errorText":"Username cannot be shorter than 3 characters"}

{"success":false,"errorText":"Username cannot contain spaces"}

{"success":false,"errorText":"Username cannot be empty"}

{"success":false,"errorText":"This email address is already taken"}

{"success":false,"errorText":"This username is already taken"}

{"success":false,"errorText":"This email address is invalid"}

{"success":false,"errorText":"Invalid user status code"}

{"success":false,"errorText":"You are already logged in"}

{"success":false,"errorText":"You are not logged in"}

{"success":false,"errorText":"Please login first"}

{"success":false,"errorText":"Session expired"}

{"success":false,"errorText":"User does not exist"}

{"success":false,"errorText":"Email cannot be blank"}
