package com.focusmate.model;

import java.time.LocalDateTime;

public class Session {
    public Integer id;
    public Integer taskId;
    public LocalDateTime start;
    public LocalDateTime end;
    public int plannedMinutes;
    public int actualMinutes;
    public boolean stoppedManually;
}