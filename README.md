# Alfa-case5-backend

# Документация API

Спецификация серверных эндпоинтов приложения.

---

## Безопасность и доступ

* **`/api/v1/**`** — **Доступно всем**. Авторизация не требуется (кроме операций с личными данными).
* **`/api/text/**`** — **Только авторизованные пользователи**. Во все запросы нужно добавлять заголовок: `-H "Cookie: token=ТОКЕН"`.
* **`/api/admin/**`** — **Только администраторы**. Доступ ограничен ролью `ADMIN` *(в разработке)*.

---

## Профиль и Аутентификация (`/api/v1/auth`)

### Регистрация (`POST`)
Создание новой учетной записи.
```bash
curl -X POST -H "Content-Type: application/json" \
  -d '{"username":"1234","password":"tea_tea1","email":"someemail@gmail.com","birthdate":"16.10.2009","cityId":"1","status":"STUDENT10","firstName":"nameName","lastName":"last_name","middleName":"name","gender":"MALE"}' \
  http://localhost:8080/api/v1/auth/register
```

### Вход (`POST`)
Авторизация пользователя. При успехе сервер возвращает сессионную Cookie.
```bash
curl -X POST -H "Content-Type: application/json" \
  -d '{"username":"001","password":"tea_1teaaaa"}' \
  http://localhost:8080/api/v1/auth/login
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
  -d '{"firstName":"111","lastName":"kvq","middleName":"someOtherName","birthdate":"16.01.2000","cityId":"15","status":"UNDERGRADUATE"}' \
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
  "birthdate": "16.10.2009", "cityName": "Бородино", "firstName": "random", "lastName": "fun", "middleName": "naming", "nickName": "char", "gender": "MALE", "regionName": "Красноярский край", "score": 0, "placement": 0, "status": "STUDENT10"
}
```

### Поиск города по названию (`GET`)
Используется для фильтрации списка городов при поиске. Возвращает массив совпадений или пустой список.
```bash
curl -X GET http://localhost:8080/api/v1/site/searchLocation/моск
```

### Получить город пользователя (`GET`)
Возвращает название города и регион конкретного пользователя. Если ID не существует, возвращает `null`.
```bash
curl -X GET http://localhost:8080/api/v1/site/user/1/city
```

### Список всех городов (`GET`)
Запрос полного списка городов для выпадающих списков или фильтров.
```bash
curl -X GET http://localhost:8080/api/v1/site/getAllCities
```

### Получить аватар пользователя (`GET`)
Загружает и отображает изображение профиля по ID.
```bash
curl -X GET http://localhost:8080/api/v1/site/user/1/avatar
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
  -d '{"caseId":"4","rating":"150","solutionText":"текст запроса","solutionResponse":"ответ ИИ"}' \
  http://localhost:8080/api/text/v1/addScore
```

### История диалога с ИИ (`GET`)
Отдает микросервису историю сообщений (запросы пользователя и ответы ИИ) в рамках конкретного кейса.
```bash
curl -X GET -H "Cookie: token=TOKEN" http://localhost:8080/api/text/v1/getChatSequence/1
```
**Ответ (JSON):**
```json
[
  { "solutionText": "запрос1", "solutionResponse": "ответ1" },
  { "solutionText": "запрос2", "solutionResponse": "ответ2" }
]
```

### Обработка нарушений (`POST`)
Микросервис сообщает о токсичном поведении пользователя. Запрос увеличивает счетчик предупреждений; при достижении лимита удаляет аккаунт.
```bash
curl -X POST -H "Cookie: token=TOKEN" http://localhost:8080/api/text/v1/processViolation
```


---

### Примеры ответов API и ошибок

Все ответы сервера приходят в формате JSON. При успешном запросе возвращается `true`, при ошибке — `false` и соответствующий текст в поле `errorText`.

**Базовый формат ответа:**
```json
{
  "success": false,
  "errorText": "Текст ошибки"
}
```

#### Сводная таблица всех ошибок

| Категория | Текст ошибки (`errorText`) | Описание / Причина |
| :--- | :--- | :--- |
| **Успешно** | *(пустая строка)* | Запрос выполнен успешно (`"success": true`). |
| **Авторизация и Сессия** | `Incorrect password` | Неверный пароль. |
| | `Invalid username or password` | Неверное имя пользователя или пароль. |
| | `You are not logged in` | Пользователь не авторизован. |
| | `Please login first` | Требуется предварительный вход в систему. |
| | `You are already logged in` | Сессия уже активна для этого пользователя. |
| | `Session expired` | Срок действия текущей сессии истек. |
| **Статус пользователя** | `User does not exist` | Пользователь не найден в системе. |
| | `User no longer exists` | Пользователь больше не существует. |
| | `User is now banned` | Пользователь заблокирован в данный момент. |
| | `User is still banned` | Действие заблокировано, так как бан еще активен. |
| | `Invalid user status code` | Передан некорректный код статуса пользователя. |
| **Валидация Username** | `Username cannot be empty` | Имя пользователя не может быть пустым. |
| | `Username cannot contain spaces` | В имени пользователя запрещены пробелы. |
| | `Username cannot be shorter than 3 characters` | Слишком короткое имя (минимум 3 символа). |
| | `Username cannot be longer than 20 characters` | Слишком длинное имя (максимум 20 символов). |
| **Валидация Email** | `Email cannot be blank` | Поле email не может быть пустым. |
| | `This email address is invalid` | Некорректный формат email-адреса. |
| | `This email address is already taken` | Данный email уже зарегистрирован. |
| **Валидация Пароля** | `Password cannot be empty` | Пароль не может быть пустым. |
| | `Password cannot be shorter than 8 characters` | Слишком короткий пароль (минимум 8 символов). |
| | `Password cannot be longer than 30 characters` | Слишком длинный пароль (максимум 30 символов). |
| | `Password must contain at least 1 digit` | Пароль должен содержать хотя бы одну цифру. |
| | `Password must contain at least 1 special character` | Пароль должен содержать хотя бы один спецсимвол. |
| **Загрузка файлов** | `File cannot be empty` | Отправлен пустой файл. |
| | `Only JPEG/JPG images are allowed` | Разрешены только изображения с расширением JPEG/JPG. |
| | `File size cannot exceed 5MB` | Размер загружаемого файла превышает 5 МБ. |
| | `Failed to process image file` | Ошибка на стороне сервера при обработке картинки. |
| **Системные / Общие** | `Invalid input data` | Переданы некорректные входные данные. |
| | `Invalid request` | Неверно сформированный HTTP-запрос. |
| | `Invalid city id` | Передан несуществующий или неверный ID города. |
| | `Internal server error` | Внутренняя ошибка сервера (Crash / Unhandled exception). |

