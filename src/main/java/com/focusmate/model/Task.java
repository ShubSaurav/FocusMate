package com.focusmate.model;

import java.time.LocalDate;

public class Task {
    public Integer id;
    public String title;
    public int priority;
    public LocalDate dueDate;
    public int targetMinutes;
    public String status;

    @Override
    public String toString() {
        return title + " (Priority " + priority + ")";
    }
}