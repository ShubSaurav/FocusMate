package com.focusmate.store;

import com.focusmate.model.Task;
import com.focusmate.model.Session;

import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoryStore {
    public static final AtomicInteger TASK_SEQ = new AtomicInteger(1);
    public static final Map<Integer, Task> TASKS = new ConcurrentHashMap<>();
    public static final List<Session> SESSIONS = new CopyOnWriteArrayList<>();
}
