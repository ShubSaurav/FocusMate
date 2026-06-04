# FocusMate

FocusMate is a polished productivity web app built with Java and Spring Boot. It helps users manage tasks, run a Pomodoro-style timer, log focus sessions, and view analytics—all from a clean browser interface.

The project works on both macOS and Windows, and supports:
- in-memory mode for fast setup
- MySQL persistence for real usage
- login/register auth and session-based access
- task lifecycle management with priority, deadlines, and completion tracking

---

## 🚀 Live Demo

Run locally at:

```text
http://localhost:8081
```

---

## 🌟 Core Features

- **Pomodoro timer** with Start, Pause / Resume, Stop, and End Task
- **Smart task workflow**: Scheduled → Active → Timed → Completed
- **Task analytics**: completion rate, average session, streak, activity calendar
- **MySQL persistence** with fallback to in-memory storage
- **User login/register** with session-based authentication
- **Modern responsive UI** with smooth floating icon animations

---

## 🧠 Tech Stack

- Java 21
- Spring Boot
- Maven
- MySQL / JDBC
- HTML, CSS, JavaScript
- REST API architecture

---

## ✅ Prerequisites

### Required

- Java 21 installed
- Maven 3.9 or newer

### Optional

- MySQL 8.x (or compatible) for persistence

If MySQL is not available, the app still runs using the built-in in-memory store.

---

## ⚡ Quick Start

### 1. Clone the repository

```bash
git clone https://github.com/ShubSaurav/FocusMate.git
cd FocusMate
```

### 2. Run the app

```bash
./run.sh
```

If you are on Windows and cannot run `run.sh`, use:

```bash
mvn clean package -DskipTests
mvn spring-boot:run
```

### 3. Open the app

```text
http://localhost:8081
```

---

## 🧩 MySQL Persistence Setup (Optional)

### 1. Create the database

```sql
CREATE DATABASE focusmate;
```

### 2. Update database config

Open `src/main/resources/application.properties` and update:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/focusmate
spring.datasource.username=YOUR_USER
spring.datasource.password=YOUR_PASSWORD
```

### 3. Restart the app

```bash
./run.sh
```

> If MySQL is down, the app continues working using the in-memory fallback.

---

## 🔧 Project Structure

```
FocusMate/
├── pom.xml
├── run.sh
├── src/main/java/com/focusmate/
│   ├── FocusMateWebApplication.java
│   ├── controller/
│   ├── dao/
│   ├── db/
│   ├── model/
│   ├── service/
│   ├── store/
│   └── util/
├── src/main/resources/
│   ├── application.properties
│   ├── schema.sql
│   └── static/
│       ├── index.html
│       ├── style.css
│       └── app.js
└── README.md
```

---

## 📌 How to Use

### Add a task
- Enter a title
- Choose priority
- Add an optional due date
- Set target focus minutes

### Track progress
- Click the task in the Scheduled list to activate it
- Use the timer controls to start, pause, stop, or complete the session
- Sessions are saved automatically

### View analytics
- Check completion rate
- See average session length
- Track streaks and activity history

---

## 🔌 API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register user |
| POST | `/api/auth/login` | Login user |
| POST | `/api/auth/logout` | Logout |
| GET | `/api/auth/me` | Current user |
| GET | `/api/tasks` | List all tasks |
| GET | `/api/tasks/scheduled` | Scheduled tasks |
| GET | `/api/tasks/completed` | Completed tasks |
| POST | `/api/tasks` | Create task |
| PUT | `/api/tasks/{id}/status` | Update status |
| DELETE | `/api/tasks/{id}` | Delete task |
| POST | `/api/sessions` | Save focus session |
| GET | `/api/analytics/summary` | Dashboard metrics |
| GET | `/api/analytics/task/{id}` | Task analytics |

### Example create task request

```bash
curl -X POST http://localhost:8081/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Read chapter 5","priority":4,"targetMinutes":45}'
```

---

## 🛠 Troubleshooting

| Problem | Solution |
|---|---|
| App fails to start | Verify Java 21 and Maven installation |
| Port 8081 in use | `lsof -ti:8081 | xargs kill -9` |
| MySQL connection error | Start MySQL and verify credentials |
| UI not updating | Hard refresh browser with Ctrl+Shift+R |

---

## 🎨 Customization

### Change colors

Edit `src/main/resources/static/style.css`:

```css
:root {
  --primary-blue: #2196F3;
  --dark-blue: #1976D2;
  --light-blue: #BBDEFB;
}
```

### Add timer preset

Edit `src/main/resources/static/index.html`:

```html
<button class="preset-btn" onclick="setTimer(45)">45 min</button>
```

---

## 🤝 Contributing

1. Fork the repo
2. Create a branch: `git checkout -b feature/your-feature`
3. Commit changes: `git commit -m "Add feature"`
4. Push: `git push origin feature/your-feature`
5. Open a pull request

---

## 📄 License

MIT License

---

## Author

Built by **ShubSaurav**.

> "Build focus, ship value."

Enjoy productive sessions! 🎯
