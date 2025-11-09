# FocusMate

FocusMate is a Java 21 + Spring Boot productivity web application that combines a Pomodoro timer, smart task scheduling workflow (Scheduled ➜ Active ➜ Timed ➜ Completed), and real‑time analytics. It can run entirely with an in‑memory store (no database needed for first-time users) or connect to MySQL for persistence.

> Current default port: **8081** (set in `src/main/resources/application.properties`).

---
## ✨ Core Features

- ⏱️ Pomodoro / Custom Timer (Start, Pause/Resume, Stop, End Task)
- 📋 Task Lifecycle: Add ➜ Scheduled ➜ Activate ➜ Auto timer start ➜ Complete
- 🗑️ Task deletion with confirmation
- 📅 Deadlines via date picker
- 📊 Analytics: session totals, completion rate, streak, per-task target vs actual
- 💾 In-memory fallback (works even if DB is offline)
- 🎨 Modern responsive UI (blue gradient theme, animations)

---
## 🗂 Project Structure (Key Parts)

```
FocusMate/
├── pom.xml                        # Maven build (Spring Boot + web)
├── run.sh                         # Helper script to build & run
├── src/main/java/com/focusmate/
│   ├── FocusMateWebApplication.java   # Spring Boot entry point
│   ├── controller/                   # REST controllers (tasks, sessions, analytics)
│   ├── model/                        # Data models (Task, Session, Preset)
│   ├── dao/                          # DB access (unused if DB down)
│   ├── store/                        # MemoryStore (in‑memory fallback)
│   └── service/                      # Scheduling / future logic
├── src/main/resources/
│   ├── application.properties        # Config (port, DB exclusions)
│   └── static/                       # Frontend assets
│       ├── index.html
│       ├── style.css
│       └── app.js
└── README.md
```

---
## 🔧 Prerequisites

- Java 21 (check: `java -version`)
- Maven 3.9+
- (Optional) MySQL 8.x running on localhost:3306

If you do NOT have MySQL ready, the app will still work using `MemoryStore`.

---
## 🚀 Quick Start (In-Memory Mode)

```bash
git clone https://github.com/ShubSaurav/FocusMate.git
cd FocusMate
./run.sh   # builds & starts server on http://localhost:8081
```

Open: http://localhost:8081

Add tasks → Click a scheduled task to activate → Click active task (or Start) to begin timer.

---
## 🗄️ Enabling MySQL Persistence (Optional)

1. Start MySQL and create database:
	 ```sql
	 CREATE DATABASE focusmate;
	 ```
2. Edit `src/main/resources/application.properties` – remove autoconfig exclusions if present and set:
	 ```properties
	 spring.datasource.url=jdbc:mysql://localhost:3306/focusmate
	 spring.datasource.username=YOUR_USER
	 spring.datasource.password=YOUR_PASSWORD
	 ```
3. Restart the app (`Ctrl+C` then `./run.sh`).

If DB is unreachable, DAO falls back gracefully to `MemoryStore`.

---
## 🔌 API Endpoints (JSON)

| Method | Endpoint                     | Description                     |
|--------|------------------------------|---------------------------------|
| GET    | /api/tasks                   | All tasks                       |
| GET    | /api/tasks/scheduled         | Scheduled (sortable list)       |
| GET    | /api/tasks/completed         | Completed tasks                 |
| POST   | /api/tasks                   | Create task                     |
| PUT    | /api/tasks/{id}/status       | Update status                   |
| DELETE | /api/tasks/{id}              | Delete task                     |
| POST   | /api/sessions                | Save a focus session            |
| GET    | /api/analytics/summary       | Global productivity metrics     |
| GET    | /api/analytics/task/{id}     | Per-task analytics              |

Sample create task:
```bash
curl -X POST http://localhost:8081/api/tasks \
	-H 'Content-Type: application/json' \
	-d '{"title":"Study Algorithms","priority":3,"targetMinutes":50}'
```

---
## ⏱️ Timer Behavior

- Activate a task (click in Scheduled list) → task moves to Active area.
- Clicking the Active Task card or Start begins countdown.
- Pause retains remaining time; Resume continues.
- Stop saves session (without completing task).
- End Task marks task completed and logs final session.

---
## 🧪 Running Without Script

```bash
mvn clean package -DskipTests
mvn spring-boot:run
```

---
## 🛠 Troubleshooting

| Issue | Fix |
|-------|-----|
| Port 8081 in use | Kill old process: `lsof -ti:8081 | xargs kill -9` |
| Build fails | Ensure Java 21 & Maven installed; run `mvn -v` |
| DB errors | Remove/adjust datasource config or start MySQL |
| Frontend stale | Hard refresh (Ctrl+Shift+R) |
| API 404 | Confirm server running & port (8081) |

Enable debug logs: `mvn spring-boot:run -Dspring-boot.run.arguments=--debug`

---
## 🎨 UI Customization

Change theme colors in `style.css` root variables:
```css
:root {
	--primary-blue: #2196F3;
	--dark-blue: #1976D2;
	--light-blue: #BBDEFB;
}
```

Add a preset button in `index.html`:
```html
<button class="preset-btn" onclick="setTimer(45)">45 min</button>
```

---
## 📦 Roadmap Ideas

- User accounts / auth
- Dark mode toggle
- Weekly & monthly analytics
- Export (CSV / PDF)
- AI task prioritization refinement
- Push notifications & sounds
- Mobile (React Native / Flutter)

---
## 🤝 Contributing

Pull requests welcome. Suggested steps:
1. Fork repository
2. Create feature branch: `git checkout -b feature/xyz`
3. Commit changes: `git commit -m "Add xyz"`
4. Push: `git push origin feature/xyz`
5. Open PR on GitHub

---
## 📄 License

MIT License – use, modify, share freely.

---
## 💻 Author

Built with focus & enthusiasm by ShubSaurav.

> "Consistency beats intensity. One Pomodoro at a time." ✅

---
## 🌐 Cloning & Running (Share This)

```bash
git clone https://github.com/ShubSaurav/FocusMate.git
cd FocusMate
./run.sh
```

Enjoy productive sessions! 🎯
