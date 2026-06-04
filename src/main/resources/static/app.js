// API Base URL - use current origin so it works on any port/environment
const API_BASE = `${window.location.origin}/api`;

// Timer State
let timerInterval = null;
let remainingSeconds = 0;
let isRunning = false;
let isPaused = false;
let selectedTaskId = null;
let activeTask = null;
let startTime = null;
let pausedTime = 0;
let currentUser = null;
let authMode = 'login';

// Calendar State
let currentCalendarDate = new Date();
let activityData = {}; // Format: { 'YYYY-MM-DD': minutesLogged }

// Initialize app
document.addEventListener('DOMContentLoaded', () => {
    updateClock();
    setInterval(updateClock, 1000);
    initPriorityButtons();
    setDailyTip();
    checkAuth();
});

async function checkAuth() {
    try {
        const response = await fetch(`${API_BASE}/auth/me`, { credentials: 'include' });
        if (!response.ok) throw new Error('Not authenticated');
        currentUser = await response.json();
        showApp();
        await Promise.all([refreshTasks(), loadAnalytics(), loadActivityData().then(renderActivityCalendar)]);
    } catch (err) {
        showLogin();
    }
}

function showLogin(message, type = 'error') {
    document.getElementById('loginWrapper').classList.remove('hidden');
    document.getElementById('appWrapper').classList.add('hidden');
    setAuthMessage(message || '', type);
}

function setAuthMessage(message, type = 'error') {
    const authMessage = document.getElementById('authMessage');
    authMessage.textContent = message || '';
    authMessage.classList.toggle('success', type === 'success');
    authMessage.classList.toggle('error', type !== 'success');
}

function showApp() {
    document.getElementById('loginWrapper').classList.add('hidden');
    document.getElementById('appWrapper').classList.remove('hidden');
    setAuthMessage('', 'success');
    updateUserHeader();
}

function updateUserHeader() {
    document.getElementById('userEmail').textContent = currentUser ? `${currentUser.name || currentUser.email}` : 'Not logged in';
}

function toggleRegister(event) {
    event.preventDefault();
    authMode = authMode === 'login' ? 'register' : 'login';
    const authNameField = document.getElementById('authNameField');
    const authSubmitBtn = document.getElementById('authSubmitBtn');
    const authSwitch = document.querySelector('.auth-switch');
    if (authMode === 'register') {
        authNameField.classList.remove('hidden');
        authSubmitBtn.textContent = 'Create account';
        authSwitch.innerHTML = 'Already have an account? <a href="#" onclick="toggleRegister(event)">Login</a>';
    } else {
        authNameField.classList.add('hidden');
        authSubmitBtn.textContent = 'Login';
        authSwitch.innerHTML = 'New here? <a href="#" onclick="toggleRegister(event)">Create an account</a>';
    }
    setAuthMessage('', 'success');
}

async function submitAuth(event) {
    event.preventDefault();
    const email = document.getElementById('authEmail').value.trim();
    const password = document.getElementById('authPassword').value;
    const name = document.getElementById('authName').value.trim();
    const body = authMode === 'register'
        ? { email, password, name }
        : { email, password };

    if (!email || !password || (authMode === 'register' && !name)) {
        setAuthMessage('Please fill in all required fields.', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/auth/${authMode}`, {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        const data = await response.json();
        if (!response.ok) {
            setAuthMessage(data.error || 'Authentication failed.', 'error');
            return;
        }
        currentUser = data;
        showApp();
        document.getElementById('authForm').reset();
        if (authMode === 'register') {
            toggleRegister(new Event('dummy')); // reset to login view after registration
        }
        await Promise.all([refreshTasks(), loadAnalytics(), loadActivityData().then(renderActivityCalendar)]);
    } catch (error) {
        setAuthMessage('Unable to authenticate. Please try again.', 'error');
        console.error('Auth error', error);
    }
}

async function logout() {
    try {
        await fetch(`${API_BASE}/auth/logout`, {
            method: 'POST',
            credentials: 'include'
        });
    } catch (error) {
        console.error('Logout failed', error);
    }
    currentUser = null;
    showLogin('You have been signed out.', 'success');
}

// Priority buttons init
function setPriorityActive(val) {
    const hidden = document.getElementById('taskPriority');
    const buttons = document.querySelectorAll('.priority-btn');
    buttons.forEach(btn => btn.classList.toggle('active', btn.dataset.value === String(val)));
    if (hidden) hidden.value = val;
}

function initPriorityButtons() {
    const buttons = document.querySelectorAll('.priority-btn');
    if (!buttons || buttons.length === 0) return;
    buttons.forEach(btn => {
        btn.addEventListener('click', () => setPriorityActive(btn.dataset.value));
    });
    const initial = document.getElementById('taskPriority')?.value || 3;
    setPriorityActive(initial);
}

// Clock Update
function updateClock() {
    const now = new Date();
    const timeString = now.toLocaleTimeString('en-US', { 
        hour: '2-digit', 
        minute: '2-digit',
        hour12: true 
    });
    document.getElementById('currentTime').textContent = timeString;
}

// Timer Functions
function setTimer(minutes) {
    if (!isRunning) {
        remainingSeconds = minutes * 60;
        updateTimerDisplay();
        document.getElementById('customMinutes').value = minutes;
    }
}

function setCustomTimer() {
    const minutes = parseInt(document.getElementById('customMinutes').value) || 25;
    setTimer(minutes);
}

function startTimer() {
    if (isRunning && !isPaused) return;

    if (!isPaused && remainingSeconds === 0) {
        const minutes = parseInt(document.getElementById('customMinutes').value) || 25;
        remainingSeconds = minutes * 60;
    }

    if (!activeTask && !isPaused) {
        showNotification('⚠️ No Active Task', 'Timer started without a task. Create or activate one to track work against it.');
    }

    if (!isPaused) {
        startTime = new Date();
        pausedTime = 0;
    } else {
        // Resuming from pause
        const pauseDuration = new Date() - startTime;
        startTime = new Date(startTime.getTime() + pauseDuration);
        isPaused = false;
    }
    
    isRunning = true;
    document.getElementById('startBtn').disabled = true;
    document.getElementById('pauseBtn').disabled = false;
    document.getElementById('stopBtn').disabled = false;
    document.getElementById('doneBtn').disabled = false;

    const timerCircle = document.querySelector('.timer-circle');
    timerCircle.classList.add('active');
    timerCircle.classList.remove('paused', 'low-time');
    timerCircle.style.background = '';
    
    timerInterval = setInterval(() => {
        if (remainingSeconds > 0) {
            remainingSeconds--;
            updateTimerDisplay();
            
            if (remainingSeconds <= 60) {
                timerCircle.classList.add('low-time');
            } else {
                timerCircle.classList.remove('low-time');
            }
        } else {
            endTask();
            showNotification('⏰ Time\'s up!', 'Great work! Take a break!');
        }
    }, 1000);
}

function pauseTimer() {
    if (!isRunning || isPaused) return;
    
    clearInterval(timerInterval);
    isPaused = true;
    isRunning = false;
    
    document.getElementById('startBtn').disabled = false;
    document.getElementById('pauseBtn').disabled = true;
    document.getElementById('startBtn').textContent = '▶️ Resume';

    const timerCircle = document.querySelector('.timer-circle');
    timerCircle.classList.remove('active');
    timerCircle.classList.add('paused');
    
    showNotification('⏸️ Paused', 'Timer paused. Click Resume to continue.');
}

function stopTimer(manual = true) {
    if (!isRunning && !isPaused) return;
    
    clearInterval(timerInterval);
    
    const endTime = new Date();
    const actualMinutes = isPaused 
        ? Math.round((endTime - startTime - pausedTime) / 60000)
        : Math.round((endTime - startTime) / 60000);
    const plannedMinutes = Math.ceil((remainingSeconds + actualMinutes * 60) / 60);
    
    // Save session to backend
    if (actualMinutes > 0) {
        saveSession(selectedTaskId, startTime, endTime, plannedMinutes, actualMinutes, manual);
    }
    
    resetTimer();
    
    showNotification(
        '⏹️ Session stopped',
        actualMinutes > 0 ? `${actualMinutes} minutes tracked!` : 'Session cancelled'
    );
}

function completeSession() {
    endTask();
}

function resetTimer() {
    isRunning = false;
    isPaused = false;
    remainingSeconds = 0;
    pausedTime = 0;
    
    document.getElementById('startBtn').disabled = false;
    document.getElementById('pauseBtn').disabled = true;
    document.getElementById('stopBtn').disabled = true;
    document.getElementById('doneBtn').disabled = true;
    document.getElementById('startBtn').textContent = '▶️ Start';
    
    updateTimerDisplay();
    
    const timerCircle = document.querySelector('.timer-circle');
    timerCircle.classList.remove('active', 'paused', 'low-time');
    timerCircle.style.background = '';
}

function updateTimerDisplay() {
    const minutes = Math.floor(remainingSeconds / 60);
    const seconds = remainingSeconds % 60;
    const display = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
    document.getElementById('timerDisplay').textContent = display;
}

async function saveSession(taskId, start, end, planned, actual, manual) {
    try {
        const response = await fetch(`${API_BASE}/sessions`, {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                taskId: taskId ? parseInt(taskId) : null,
                start: start.toISOString(),
                end: end.toISOString(),
                plannedMinutes: planned,
                actualMinutes: actual,
                stoppedManually: manual
            })
        });
        
        if (response.ok) {
            console.log('Session saved successfully');
            // Update activity data for the calendar
            await updateActivityData(start, actual);
            // Refresh analytics and tasks
            await Promise.all([loadAnalytics(), refreshTasks()]);
        }
    } catch (error) {
        console.error('Error saving session:', error);
    }
}

// Task Management
async function addTask(event) {
    event.preventDefault();
    
    const title = document.getElementById('taskTitle').value.trim();
    const priority = parseInt(document.getElementById('taskPriority').value) || 3;
    const dueDate = document.getElementById('taskDueDate').value;
    const targetMinutes = parseInt(document.getElementById('taskTarget').value) || 60;
    
    if (!title) {
        showNotification('⚠️ Error', 'Task title is required!');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/tasks`, {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                title,
                priority,
                dueDate: dueDate || null,
                targetMinutes,
                status: 'PENDING'
            })
        });
        
        if (response.ok) {
            const task = await response.json();
            document.getElementById('taskForm').reset();
            document.getElementById('taskPriority').value = 3;
            document.getElementById('taskTarget').value = 60;
            setPriorityActive(3);
            
            showNotification('✅ Task Added', `"${title}" added to scheduled tasks!`);
            await refreshTasks();
        }
    } catch (error) {
        console.error('Error adding task:', error);
        showNotification('❌ Error', 'Failed to add task');
    }
}

// Activate a task (move from scheduled to active)
async function activateTask(task) {
    activeTask = task;
    selectedTaskId = task.id;
    
    // Update active task container
    const container = document.getElementById('activeTaskContainer');
    container.innerHTML = `
        <div class="active-task-card" onclick="startTaskTimer()">
            <h3>${task.title}</h3>
            <div class="task-details">
                <span class="detail-badge">⭐ Priority: ${task.priority}</span>
                <span class="detail-badge">🎯 ${task.targetMinutes} min</span>
                ${task.dueDate ? `<span class="detail-badge">📅 ${task.dueDate}</span>` : ''}
            </div>
            <p class="click-to-start">👆 Click here to start working on this task</p>
        </div>
    `;
    
    // Set timer to task's target minutes
    document.getElementById('customMinutes').value = task.targetMinutes;
    setTimer(task.targetMinutes);
    
    // Mark as IN_PROGRESS to remove from scheduled list
    try {
        await fetch(`${API_BASE}/tasks/${task.id}/status`, {
            method: 'PUT',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status: 'IN_PROGRESS' })
        });
        await refreshTasks(); // Refresh to remove from scheduled
    } catch (error) {
        console.error('Error updating task status:', error);
    }
    
    showNotification('🎯 Task Activated', `Click the active task to start timer!`);
}

// Start timer when active task is clicked
function startTaskTimer() {
    if (!activeTask) return;
    startTimer();
    showNotification('▶️ Timer Started', `Working on: ${activeTask.title}`);
}

// End task (complete and move to completed)
async function endTask() {
    if (!isRunning && !isPaused) return;
    
    clearInterval(timerInterval);
    
    const endTime = new Date();
    const actualMinutes = Math.round((endTime - startTime - pausedTime) / 60000);
    const plannedMinutes = Math.ceil((remainingSeconds + actualMinutes * 60) / 60);
    
    // Save session to backend
    if (actualMinutes > 0) {
        await saveSession(selectedTaskId, startTime, endTime, plannedMinutes, actualMinutes, false);
    }
    
    // Mark task as done
    if (selectedTaskId) {
        await markTaskComplete(parseInt(selectedTaskId));
        showNotification('✅ Task Completed!', `Task no. ${selectedTaskId} completed! 🎉 ${actualMinutes} minutes tracked.`);
    }
    
    // Clear active task
    activeTask = null;
    document.getElementById('activeTaskContainer').innerHTML = '<p class="empty-state">Click a scheduled task to start working on it</p>';
    
    resetTimer();
    await refreshTasks();
}

async function refreshTasks() {
    try {
        const scheduledRes = await fetch(`${API_BASE}/tasks/scheduled`, { credentials: 'include' });
        let scheduledTasks = await scheduledRes.json();
        // Sort by priority descending (5 -> 1)
        scheduledTasks = (scheduledTasks || []).sort((a, b) => (parseInt(b.priority||0) - parseInt(a.priority||0)));
        renderTasks(scheduledTasks, 'scheduledTasks', false);
        
        // Load completed tasks
        const completedRes = await fetch(`${API_BASE}/tasks/completed`, { credentials: 'include' });
        const completedTasks = await completedRes.json();
        renderTasks(completedTasks, 'completedTasks', true);
        document.getElementById('completedCount').textContent = completedTasks.length;
        
        // Update task selects
        updateTaskSelects(scheduledTasks);
    } catch (error) {
        console.error('Error loading tasks:', error);
        renderTasks([], 'scheduledTasks', false);
        renderTasks([], 'completedTasks', true);
        document.getElementById('completedCount').textContent = '0';
        updateTaskSelects([]);
    }
}

function renderTasks(tasks, containerId, isCompleted) {
    const container = document.getElementById(containerId);
    container.innerHTML = '';
    
    if (tasks.length === 0) {
        container.innerHTML = '<p style="text-align:center; color:#999;">No tasks yet</p>';
        return;
    }
    
    tasks.forEach(task => {
        const taskEl = document.createElement('div');
        // Add priority class for color coding (1=low, 5=high)
        const priorityClass = `priority-${task.priority || 3}`;
        taskEl.className = 'task-item' + (isCompleted ? ' completed' : ` ${priorityClass}`);
        
        const taskContent = document.createElement('div');
        taskContent.className = 'task-content';
        taskContent.innerHTML = `
            <div class="task-title">${task.title}</div>
            <div class="task-meta">
                <span>⭐ Priority: ${task.priority}</span>
                <span>🎯 ${task.targetMinutes} min</span>
                ${task.dueDate ? `<span>📅 ${task.dueDate}</span>` : ''}
            </div>
        `;
        
        if (!isCompleted) {
            // Click scheduled task to activate it
            taskContent.onclick = () => activateTask(task);
            
            // Add delete button
            const deleteBtn = document.createElement('button');
            deleteBtn.className = 'task-delete-btn';
            deleteBtn.innerHTML = '🗑️';
            deleteBtn.onclick = (e) => {
                e.stopPropagation();
                deleteTask(task.id);
            };
            
            taskEl.appendChild(taskContent);
            taskEl.appendChild(deleteBtn);
        } else {
            taskEl.appendChild(taskContent);
        }
        
        container.appendChild(taskEl);
    });
}

// Delete a task
async function deleteTask(taskId) {
    if (!confirm('Are you sure you want to delete this task?')) {
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/tasks/${taskId}`, {
            method: 'DELETE',
            credentials: 'include'
        });
        
        if (response.ok) {
            showNotification('🗑️ Task Deleted', 'Task removed successfully');
            
            // Clear active task if it was the deleted one
            if (activeTask && activeTask.id === taskId) {
                activeTask = null;
                selectedTaskId = null;
                document.getElementById('activeTaskContainer').innerHTML = 
                    '<p class="empty-state">Click a scheduled task to start working on it</p>';
            }
            
            await refreshTasks();
        }
    } catch (error) {
        console.error('Error deleting task:', error);
        showNotification('❌ Error', 'Failed to delete task');
    }
}

function updateTaskSelects(tasks) {
    const select = document.getElementById('analyticsTaskSelect');
    const currentValue = select.value;
    select.innerHTML = '<option value="">Select task for details...</option>';
    tasks.forEach(task => {
        const option = document.createElement('option');
        option.value = task.id;
        option.textContent = task.title;
        select.appendChild(option);
    });
    select.value = currentValue;
}

async function markTaskComplete(taskId) {
    try {
        await fetch(`${API_BASE}/tasks/${taskId}/status`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status: 'DONE' })
        });
        showNotification('✅ Complete!', 'Task marked as done');
        refreshTasks();
        loadAnalytics();
    } catch (error) {
        console.error('Error updating task:', error);
    }
}

// Analytics
async function loadAnalytics() {
    try {
        const response = await fetch(`${API_BASE}/analytics/summary`, { credentials: 'include' });
        const data = await response.json();
        
        document.getElementById('todaySessions').textContent = data.todaySessions || 0;
        document.getElementById('totalTime').textContent = formatTime(data.totalMinutes || 0);
        document.getElementById('completionRate').textContent = `${data.completionRate || 0}%`;
        document.getElementById('avgSession').textContent = `${data.avgSession || 0} min`;
        document.getElementById('streak').textContent = `${data.streak || 0} days`;
    } catch (error) {
        console.error('Error loading analytics:', error);
    }
}

async function updateAnalytics() {
    const taskId = document.getElementById('analyticsTaskSelect').value;
    if (!taskId) return;
    
    try {
        const response = await fetch(`${API_BASE}/analytics/task/${taskId}`, { credentials: 'include' });
        const data = await response.json();
        renderProgressBars(data);
    } catch (error) {
        console.error('Error loading task analytics:', error);
    }
}

function renderProgressBars(data) {
    const container = document.getElementById('progressBars');
    container.innerHTML = '';
    
    const targetBar = createProgressBar('🎯 Target', data.target || 0, data.target || 0);
    const actualBar = createProgressBar('✅ Actual', data.actual || 0, data.target || 0);
    
    container.appendChild(targetBar);
    container.appendChild(actualBar);
}

function createProgressBar(label, value, max) {
    const percentage = max > 0 ? Math.min((value / max) * 100, 100) : 0;
    
    const item = document.createElement('div');
    item.className = 'progress-item';
    item.innerHTML = `
        <div class="progress-label">
            <span>${label}</span>
            <span>${value} / ${max} min</span>
        </div>
        <div class="progress-bar">
            <div class="progress-fill" style="width: ${percentage}%">
                ${Math.round(percentage)}%
            </div>
        </div>
    `;
    return item;
}

// Utility Functions
function formatTime(minutes) {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return `${hours}h ${mins}m`;
}

function showNotification(title, message) {
    // Simple alert for now - can be enhanced with toast notifications
    console.log(`${title}: ${message}`);
    if ('Notification' in window && Notification.permission === 'granted') {
        new Notification(title, { body: message, icon: '🎯' });
    }
}

function setDailyTip() {
    const tips = [
        'Take a 5-minute break every 25 minutes to stay focused!',
        'Drink water during breaks to stay hydrated and alert.',
        'Use the Pomodoro technique for complex tasks.',
        'Review your completed tasks daily to track progress.',
        'Prioritize tasks by urgency and importance.',
        'Start with the most challenging task when you\'re fresh.',
        'Break large tasks into smaller, manageable chunks.',
        'Eliminate distractions during focus sessions.',
        'Celebrate small wins to stay motivated!',
        'Consistent practice builds lasting productivity habits.'
    ];
    
    const tipIndex = new Date().getDate() % tips.length;
    document.getElementById('dailyTip').textContent = tips[tipIndex];
}

// Activity Calendar Functions
async function loadActivityData() {
    try {
        const response = await fetch(`${API_BASE}/analytics/activity`, { credentials: 'include' });
        const data = await response.json();

        activityData = {};
        if (Array.isArray(data)) {
            data.forEach(entry => {
                if (entry.date && typeof entry.minutes === 'number') {
                    activityData[entry.date] = entry.minutes;
                }
            });
        } else {
            activityData = data || {};
        }
    } catch (error) {
        console.error('Error loading activity data:', error);
        activityData = {};
    }
    renderActivityCalendar();
}

function renderActivityCalendar() {
    const year = currentCalendarDate.getFullYear();
    const month = currentCalendarDate.getMonth();
    const monthNames = ['January', 'February', 'March', 'April', 'May', 'June',
                        'July', 'August', 'September', 'October', 'November', 'December'];
    document.getElementById('calendarMonth').textContent = `${monthNames[month]} ${year}`;

    const firstDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const daysInPrevMonth = new Date(year, month, 0).getDate();

    const container = document.getElementById('activityCalendar');
    container.innerHTML = '';

    for (let i = 0; i < firstDay; i++) {
        const dayEl = createActivityDayElement(daysInPrevMonth - firstDay + 1 + i, month - 1, year, true);
        container.appendChild(dayEl);
    }

    for (let i = 1; i <= daysInMonth; i++) {
        const dayEl = createActivityDayElement(i, month, year, false);
        container.appendChild(dayEl);
    }

    const totalCells = firstDay + daysInMonth;
    const remainingCells = (Math.ceil(totalCells / 7) * 7) - totalCells;
    for (let i = 1; i <= remainingCells; i++) {
        const dayEl = createActivityDayElement(i, month + 1, year, true);
        container.appendChild(dayEl);
    }
}

function createActivityDayElement(day, month, year, isOtherMonth) {
    const dayEl = document.createElement('div');
    dayEl.className = 'activity-day';
    const date = new Date(year, month, day);
    const dateStr = date.toISOString().split('T')[0];
    const minutes = activityData[dateStr] || 0;

    let level = 0;
    if (minutes >= 180) level = 4;
    else if (minutes >= 120) level = 3;
    else if (minutes >= 60) level = 2;
    else if (minutes > 0) level = 1;

    dayEl.setAttribute('data-level', level);
    dayEl.setAttribute('data-date', dateStr);
    dayEl.setAttribute('data-minutes', minutes);
    dayEl.textContent = day;

    if (isOtherMonth) {
        dayEl.style.opacity = '0.2';
    }

    dayEl.title = `${dateStr}: ${minutes} min focus`;
    dayEl.addEventListener('click', () => showActivityDetails(dateStr, minutes));
    return dayEl;
}

function showActivityDetails(dateStr, minutes) {
    const date = new Date(dateStr);
    const dateFormatted = date.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' });
    showNotification(`📅 ${dateFormatted}`, `Focused for ${minutes} minutes.`);
}

function previousMonth() {
    currentCalendarDate.setMonth(currentCalendarDate.getMonth() - 1);
    renderActivityCalendar();
}

function nextMonth() {
    currentCalendarDate.setMonth(currentCalendarDate.getMonth() + 1);
    renderActivityCalendar();
}

async function updateActivityData(date, minutes) {
    const dateStr = date instanceof Date ? date.toISOString().split('T')[0] : date;
    activityData[dateStr] = (activityData[dateStr] || 0) + minutes;
    renderActivityCalendar();
}
// Request notification permission
if ('Notification' in window && Notification.permission === 'default') {
    Notification.requestPermission();
}
