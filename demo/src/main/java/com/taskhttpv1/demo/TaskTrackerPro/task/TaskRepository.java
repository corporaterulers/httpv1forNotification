package com.taskhttpv1.demo.TaskTrackerPro.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByStatusAndDueDateBetween(String status, Instant from, Instant to);

    // for connecting to repository and for handling the expections
}
