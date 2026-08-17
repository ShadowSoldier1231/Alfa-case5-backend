

# Alfa-case5-backend

# Документация API

Спецификация серверных эндпоинтов приложения.

---
## Системные эндпоинты
### Проверка работоспособности (`GET`)
Легковесный эндпоинт для Health Check. Используется мониторингом (например, Docker или Kubernetes) для проверки того, что приложение запущено и база данных доступна.
```bash
curl -X GET http://localhost:8080/health
```
**Ответ (JSON):**
```json
{
  "status": "UP",
  "timestamp": "2026-07-23T10:15:30.123",
  "database": "UP"
}
```
*(Если база данных недоступна, поле `database` будет содержать `"DOWN: <текст ошибки>"`)*.

---
## Безопасность и доступ
- **`/api/v1/**`** — **Доступно всем**. Авторизация не требуется (кроме операций с личными данными).
- **`/api/text/v1/**`** — **Требует Cookie**. На уровне Spring Security эндпоинты открыты (`permitAll`), но контроллеры вручную проверяют наличие и валидность заголовка `Cookie: token=ТОКЕН`.
- **`/api/admin/**`** — **Только администраторы**. Доступ строго ограничен ролью `ADMIN` (проверяется Spring Security).

---
## Профиль и Аутентификация (`/api/v1/auth`)
### Регистрация (`POST`)
Создание новой учетной записи. Сервер возвращает ID пользователя и инициирует процесс верификации.
```bash
curl -X POST -H "Content-Type: application/json" \
-d '{"username":"1234","password":"tea_tea1","email":"someemail@gmail.com","birthdate":"16.10.2009","cityId":"1","status":"STUDENT10","firstName":"nameName","lastName":"last_name","middleName":"name","gender":"MALE","validationMethod":"EMAIL"}' \
http://localhost:8080/api/v1/auth/register
```
> **Примечание:** Поле `validationMethod` обязательно и принимает значения `EMAIL` или `TELEGRAM`.

### Верификация аккаунта (`POST`)
Подтверждение регистрации через код, отправленный на Email. Код передается в URL (path variable).
```bash
curl -X POST -H "Cookie: token=TOKEN" http://localhost:8080/api/v1/auth/verify/818018
```
**Ответ (JSON):**
```json
{
  "success": true,
  "errorText": "",
  "id": 8
}
```

### Вход (`POST`)
Авторизация пользователя. При успехе сервер возвращает сессионную Cookie.
```bash
curl -X POST -H "Content-Type: application/json" \
-d '{"username":"001","password":"tea_1teaaaa"}' \
http://localhost:8080/api/v1/auth/login
```

### Получение ID текущего пользователя (`GET`) *(Требует Cookie)*
Легковесный метод для получения только ID пользователя по валидной сессионной Cookie.
```bash
curl -X GET -H "Cookie: token=TOKEN" http://localhost:8080/api/v1/auth/getId
```
**Ответ (JSON):**
```json
{
  "id": 8
}
```

### Получение полного профиля (`GET`) *(Требует Cookie)*
Возвращает расширенную информацию о текущем авторизованном пользователе, включая email, личные данные, статистику в таблице лидеров и информацию о городе.
```bash
curl -X GET -H "Cookie: token=TOKEN" http://localhost:8080/api/v1/auth/me
```
**Ответ (JSON):**
```json
{
  "id": 8,
  "email": "someemail@gmail.com",
  "firstName": "nameName",
  "lastName": "last_name",
  "middleName": "name",
  "nickName": "char",
  "birthdate": "16.10.2009",
  "gender": "MALE",
  "status": "STUDENT10",
  "cityName": "Название города",
  "regionName": "Название региона",
  "score": 150,
  "placement": 42,
  "avatarUrl": "avatars/uuid.jpg"
}
```

### Смена пароля (`POST`) *(Требует Cookie)*
Замена текущего пароля на новый.
```bash
curl -X POST -H "Content-Type: application/json" -H "Cookie: token=TOKEN" \
-d '{"oldPassword":"tea_1teaaaa","newPassword":"teaFFan13"}' \
http://localhost:8080/api/v1/auth/resetpassword
```

### Изменение личных данных (`POST`) *(Требует Cookie)*
Обновление полей профиля. При ошибке валидации данные не сохраняются.
```bash
curl -X POST -H "Content-Type: application/json" -H "Cookie: token=TOKEN" \
-d '{"firstName":"111","lastName":"kvq","middleName":"someOtherName","birthdate":"16.01.2000","cityId":"15","status":"UNDERGRADUATE","nickName":"newNick"}' \
http://localhost:8080/api/v1/auth/changeparams
```

### Смена Email (`POST`) *(Требует Cookie)*
```bash
curl -X POST -H "Content-Type: application/json" -H "Cookie: token=TOKEN" \
-d '{"email":"test@gmail.com"}' \
http://localhost:8080/api/v1/auth/changeemail
```

### Установка аватара (`POST`) *(Требует Cookie)*
Загрузка файла изображения профиля.
```bash
curl -v -F "file=@/path/to/image.jpeg" -H "Cookie: token=TOKEN" \
http://localhost:8080/api/v1/auth/setProfilePicture
```

### Выход (`GET`) *(Требует Cookie)*
Удаление сессионной куки пользователя.
```bash
curl -X GET -H "Cookie: token=TOKEN" http://localhost:8080/api/v1/auth/logout
```

---
## Публичные данные и просмотр профилей (`/api/v1/site`)
### Карточка профиля (`GET`)
Возвращает публичную статистику и данные любого пользователя по его ID для отображения на сайте.
```bash
curl -X GET http://localhost:8080/api/v1/site/user/1/profile
```
**Ответ (JSON):**
```json
{
  "id": 1,
  "birthdate": "16.10.2009",
  "cityName": "Бородино",
  "regionName": "Красноярский край",
  "firstName": "random",
  "lastName": "fun",
  "middleName": "naming",
  "nickName": "char",
  "gender": "MALE",
  "status": "STUDENT10",
  "score": 0,
  "placement": 0,
  "avatarUrl": "avatars/uuid.jpg"
}
```

### Поиск города по названию (`GET`)
Используется для фильтрации списка городов при поиске. Возвращает пагинированный массив совпадений.
```bash
curl -X GET "http://localhost:8080/api/v1/site/searchLocation/моск?page=0&size=25&sort=city_name,asc"
```
**Ответ (JSON):**
```json
{
  "items": [
    {"id": 1, "cityName": "Москва", "regionName": "Московская область"}
  ],
  "page": 0,
  "size": 25,
  "totalElements": 1,
  "totalPages": 1
}
```

### Получить город пользователя (`GET`)
Возвращает информацию о городе конкретного пользователя. Если ID не существует, возвращает объект с дефолтными значениями (`id: -1`, `cityName: "not_set"`, `regionName: "not_set"`).
```bash
curl -X GET http://localhost:8080/api/v1/site/user/1/city
```

### Список всех городов (`GET`)
Запрос пагинированного списка городов для выпадающих списков или фильтров с возможностью поиска.
```bash
curl -X GET "http://localhost:8080/api/v1/site/getAllCities?page=0&size=25&search=&sort=city_name,asc"
```
**Ответ (JSON):**
```json
{
  "items": [
    {"id": 1, "cityName": "Москва", "regionName": "Московская область"},
    {"id": 2, "cityName": "Санкт-Петербург", "regionName": "Ленинградская область"}
  ],
  "page": 0,
  "size": 25,
  "totalElements": 2,
  "totalPages": 1
}
```

---
## Кейсы и Материалы (`/api/v1/cases`)
### Список всех активных кейсов (`GET`)
Возвращает пагинированный список опубликованных кейсов с возможностью поиска и сортировки.
```bash
curl -X GET "http://localhost:8080/api/v1/cases/getAll?page=0&size=25&search=&sort=created_at,desc"
```
**Ответ (JSON):**
```json
{
  "items": [
    {
      "id": 1,
      "slug": "some-case",
      "title": "Название",
      "titleEn": "Title",
      "description": "Описание",
      "fullDescription": "Полное описание",
      "difficulty": "EASY",
      "averageSolveMin": 30,
      "pdfUrl": "cases/pdfs/uuid.pdf",
      "iconUrl": "cases/icons/uuid.jpg",
      "viewsCount": 10,
      "createdAt": "2026-06-25T12:00:00",
      "updatedAt": "2026-06-25T12:00:00",
      "tags": [{"id": 1, "name": "Java", "count": 15}]
    }
  ],
  "page": 0,
  "size": 25,
  "totalElements": 1,
  "totalPages": 1
}
```

### Полная информация о кейсе (`GET`)
Возвращает детальную информацию о кейсе по его ID, включая полное описание и ссылки на файлы. Автоматически увеличивает счетчик просмотров.
```bash
curl -X GET http://localhost:8080/api/v1/cases/1
```

### Список активных тегов (`GET`)
Возвращает пагинированный список разрешенных тегов, которые можно использовать для фильтрации, с количеством привязанных кейсов.
```bash
curl -X GET "http://localhost:8080/api/v1/cases/tags?page=0&size=25&search=&sort=case_count,desc"
```
**Ответ (JSON):**
```json
{
  "items": [
    {"id": 1, "name": "Java", "count": 15},
    {"id": 2, "name": "Spring", "count": 10}
  ],
  "page": 0,
  "size": 25,
  "totalElements": 2,
  "totalPages": 1
}
```

---
## Лидерборд и Рейтинги (`/api/v1/site/leaderboard`)
### Топ-5 игроков лидерборда (`GET`)
Возвращает список 5 лучших игроков с их статистикой и городами. Если список пуст, возвращает `204 No Content`.
```bash
curl -X GET http://localhost:8080/api/v1/site/leaderboard/top5
```
**Ответ (JSON):**
```json
[
  {
    "userId": 1,
    "placement": 1,
    "score": 1500,
    "firstName": "Иван",
    "nickName": "champion",
    "cityName": "Москва",
    "avatarUrl": "avatars/1.jpg"
  },
  {
    "userId": 5,
    "placement": 2,
    "score": 1200,
    "firstName": "Анна",
    "nickName": "pro_player",
    "cityName": "Санкт-Петербург",
    "avatarUrl": "avatars/5.jpg"
  }
]
```

### Топ-5 игроков по конкретному кейсу (`GET`)
Возвращает список 5 лучших игроков, которые решали указанный кейс. **Авторизация не требуется.**
```bash
curl -X GET http://localhost:8080/api/v1/site/leaderboard/case/1/top5
```

### Мое место в глобальном рейтинге (`GET`) *(Требует Cookie)*
Возвращает текущее место авторизованного пользователя в общем зачете и общее количество верифицированных участников. Если у пользователя 0 очков, `placement` будет равен `0`.
```bash
curl -X GET -H "Cookie: token=TOKEN" http://localhost:8080/api/v1/site/leaderboard/global/my-place
```
**Ответ (JSON):**
```json
{
  "placement": 42,
  "total": 1500
}
```

### Мое место в рейтинге по кейсу (`GET`) *(Требует Cookie)*
Возвращает место авторизованного пользователя в рейтинге по конкретному кейсу и общее количество участников в глобальном зачете.
```bash
curl -X GET -H "Cookie: token=TOKEN" http://localhost:8080/api/v1/site/leaderboard/local/my-place/1
```
**Ответ (JSON):**
```json
{
  "placement": 5,
  "total": 1500
}
```

---
## Администрирование (`/api/admin/v1`)
*Все эндпоинты в этом разделе требуют валидную сессионную Cookie пользователя с ролью `ADMIN`.*

### Управление кейсами
#### Список всех кейсов (`GET`)
Возвращает пагинированный список всех кейсов, включая неактивные (скрытые), с возможностью поиска и сортировки.
```bash
curl -X GET -H "Cookie: token=TOKEN" "http://localhost:8080/api/admin/v1/cases?page=0&size=25&search=&sort=created_at,desc"
```
**Ответ (JSON):**
```json
{
  "items": [
    {
      "id": 1,
      "slug": "some-case",
      "title": "Название",
      "titleEn": "Title",
      "description": "Описание",
      "fullDescription": "Полное описание",
      "difficulty": "EASY",
      "averageSolveMin": 30,
      "pdfUrl": "cases/pdfs/uuid.pdf",
      "iconUrl": "cases/icons/uuid.jpg",
      "promptContextEn": "Prompt...",
      "viewsCount": 10,
      "active": true,
      "createdAt": "2026-06-25T12:00:00",
      "updatedAt": "2026-06-25T12:00:00",
      "tags": []
    }
  ],
  "page": 0,
  "size": 25,
  "totalElements": 1,
  "totalPages": 1
}
```

#### Создание нового кейса (`POST`)
Создает новый кейс. Принимает данные в формате `multipart/form-data`.
```bash
curl -X POST -H "Cookie: token=TOKEN" \
-F 'case={"slug":"new-case","title":"Новый кейс","description":"Описание","difficulty":"EASY"};type=application/json' \
-F 'pdfFile=@/path/to/file.pdf' \
-F 'iconFile=@/path/to/icon.jpg' \
http://localhost:8080/api/admin/v1/createCase
```

#### Обновление кейса (`PUT`)
Обновляет данные кейса. Для удаления существующих файлов передайте `"removePdf": true` или `"removeIcon": true` в JSON-части `case`.
```bash
curl -X PUT -H "Cookie: token=TOKEN" \
-F 'case={"title":"Обновленное название","removePdf":true};type=application/json' \
-F 'iconFile=@/path/to/new_icon.jpg' \
http://localhost:8080/api/admin/v1/cases/42
```

### Управление тегами
#### Список тегов (`GET`)
Возвращает пагинированный список тегов с количеством привязанных кейсов.
```bash
curl -X GET -H "Cookie: token=TOKEN" "http://localhost:8080/api/admin/v1/tags?page=0&size=25&search=&sort=createdAt,desc"
```

#### Создание нового тега (`POST`)
Создает новый тег. Имя тега должно быть уникальным.
```bash
curl -X POST -H "Cookie: token=TOKEN" -H "Content-Type: application/json" \
-d '{"name": "Spring Boot"}' \
http://localhost:8080/api/admin/v1/tags
```

#### Обновление тега (`PATCH`)
Обновляет имя или статус активности тега.
```bash
curl -X PATCH -H "Cookie: token=TOKEN" -H "Content-Type: application/json" \
-d '{"name": "New Name", "active": true}' \
http://localhost:8080/api/admin/v1/tags/5
```

#### Деактивация тега (`PATCH`)
Делает тег неактивным (скрывает его из публичного списка), но не удаляет из базы данных.
```bash
curl -X PATCH -H "Cookie: token=TOKEN" http://localhost:8080/api/admin/v1/tags/5/deactivate
```

#### Активация тега (`PATCH`)
Делает тег активным (возвращает в публичный список).
```bash
curl -X PATCH -H "Cookie: token=TOKEN" http://localhost:8080/api/admin/v1/tags/5/activate
```

#### Привязка тега к кейсу (`POST`)
Связывает существующий тег с конкретным кейсом.
```bash
curl -X POST -H "Cookie: token=TOKEN" http://localhost:8080/api/admin/v1/cases/42/tags/5
```

#### Отвязка тега от кейса (`DELETE`)
Удаляет связь между тегом и кейсом.
```bash
curl -X DELETE -H "Cookie: token=TOKEN" http://localhost:8080/api/admin/v1/cases/42/tags/5
```

### Управление пользователями
#### Список пользователей (`GET`)
Возвращает пагинированный список пользователей с возможностью поиска и сортировки.
```bash
curl -X GET -H "Cookie: token=TOKEN" "http://localhost:8080/api/admin/v1/users?page=0&size=25&search=&sort=createdAt,desc"
```

#### Детальная информация о пользователе (`GET`)
Возвращает полную информацию о пользователе, включая профиль и статистику.
```bash
curl -X GET -H "Cookie: token=TOKEN" http://localhost:8080/api/admin/v1/users/42
```

#### Создание пользователя (`POST`)
Создает нового пользователя с заданными параметрами. Пользователь сразу считается верифицированным.
```bash
curl -X POST -H "Cookie: token=TOKEN" -H "Content-Type: application/json" \
-d '{"username":"admin_created","password":"SecurePass1!","email":"test@test.com","role":"USER","firstName":"Иван","lastName":"Иванов"}' \
http://localhost:8080/api/admin/v1/users
```

#### Изменение данных пользователя (`PATCH`)
Частичное обновление полей пользователя (роль, бан, верификация, личные данные). Рейтинг изменить нельзя.
```bash
curl -X PATCH -H "Cookie: token=TOKEN" -H "Content-Type: application/json" \
-d '{"role":"ADMIN","isVerified":true,"bannedUntil":"2026-12-31T23:59:59"}' \
http://localhost:8080/api/admin/v1/users/42
```

#### Удаление пользователя (`DELETE`)
Удаляет пользователя и все связанные с ним данные (решения, достижения, аватар) через каскадное удаление.
```bash
curl -X DELETE -H "Cookie: token=TOKEN" http://localhost:8080/api/admin/v1/users/42
```

---
## Геймификация и ИИ (`/api/text/v1`)
*Эндпоинты для интеграции с микросервисом ИИ и геймификации. Внешний микросервис обращается к этим методам для синхронизации игрового прогресса. Для всех запросов обязательна валидная Cookie сессии пользователя.*

### Проверка сессии (`GET`)
Вызывается микросервисом для проверки валидности текущей сессии пользователя.
```bash
curl -X GET -H "Cookie: token=TOKEN" http://localhost:8080/api/text/v1/checkCookie
```

### Сохранение решения (`POST`)
Принимает от микросервиса данные о решении кейса и обновляет суммарный рейтинг (score) игрока на основном сервере.
```bash
curl -X POST -H "Content-Type: application/json" -H "Cookie: token=TOKEN" \
-d '{"caseId": 4, "rating": 150, "solutionText": "текст запроса", "solutionResponse": "ответ ИИ"}' \
http://localhost:8080/api/text/v1/addScore
```

### История диалога с ИИ (`GET`)
Отдает микросервису пагинированную историю сообщений (запросы пользователя и ответы ИИ) в рамках конкретного кейса. Первая страница содержит самые старые сообщения.
```bash
curl -X GET -H "Cookie: token=TOKEN" "http://localhost:8080/api/text/v1/getChatSequence/1?page=0&size=25"
```
**Ответ (JSON):**
```json
{
  "items": [
    { "solutionId": 1, "rating": 8, "solutionText": "запрос1", "solutionResponse": "ответ1" },
    { "solutionId": 2, "rating": 9, "solutionText": "запрос2", "solutionResponse": "ответ2" }
  ],
  "page": 0,
  "size": 25,
  "totalElements": 2,
  "totalPages": 1
}
```

### Обработка нарушений (`POST`)
Микросервис сообщает о токсичном поведении пользователя. Запрос увеличивает счетчик предупреждений; при достижении лимита удаляет аккаунт.
```bash
curl -X POST -H "Cookie: token=TOKEN" http://localhost:8080/api/text/v1/processViolation
```

### Получение промпта (контекста) кейса (`GET`) *(Требует Cookie)*
Возвращает ID, название и контекстный промпт (инструкции для ИИ) для указанного кейса. Используется микросервисом ИИ для инициализации диалога.
```bash
curl -X GET -H "Cookie: token=TOKEN" http://localhost:8080/api/text/v1/cases/1/prompt
```
**Ответ (JSON) при успехе:**
```json
{
  "id": 1,
  "title": "Название кейса",
  "promptContextEn": "Ты опытный разработчик. Твоя задача..."
}
```

---
## Примеры ответов API и ошибок
Все ответы сервера приходят в формате JSON. При успешном запросе возвращается `"success": true`. При ошибках валидации (`@Valid`) возвращается HTTP 400 с текстом ошибки в формате `"<поле>: <сообщение>"` (например, `"name: Tag name cannot be empty"`).

**Базовый формат ответа:**
```json
{
  "success": false,
  "errorText": "Текст ошибки"
}
```

### Сводная таблица всех ошибок
| Категория | Текст ошибки (`errorText`) | Описание / Причина |
| :--- | :--- | :--- |
| **Успешно** | *(пустая строка)* | Запрос выполнен успешно (`"success": true`). |
| **Авторизация и Сессия** | `Incorrect password` | Неверный пароль. |
| &nbsp; | `Invalid username or password` | Неверное имя пользователя или пароль. |
| &nbsp; | `You are not logged in` | Пользователь не авторизован. |
| &nbsp; | `Please login first` | Требуется предварительный вход в систему. |
| &nbsp; | `You are already logged in` | Сессия уже активна для этого пользователя. |
| &nbsp; | `Session expired` | Срок действия текущей сессии истек. |
| &nbsp; | `Access denied: ADMIN role required` | У пользователя нет прав администратора (HTTP 403). |
| **Верификация** | `Verification session expired.` | Сессия верификации отсутствует или истекла. |
| &nbsp; | `Invalid or expired verification session.` | Неверная или истекшая сессия верификации. |
| &nbsp; | `Verification code is required` | Не передан код для верификации. |
| &nbsp; | `Invalid or expired verification code` | Код неверный, не существует или срок его действия истек. |
| **Статус, Модерация и Баны** | `User does not exist` | Пользователь не найден в системе (или в таблице лидеров). |
| &nbsp; | `User is now banned` | Пользователь получил бан (на 2 месяца) после накопления >2 предупреждений. |
| &nbsp; | `User no longer exists` | Аккаунт окончательно удален, так как количество банов достигло 3 и более. |
| &nbsp; | `User is still banned` | Действие заблокировано, так как срок текущего бана еще не истек. |
| &nbsp; | `Account is not verified` | Аккаунт не прошел верификацию. |
| **Валидация Username** | `Username cannot be empty` | Имя пользователя не может быть пустым. |
| &nbsp; | `Username cannot contain spaces` | В имени пользователя запрещены пробелы. |
| &nbsp; | `Username cannot be shorter than 3 characters` | Слишком короткое имя (минимум 3 символа). |
| &nbsp; | `Username cannot be longer than 20 characters` | Слишком длинное имя (максимум 20 символов). |
| &nbsp; | `Username is already taken` | Имя пользователя уже занято. |
| **Валидация Email** | `Email cannot be blank` | Поле email не может быть пустым. |
| &nbsp; | `This email address is invalid` | Некорректный формат email-адреса. |
| &nbsp; | `This email address is already taken` | Данный email уже зарегистрирован. |
| &nbsp; | `Invalid email format` | Некорректный формат email (при создании пользователя администратором). |
| &nbsp; | `Email is already taken` | Email уже зарегистрирован (при создании/обновлении пользователя). |
| **Валидация Пароля** | `Password cannot be empty` | Пароль не может быть пустым. |
| &nbsp; | `Password cannot be shorter than 8 characters` | Слишком короткий пароль (минимум 8 символов). |
| &nbsp; | `Password cannot be longer than 30 characters` | Слишком длинный пароль (максимум 30 символов). |
| &nbsp; | `Password must contain at least 1 digit` | Пароль должен содержать хотя бы одну цифру. |
| &nbsp; | `Password must contain at least 1 special character` | Пароль должен содержать хотя бы один спецсимвол. |
| **Загрузка файлов (S3)** | `File cannot be empty` | Отправлен пустой файл. |
| &nbsp; | `Invalid file extension. Allowed Extensions: [.pdf, .jpg, .jpeg, .png, .webp]` | Загружен файл с недопустимым расширением. |
| &nbsp; | `File size cannot exceed 5 MB` | Размер загружаемого файла превышает 5 МБ. |
| &nbsp; | `Invalid file content` | Файл поврежден или слишком мал для проверки заголовка. |
| &nbsp; | `File content does not match allowed formats` | Магические байты файла не соответствуют разрешенным форматам. |
| &nbsp; | `File extension does not match content` | Расширение файла не совпадает с его фактическим содержимым. |
| &nbsp; | `PDF files are not allowed for this upload` | Попытка загрузить PDF в поле, предназначенное только для изображений. |
| &nbsp; | `Could not upload file: ...` | Ошибка сети или S3 при загрузке. |
| **Управление тегами** | `Tag name cannot be empty` | Имя тега не может быть пустым. |
| &nbsp; | `Tag name must be between 1 and 100 characters` | Имя тега должно быть от 1 до 100 символов. |
| &nbsp; | `Tag with this name already exists` | Тег с таким именем уже существует в системе. |
| &nbsp; | `Tag not found` | Указанный ID тега не найден в базе данных. |
| &nbsp; | `Tag is already attached to this case` | Этот тег уже привязан к данному кейсу. |
| &nbsp; | `Tag is not attached to this case` | Попытка отвязать тег, который не был привязан к этому кейсу. |
| &nbsp; | `Request cannot be empty` | Тело запроса обновления тега пустое. |
| &nbsp; | `No fields to update` | В запросе обновления тега не передано ни имени, ни статуса активности. |
| **Управление кейсами** | `Case not found` | Указанный ID кейса не найден (общая ошибка). |
| &nbsp; | `Кейс не найден` | Кейс не найден при инкременте счетчика просмотров *(в коде может отображаться как "ÐšÐµÐ¹Ñ Ð½Ðµ Ð½Ð°Ð¹Ð´ÐµÐ½" из-за проблем с кодировкой)*. |
| &nbsp; | `Case with ID: X is not found` | Указанный ID кейса не найден в базе данных (вместо X будет реальный ID). |
| &nbsp; | `Invalid slug format` | Slug содержит недопустимые символы (только строчные буквы, цифры и дефис). |
| &nbsp; | `Slug too long (max 100)` | Slug превышает 100 символов. |
| &nbsp; | `Title too long (max 255)` | Название превышает 255 символов. |
| &nbsp; | `TitleEn too long (max 255)` | Английское название превышает 255 символов. |
| &nbsp; | `Description too long (max 1000)` | Краткое описание превышает 1000 символов. |
| &nbsp; | `Full description too long (max 5000)` | Полное описание превышает 5000 символов. |
| &nbsp; | `Solve time must be at least 1 min` | Время решения должно быть не менее 1 минуты. |
| &nbsp; | `Solve time cannot exceed 10000 min` | Время решения не может превышать 10000 минут. |
| &nbsp; | `Prompt context too long (max 2000)` | Промпт превышает 2000 символов. |
| **Пагинация и Сортировка** | `Page cannot be negative` | Номер страницы не может быть меньше нуля. |
| &nbsp; | `Size must be between 1 and 100` | Размер страницы должен быть от 1 до 100 элементов. |
| **Системные / Общие / Профиль** | `Invalid input data` | Переданы некорректные входные данные. |
| &nbsp; | `Invalid request` | Неверно сформированный HTTP-запрос (или null в объекте запроса). |
| &nbsp; | `Invalid city id` | Передан несуществующий или неверный ID города. |
| &nbsp; | `Invalid user ID` | Передан некорректный ID пользователя (например, <= 0). |
| &nbsp; | `Profile not found` | Профиль пользователя не найден. |
| &nbsp; | `User not found` | Пользователь не найден в базе данных. |
| &nbsp; | `User data not found` | Расширенные данные пользователя не найдены. |
| &nbsp; | `Failed to update email` | Внутренняя ошибка сервера при обновлении email. |
| &nbsp; | `Invalid validation method provided` | Передан некорректный метод верификации (ожидается EMAIL или TELEGRAM). |
| &nbsp; | `Internal server error` | Внутренняя ошибка сервера (Crash / Unhandled exception). |