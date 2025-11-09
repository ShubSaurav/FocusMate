# 🎯 FocusMate - Web Application

A beautiful web-based Pomodoro Timer and Task Manager with smart scheduling and analytics.

## 🚀 Features

- **⏱️ Pomodoro Timer**: Customizable focus sessions with presets (25/50/15 min) and custom durations
- **📋 Smart Task Scheduler**: AI-powered task prioritization based on urgency, priority, and deadlines
- **✅ Task Management**: Create, track, and complete tasks with target time goals
- **📊 Analytics Dashboard**: Visual progress tracking with target vs actual time comparison
- **🎨 Modern UI**: Beautiful blue-themed responsive design with hover effects and animations

## 📁 Project Structure

```
FocusMate/
├── src/main/
│   ├── java/com/focusmate/
│   │   ├── FocusMateWebApplication.java    # Spring Boot main app
│   │   ├── controller/                     # REST API controllers
│   │   ├── model/                          # Data models
│   │   ├── dao/                            # Database access
│   │   └── service/                        # Business logic
│   └── resources/
│       ├── static/                         # Web frontend
│       │   ├── index.html                  # Main page
│       │   ├── style.css                   # Modern CSS
│       │   └── app.js                      # JavaScript logic
│       └── application.properties          # Configuration
├── pom.xml                                 # Original desktop app
└── pom-web.xml                             # Web app dependencies
```

## 🛠️ Setup Instructions

### Prerequisites
- Java 21 (LTS)
- Maven 3.9+
- MySQL 8.0+

### Database Setup

1. Start MySQL server
2. Create database:
```sql
CREATE DATABASE focusmate;
USE focusmate;

-- Tables will be auto-created by the existing DAO classes
```

### Running the Application

#### Option 1: Using Spring Boot (Full Web App)

1. Copy web dependencies:
```bash
cp pom-web.xml pom.xml
```

2. Build the project:
```bash
mvn clean install
```

3. Run Spring Boot:
```bash
mvn spring-boot:run
```

4. Open browser:
```
http://localhost:8080
```

#### Option 2: Simple Static Web Server (Frontend Only)

If Spring Boot dependencies fail, you can run just the frontend:

```bash
# Install a simple HTTP server (Node.js)
npm install -g http-server

# Serve the static files
cd src/main/resources/static
http-server -p 8080
```

Then open: `http://localhost:8080`

## 🎨 Features Overview

### Pomodoro Timer
- **Live countdown** with pulsating animation
- **Task selection** from your task list
- **Preset buttons**: Quick 25, 50, or 15-minute sessions
- **Custom duration**: Set any time between 1-120 minutes
- **Session tracking**: Automatically saves to database
- **Alert animation**: Red pulsing effect in last 60 seconds

### Task Management
- **Add tasks** with title, priority (1-5), due date, target minutes
- **Smart scheduling**: Auto-sorts by urgency, priority, and remaining time
- **Scheduled view**: See tasks ordered by importance
- **Completed view**: Track finished work
- **One-click completion**: Mark tasks done

### Analytics
- **Completion rate**: Track your productivity percentage
- **Average session**: See your typical focus duration
- **Streak counter**: Days of consistent work
- **Progress bars**: Visual target vs actual time for each task
- **Daily tips**: Productivity advice that changes daily

## 🔧 Configuration

Edit `src/main/resources/application.properties`:

```properties
# Change port
server.port=8080

# MySQL connection
spring.datasource.url=jdbc:mysql://localhost:3306/focusmate
spring.datasource.username=root
spring.datasource.password=yourpassword
```

## 📱 UI Features

- **Responsive design**: Works on desktop, tablet, mobile
- **Modern animations**: Fade-in, slide, pulse effects
- **Hover effects**: Interactive buttons with smooth transitions
- **Color theme**: Professional blue gradient (#2196F3)
- **Card layouts**: Clean, organized sections
- **Real-time updates**: Live timer and clock

## 🔄 API Endpoints

```
GET  /api/tasks              - Get all tasks
GET  /api/tasks/scheduled    - Get smart-sorted tasks
GET  /api/tasks/completed    - Get completed tasks
POST /api/tasks              - Create new task
PUT  /api/tasks/{id}/status  - Update task status

POST /api/sessions           - Save timer session
GET  /api/analytics/summary  - Get overall stats
GET  /api/analytics/task/{id} - Get task-specific analytics
```

## 🎯 Usage Tips

1. **Start your day**: Add 3-5 priority tasks
2. **Use Auto-Arrange**: Let the smart scheduler optimize your order
3. **Focus sessions**: Start timer for your top task
4. **Take breaks**: Follow 25-5 or 50-10 Pomodoro technique
5. **Track progress**: Check analytics to see target vs actual time
6. **Mark complete**: Click tasks to mark them done

## 🚧 Troubleshooting

**Spring Boot won't start?**
- Ensure Java 21 is installed: `java -version`
- Check MySQL is running
- Verify database credentials in `application.properties`

**Frontend not loading?**
- Check browser console (F12) for errors
- Try clearing cache (Ctrl+Shift+R)
- Verify files are in `src/main/resources/static/`

**Database connection failed?**
- Start MySQL: `mysql.server start` (Mac) or `sudo systemctl start mysql` (Linux)
- Check credentials and database name
- Ensure MySQL port 3306 is not blocked

## 🎨 Customization

### Change Color Theme

Edit `src/main/resources/static/style.css`:

```css
:root {
    --primary-blue: #2196F3;     /* Your primary color */
    --dark-blue: #1976D2;        /* Darker shade */
    --light-blue: #90CAF9;       /* Lighter shade */
}
```

### Add New Presets

Edit `src/main/resources/static/index.html`:

```html
<button class="preset-btn" onclick="setTimer(45)">45 min</button>
```

## 📊 Future Enhancements

- [ ] User authentication & multi-user support
- [ ] Cloud sync across devices
- [ ] Mobile app (React Native)
- [ ] Slack/Teams integration
- [ ] AI task prioritization
- [ ] Weekly/monthly reports
- [ ] Export data to CSV/PDF
- [ ] Dark mode toggle
- [ ] Custom sound notifications
- [ ] Pomodoro statistics graphs

## 📄 License

MIT License - Feel free to use and modify!

## 👨‍💻 Author

Built with ❤️ for productivity enthusiasts

---

**Happy focusing! 🎯⏱️**
