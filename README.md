

# Alfa-case5-backend

# Документация API

Спецификация серверных эндпоинтов приложения.

> **Базовый адрес:** в примерах используется `http://localhost:8080` (локальный запуск без Docker).  
> При запуске через `docker-compose` приложение обычно доступно на внешнем порту `999`: `http://localhost:999`.  
> Nginx на порту `2479` проксирует только `/storage/...` и не используется для `/api/**`.

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
- **`/api/v1/**`** — **Доступно всем**. Авторизация не требуется (кроме операций с личными данными, где проверяется Cookie).
- **`/api/text/v1/**`** — **Требует Cookie**. На уровне Spring Security эндпоинты открыты (`permitAll`), но контроллеры вручную проверяют наличие и валидность заголовка `Cookie: token=ТОКЕН`.
- **`/api/admin/**`** — **Только администраторы**. Доступ строго ограничен ролью `ADMIN`. При отсутствии прав возвращается стандартизированный JSON-ответ с HTTP 403.

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
> **Защита от спама:** Действует ограничение на отправку писем — не более 3 запросов в час с одного IP-адреса.

### Верификация аккаунта (`POST`)
Подтверждение регистрации через код, отправленный на Email. Код передается в URL (path variable).
```bash
curl -X POST -H "Cookie: token=TOKEN" http://localhost:8080/api/v1/auth/verify/818018
```
> **Защита от перебора:** 
> - Не более 5 неверных попыток ввода кода для одного аккаунта (после этого аккаунт блокируется на 15 минут).
> - Не более 20 попыток верификации с одного IP-адреса в час.

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

### Восстановление доступа (Забыл логин/пароль)
*Эндпоинты не требуют авторизации (Cookie). Для защиты от перебора пользователей (User Enumeration) методы инициации всегда возвращают успех, даже если email или username не найдены в базе.*

#### Восстановление username (`POST`)
Отправляет username на указанный email. Лимит: не более 3 запросов в час с одного IP.
```bash
curl -X POST -H "Content-Type: application/json" \
-d '{"email":"someemail@gmail.com"}' \
http://localhost:8080/api/v1/auth/forgotUsername
```
**Ответ (JSON):**
```json
{
  "success": true,
  "errorText": ""
}
```

#### Инициация сброса пароля (`POST`)
Запрашивает сброс пароля. Проверяет связку email и username. Если они верны, генерирует 6-значный код и отправляет его на почту. Лимит: не более 3 запросов в час с одного IP.
```bash
curl -X POST -H "Content-Type: application/json" \
-d '{"email":"someemail@gmail.com", "username":"1234"}' \
http://localhost:8080/api/v1/auth/forgotPassword/init
```
**Ответ (JSON):**
```json
{
  "success": true,
  "errorText": ""
}
```

#### Подтверждение сброса пароля (`POST`)
Устанавливает новый пароль по 6-значному коду из письма. При успехе сбрасывает все активные сессии пользователя (требует повторного входа на всех устройствах). Защита от брутфорса: 5 неудачных попыток блокируют IP на 30 минут.
```bash
curl -X POST -H "Content-Type: application/json" \
-d '{"email":"someemail@gmail.com", "username":"1234", "code":123456, "newPassword":"newTea1!"}' \
http://localhost:8080/api/v1/auth/forgotPassword/confirm
```
**Ответ (JSON):**
```json
{
  "success": true,
  "errorText": "",
  "id": 8
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

### Повторная отправка кода верификации и смена email (`POST`)
Позволяет запросить новый код или ссылку для верификации, если пользователь еще не подтвердил аккаунт. Если переданный email отличается от того, что указан в базе, email будет обновлен (удобно, если при регистрации была допущена опечатка). Требует ввода логина и пароля. В ответе возвращается новая pre-auth сессия (Cookie).
```bash
curl -X POST -H "Content-Type: application/json" \
-d '{"username":"1234","password":"tea_tea1","email":"corrected_email@gmail.com","validationMethod":"EMAIL"}' \
http://localhost:8080/api/v1/auth/resendEmail
```
> **Примечание:** Если аккаунт уже верифицирован, пользователь забанен или введен неверный пароль, запрос будет отклонен.

**Ответ (JSON):**
```json
{
  "success": true,
  "errorText": "",
  "verification": "Verification code sent to your email",
  "id": 8
}
```
*(Если выбран метод `TELEGRAM`, в поле `verification` будет ссылка вида `https://t.me/bot_name?start=token`)*.

---

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

### Мои достижения (`GET`) *(Требует Cookie)*
Возвращает полный список всех достижений системы. Для каждого достижения указывается статус: получено ли оно текущим пользователем (и дата получения), или оно еще заблокировано (`obtainedAt: null`).
```bash
curl -X GET -H "Cookie: token=TOKEN" http://localhost:8080/api/v1/site/me/achievements
```
**Ответ (JSON):**
```json
[
  {
    "id": 1,
    "name": "Быстрый старт",
    "description": "Решите первый кейс менее чем за 30 минут",
    "iconUrl": "/achievements/quick_start.png",
    "obtainedAt": "2026-07-23T10:15:30.123"
  },
  {
    "id": 2,
    "name": "Стремительный взлёт",
    "description": "Решите 5 кейсов",
    "iconUrl": "/achievements/rapid_rise.png",
    "obtainedAt": null
  }
]
```

### Достижения пользователя (`GET`)
Публичный эндпоинт для просмотра статистики достижений любого пользователя по его ID. Возвращает список всех достижений со статусом их получения указанным пользователем.
```bash
curl -X GET http://localhost:8080/api/v1/site/1/achievements
```
**Ответ (JSON):**
*(Формат ответа идентичен эндпоинту `/me/achievements`, но отражает прогресс запрашиваемого пользователя. Если ID пользователя некорректен (<= 0), возвращается ошибка `Invalid user ID`. Если пользователь не найден, возвращается `Profile not found`).*

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

### Получение идеального решения кейса (`GET`) *(Требует Cookie)*

Возвращает `caseId` и `perfectSolution` для текущего пользователя.

Идеальное решение доступно только если пользователь завершил кейс. Завершение кейса фиксируется через `POST /api/text/v1/finishSolving/{caseId}`.

Если кейс не найден или скрыт (`isActive = false`), возвращается ошибка `Case not found`.  
Если пользователь еще не завершил кейс, возвращается ошибка `Case is not solved yet`.

```bash
curl -X GET -H "Cookie: token=TOKEN" http://localhost:8080/api/v1/cases/1/perfectSolution
```

**Ответ (JSON):**

```json
{
  "caseId": 1,
  "perfectSolution": "Текст идеального решения"
}
```

> **Примечание:** Если администратор не заполнил поле `perfectSolution`, значение `perfectSolution` может быть `null`.


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

## Избранное (`/api/v1/site/me/favorites`)
*Все эндпоинты в этом разделе требуют валидную сессионную Cookie пользователя.*

### Список избранных кейсов (`GET`)
Возвращает пагинированный список кейсов, добавленных текущим пользователем в избранное. Поддерживает текстовый поиск и сортировку.
```bash
curl -X GET -H "Cookie: token=TOKEN" "http://localhost:8080/api/v1/site/me/favorites?page=0&size=25&search=&sort=added_at,desc"
```
> **Примечание:** Параметр `sort` по умолчанию равен `added_at,desc` (сначала самые недавно добавленные). Доступные поля для сортировки: `added_at`, `title`, `views_count`, `difficulty`, `created_at`.

**Ответ (JSON):**
```json
{
  "items": [
    {
      "id": 42,
      "slug": "spring-security-case",
      "title": "Настройка Spring Security",
      "titleEn": "Spring Security Setup",
      "description": "Краткое описание кейса",
      "fullDescription": "Полное описание...",
      "difficulty": "MEDIUM",
      "averageSolveMin": 45,
      "pdfUrl": "cases/pdfs/uuid.pdf",
      "iconUrl": "cases/icons/uuid.jpg",
      "viewsCount": 120,
      "createdAt": "2026-06-25T12:00:00",
      "updatedAt": "2026-06-25T12:00:00",
      "addedAt": "2026-08-18T10:30:00",
      "tags": [
        {"id": 1, "name": "Java", "count": 15},
        {"id": 2, "name": "Spring", "count": 10}
      ]
    }
  ],
  "page": 0,
  "size": 25,
  "totalElements": 1,
  "totalPages": 1
}
```

### Добавление кейса в избранное (`POST`)
Добавляет указанный кейс в список избранных текущего пользователя.
```bash
curl -X POST -H "Cookie: token=TOKEN" http://localhost:8080/api/v1/site/me/favorites/42
```
**Ответ (JSON):**
```json
{
  "success": true,
  "errorText": "",
  "id": 8
}
```

### Удаление кейса из избранного (`DELETE`)
Удаляет указанный кейс из списка избранных текущего пользователя.
```bash
curl -X DELETE -H "Cookie: token=TOKEN" http://localhost:8080/api/v1/site/me/favorites/42
```
**Ответ (JSON):**
```json
{
  "success": true,
  "errorText": "",
  "id": 8
}
```

---
## Предпочтения пользователя (`/api/v1/site/me/preferences`)
*Все эндпоинты в этом разделе требуют валидную сессионную Cookie пользователя.*

### Получение предпочтений (`GET`)
Возвращает текущие настройки предпочтений пользователя (выбранная сложность и список любимых тегов с их статистикой). Если пользователь еще не настраивал предпочтения, вернется объект с `null` сложностью и пустым списком тегов.
```bash
curl -X GET -H "Cookie: token=TOKEN" http://localhost:8080/api/v1/site/me/preferences
```
**Ответ (JSON):**
```json
{
  "userId": 8,
  "preferredDifficulty": "MEDIUM",
  "preferredTags": [
    {
      "id": 1,
      "name": "Java",
      "active": true,
      "caseCount": 15
    },
    {
      "id": 3,
      "name": "Spring",
      "active": true,
      "caseCount": 8
    }
  ]
}
```

### Обновление предпочтений (`PATCH`)
Позволяет изменить предпочитаемую сложность и список тегов. 
* Если нужно просто обновить значения, передайте новые данные в полях `preferredDifficulty` и `preferredTags`.
* Если нужно **сбросить** (удалить) сохраненную сложность или теги, используйте флаги `removeDifficulty: true` и `removeTags: true` соответственно.
```bash
curl -X PATCH -H "Content-Type: application/json" -H "Cookie: token=TOKEN" \
-d '{"preferredDifficulty": "HARD", "preferredTags": [1, 3], "removeDifficulty": false, "removeTags": false}' \
http://localhost:8080/api/v1/site/me/preferences
```
**Ответ (JSON):**
```json
{
  "success": true,
  "errorText": "",
  "id": 8
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
> **Примечание:** В JSON-части `case` при создании можно дополнительно передать поле `perfectSolution` — текст идеального решения кейса. Максимальная длина — 10000 символов.


#### Обновление кейса (`PUT`)

Обновляет данные кейса. Для удаления существующих файлов передайте `"removePdf": true` или `"removeIcon": true` в JSON-части `case`.

Для добавления или обновления идеального решения передайте поле `perfectSolution`.

Для удаления идеального решения передайте `"removePerfectSolution": true`.

Если одновременно переданы `"removePerfectSolution": true` и `"perfectSolution"`, поле `perfectSolution` будет удалено.

```bash
curl -X PUT -H "Cookie: token=TOKEN" \
-F 'case={"title":"Обновленное название","perfectSolution":"Текст идеального решения"};type=application/json' \
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

### Просмотр решений пользователей (История диалогов)
*Эндпоинты для просмотра истории взаимодействия пользователя с ИИ (сохраненные решения).*

#### Все решения пользователя (`GET`)
Возвращает пагинированный список всех решений (историю диалогов с ИИ) конкретного пользователя по всем кейсам, отсортированных по времени (от старых к новым).
```bash
curl -X GET -H "Cookie: token=TOKEN" "http://localhost:8080/api/admin/v1/users/42/solutions?page=0&size=25"
```
**Ответ (JSON):**
```json
{
  "items": [
    {
      "solutionId": 101,
      "rating": 85,
      "solutionText": "Текст первого запроса пользователя к ИИ",
      "solutionResponse": "Текст первого ответа ИИ",
      "caseId": 5
    },
    {
      "solutionId": 102,
      "rating": 92,
      "solutionText": "Текст второго запроса",
      "solutionResponse": "Текст второго ответа",
      "caseId": 12
    }
  ],
  "page": 0,
  "size": 25,
  "totalElements": 2,
  "totalPages": 1
}

#### Решения пользователя по конкретному кейсу (`GET`)
Возвращает пагинированный список решений (историю диалога) конкретного пользователя в рамках указанного кейса.
```bash
curl -X GET -H "Cookie: token=TOKEN" "http://localhost:8080/api/admin/v1/users/42/solutions/case/5?page=0&size=25"
```
**Ответ (JSON):**
```json
{
  "items": [
    {
      "solutionId": 101,
      "rating": 85,
      "solutionText": "Текст первого запроса пользователя к ИИ",
      "solutionResponse": "Текст первого ответа ИИ",
      "caseId": 5
    },
    {
      "solutionId": 102,
      "rating": 92,
      "solutionText": "Текст второго запроса",
      "solutionResponse": "Текст второго ответа",
      "caseId": 12
    }
  ],
  "page": 0,
  "size": 25,
  "totalElements": 2,
  "totalPages": 1
}

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
-d '{"caseId": 4, "rating": 100, "solutionText": "текст запроса", "solutionResponse": "ответ ИИ"}' \
http://localhost:8080/api/text/v1/addScore
```
> **Ограничения:** `rating` должен быть от `0` до `100`. `caseId`, `solutionText` и `solutionResponse` обязательны и не могут быть пустыми.

### История диалога с ИИ (`GET`)
Отдает микросервису пагинированную историю сообщений (запросы пользователя и ответы ИИ) в рамках конкретного кейса. Первая страница содержит самые старые сообщения.
```bash
curl -X GET -H "Cookie: token=TOKEN" "http://localhost:8080/api/text/v1/getChatSequence/1?page=0&size=25"
```
**Ответ (JSON):**
```json
{
  "items": [
    {
      "solutionId": 101,
      "rating": 85,
      "solutionText": "Текст первого запроса пользователя к ИИ",
      "solutionResponse": "Текст первого ответа ИИ",
      "caseId": 5
    },
    {
      "solutionId": 102,
      "rating": 92,
      "solutionText": "Текст второго запроса",
      "solutionResponse": "Текст второго ответа",
      "caseId": 12
    }
  ],
  "page": 0,
  "size": 25,
  "totalElements": 2,
  "totalPages": 1
}

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

### Начало решения кейса (`POST`) *(Требует Cookie)*
Обозначает, что пользователь начал решение кейса. Сервер сохраняет время начала в Redis (запись автоматически удаляется через 24 часа). Это время будет использовано для вычисления затраченных минут при отправке решения (`addScore`). 
Операция идемпотентна: если таймер уже запущен (например, из-за двойного клика или обновления страницы), сервер не перезапишет время, а вернет существующую метку.
Если пользователь уже завершал (сдавался) этот кейс ранее, сервер вернет ошибку `400 Bad Request` с текстом `Case is already solved`.
```bash
curl -X POST -H "Cookie: token=TOKEN" http://localhost:8080/api/text/v1/startSolving/4
```
**Ответ (JSON):**
```json
{
  "active": true,
  "completed": false,
  "bestRating": 0,
  "timestamp": "2026-01-22T10:00:00.123Z"
}
```
*(Поле `timestamp` всегда возвращается в формате UTC (ISO-8601) для корректной синхронизации часовых поясов между клиентом и сервером).*

### Проверка статуса решения (`GET`) *(Требует Cookie)*
Возвращает текущий статус таймера решения для указанного кейса, а также информацию о том, был ли кейс завершен ранее (`completed`), и лучший полученный за него рейтинг (`bestRating`). 
Если решение не было начато, таймер истек (прошло 24 часа) или был сброшен, `active` будет `false`, а `timestamp` — `null`.
```bash
curl -X GET -H "Cookie: token=TOKEN" http://localhost:8080/api/text/v1/solvingState/4
```
**Ответ (JSON) при активном таймере:**
```json
{
  "active": true,
  "completed": false,
  "bestRating": 0,
  "timestamp": "2026-01-22T10:00:00.123Z"
}
```
**Ответ (JSON) при неактивном/отсутствующем таймере (но кейс был завершен ранее):**
```json
{
  "active": false,
  "completed": true,
  "bestRating": 85,
  "timestamp": null
}
```

### Завершение решения кейса (`POST`) *(Требует Cookie)*
Фиксирует осознанное завершение (или сдачу) кейса пользователем. Сервер удаляет запись о таймере из Redis и создает в базе данных отметку о том, что кейс был завершен. Наличие этой отметки препятствует новым попыткам решения данного кейса в будущем (например, после просмотра эталонного решения). 
Возвращает итоговый статус с флагом `completed: true` и лучшим рейтингом пользователя по этому кейсу.
```bash
curl -X POST -H "Cookie: token=TOKEN" http://localhost:8080/api/text/v1/finishSolving/4
```
**Ответ (JSON):**
```json
{
  "active": false,
  "completed": true,
  "bestRating": 85,
  "timestamp": null
}
```

---

## Примеры ответов API и ошибок
Все ответы сервера приходят в формате JSON. При успешном запросе возвращается `"success": true`. 

**Базовый формат ответа при ошибке:**
```json
{
  "success": false,
  "errorText": "Текст ошибки на английском языке"
}
```
> **Важно:** Благодаря глобальной обработке ошибок, даже системные сбои (401, 403, 404, 405, 413) возвращают этот единый формат.

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
| &nbsp; | `Unauthorized: <message>` | **HTTP 401.** Ответ при обращении к защищённому ресурсу без валидной сессии (вместо `<message>` — текст исключения). |
| &nbsp; | `Access Denied: <message>` | **HTTP 403.** Ответ при нехватке прав доступа (вместо `<message>` — текст исключения). |
| **Восстановление доступа** | `Invalid email or username` | Неверная связка email и username при подтверждении сброса пароля. |
| **Верификация** | `Verification session expired.` | Сессия верификации отсутствует или истекла. |
| &nbsp; | `Invalid or expired verification session.` | Неверная или истекшая сессия верификации. |
| &nbsp; | `Account is already verified` | Аккаунт уже прошел верификацию, повторная отправка кода невозможна. |
| &nbsp; | `Verification code is required` | Не передан код для верификации. |
| &nbsp; | `Invalid or expired verification code` | Код неверный, не существует или срок его действия истек. |
| **Лимиты и защита от перебора** | `Too many email requests. Try again later.` | Превышен лимит запросов на отправку email (макс. 3 в час с одного IP). |
| &nbsp; | `Too many failed attempts. Try again later.` | Превышено количество неверных попыток ввода кода. Аккаунт временно заблокирован (на 15 минут). |
| &nbsp; | `Too many verification attempts from your IP. Try again later.` | Превышен лимит попыток верификации с текущего IP-адреса (макс. 20 в час). |
| &nbsp; | `Too many failed attempts. Please try again later.` | Превышен лимит неудачных попыток ввода кода сброса пароля, IP заблокирован (HTTP 429). |
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
| &nbsp; | `Username is required` | Поле username не было передано в запросе. |
| **Валидация Email** | `Email cannot be blank` | Поле email не может быть пустым. |
| &nbsp; | `Email is required` | Поле email не было передано в запросе на повторную отправку. |
| &nbsp; | `This email address is invalid` | Некорректный формат email-адреса. |
| &nbsp; | `This email address is already taken` | Данный email уже зарегистрирован. |
| &nbsp; | `Invalid email format` | Некорректный формат email (при создании пользователя администратором). |
| &nbsp; | `Email is already taken` | Email уже зарегистрирован (при создании/обновлении пользователя). |
| **Валидация Пароля** | `Password cannot be empty` | Пароль не может быть пустым. |
| &nbsp; | `New password cannot be empty` | Поле нового пароля не может быть пустым. |
| &nbsp; | `Password cannot be shorter than 8 characters` | Слишком короткий пароль (минимум 8 символов). |
| &nbsp; | `Password cannot be longer than 30 characters` | Слишком длинный пароль (максимум 30 символов). |
| &nbsp; | `Password must contain at least 1 digit` | Пароль должен содержать хотя бы одну цифру. |
| &nbsp; | `Password must contain at least 1 special character` | Пароль должен содержать хотя бы один спецсимвол. |
| &nbsp; | `Password is required` | Поле password не было передано в запросе. |
| **Валидация запроса** | `Missing required parameter: <имя>` | **HTTP 400.** В запросе отсутствует обязательный `@RequestParam`. |
| &nbsp; | `Validation method is required` | Не указан метод верификации (ожидается EMAIL или TELEGRAM). |
| &nbsp; | `Invalid input data format` | **HTTP 400.** Ошибка парсинга JSON или несовпадение типов (например, строка вместо enum). |
| **Загрузка файлов (S3)** | `File size exceeds the maximum allowed limit` | **HTTP 413.** Размер файла превышает глобальный лимит Spring. |
| &nbsp; | `File cannot be empty` | Отправлен пустой файл. |
| &nbsp; | `File size cannot exceed 5 MB` | **HTTP 400.** Размер файла превышает бизнес-лимит в 5 МБ. |
| &nbsp; | `Invalid file extension. Allowed Extensions: [.pdf, .jpg, .jpeg, .png, .webp]` | Загружен файл с недопустимым расширением. |
| &nbsp; | `Invalid file content` | Файл поврежден или слишком мал для проверки заголовка. |
| &nbsp; | `File content does not match allowed formats` | Магические байты файла не соответствуют разрешенным форматам. |
| &nbsp; | `File extension does not match content` | Расширение файла не совпадает с его фактическим содержимым. |
| &nbsp; | `PDF files are not allowed for this upload` | Попытка загрузить PDF в поле, предназначенное только для изображений. |
| &nbsp; | `Failed to read file content` | Не удалось прочитать содержимое файла для проверки. |
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
| &nbsp; | `Кейс не найден` | Кейс не найден при инкременте счетчика просмотров. |
| &nbsp; | `Case with ID: X is not found` | Указанный ID кейса не найден в базе данных (вместо X будет реальный ID). |
| &nbsp; | `Case with this slug already exists` | Кейс с таким `slug` уже существует. |
| &nbsp; | `Case is not active` | Попытка добавить неактивный кейс в избранное. |
| &nbsp; | `Invalid slug format` | Slug содержит недопустимые символы (только строчные буквы, цифры и дефис). |
| &nbsp; | `Slug too long (max 100)` | Slug превышает 100 символов. |
| &nbsp; | `Slug cannot be empty` | Slug не может быть пустым. |
| &nbsp; | `Title too long (max 255)` | Название превышает 255 символов. |
| &nbsp; | `Case is already solved` | Этот кейс уже был решен прежде. |
| &nbsp; | `Perfect solution too long (max 10000)` | Поле `perfectSolution` превышает 10000 символов. |
| &nbsp; | `Case is not solved yet` | Пользователь еще не завершил кейс, поэтому идеальное решение недоступно. |
| &nbsp; | `Title cannot be empty` | Название кейса не может быть пустым. |
| &nbsp; | `TitleEn too long (max 255)` | Английское название превышает 255 символов. |
| &nbsp; | `Description too long (max 1000)` | Краткое описание превышает 1000 символов. |
| &nbsp; | `Description cannot be empty` | Описание обязательно для заполнения. |
| &nbsp; | `Full description too long (max 5000)` | Полное описание превышает 5000 символов. |
| &nbsp; | `Difficulty is required` | Сложность кейса обязательна для заполнения. |
| &nbsp; | `Solve time must be at least 1 min` | Время решения должно быть не менее 1 минуты. |
| &nbsp; | `Solve time cannot exceed 10000 min` | Время решения не может превышать 10000 минут. |
| &nbsp; | `Prompt context too long (max 2000)` | Промпт превышает 2000 символов. |
| **Интеграция ИИ** | `Invalid rating value` | Рейтинг отсутствует, меньше 0 или больше 100. |
| &nbsp; | `Invalid case ID` | Некорректный ID кейса в истории диалога. |
| **Системные / Маршрутизация** | `Resource not found` | **HTTP 404.** Запрос к несуществующему URL-адресу (эндпоинту). |
| &nbsp; | `Method not allowed` | **HTTP 405.** Использован неверный HTTP-метод для существующего URL. |
| **Пагинация и Сортировка** | `Page cannot be negative` | Номер страницы не может быть меньше нуля. |
| &nbsp; | `Search query is too long` | Поисковый запрос превышает максимально допустимую длину (200 символов). |
| &nbsp; | `Size must be between 1 and 100` | Размер страницы должен быть от 1 до 100 элементов. |
| **Системные / Общие / Профиль** | `Invalid input data` | Переданы некорректные входные данные. |
| &nbsp; | `Invalid request` | Неверно сформированный HTTP-запрос (или null в объекте запроса). |
| &nbsp; | `Invalid city id` | Передан несуществующий или неверный ID города. |
| &nbsp; | `Invalid user ID` | Передан некорректный ID пользователя (например, <= 0). |
| &nbsp; | `Profile not found` | Профиль пользователя не найден. |
| &nbsp; | `Invalid case ID` | Передан некорректный ID кейса (например, <= 0). |
| &nbsp; | `Profile could not be loaded` | Внутренняя ошибка при загрузке детального профиля пользователя. |
| &nbsp; | `User not found` | Пользователь не найден в базе данных. |
| &nbsp; | `User data not found` | Расширенные данные пользователя не найдены. |
| &nbsp; | `Failed to update email` | Внутренняя ошибка сервера при обновлении email. |
| &nbsp; | `Invalid validation method provided` | Передан некорректный метод верификации (ожидается EMAIL или TELEGRAM). |
| &nbsp; | `Internal server error` | Внутренняя ошибка сервера (Crash / Unhandled exception). |
| **Избранное (Favorites)** | `this case is already in your favourites` | Попытка добавить в избранное кейс, который там уже есть (HTTP 409). |
| &nbsp; | `this case is not in your favourites` | Попытка удалить из избранного кейс, которого там нет (HTTP 400). |
| **Предпочтения (Preferences)** | `Preferred tags list cannot exceed 15 items` | Передано больше 15 предпочитаемых тегов. |
| &nbsp; | `One or more tags are invalid or inactive` | В запросе обновления переданы ID несуществующих или деактивированных тегов. |
