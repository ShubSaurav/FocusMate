package com.focusmate.service;

import com.focusmate.dao.TaskDAO;
import com.focusmate.model.Task;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Scheduler {
    private final TaskDAO dao;
    private final double w1 = 2.0; // priority weight
    private final double w2 = 1.0; // deadline urgency weight
    private final double w3 = 1.5; // target gap weight

    public Scheduler(TaskDAO dao) {
        this.dao = dao;
    }

    public List<Task> sorted() throws Exception {
        List<Task> tasks = dao.listAll();
        LocalDate today = LocalDate.now();
        List<Scored> scoredList = new ArrayList<>();

        for (Task t : tasks) {
            int actual = dao.getActualMinutes(t.id);
            int gap = Math.max(0, t.targetMinutes - actual);
            double urgency = 0.0;
            if (t.dueDate != null) {
                long days = Math.max(1, ChronoUnit.DAYS.between(today, t.dueDate));
                urgency = 1.0 / days;
            }
            double score = w1 * t.priority + w2 * urgency + w3 * (gap / 60.0);
            scoredList.add(new Scored(t, score));
        }

        scoredList.sort((a, b) -> Double.compare(b.score, a.score));

        List<Task> sortedTasks = new ArrayList<>();
        for (Scored s : scoredList) sortedTasks.add(s.task);
        return sortedTasks;
    }

    private static class Scored {
        Task task;
        double score;

        Scored(Task t, double s) {
            task = t;
            score = s;
        }
    }
}