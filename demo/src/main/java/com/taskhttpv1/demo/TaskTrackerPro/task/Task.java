package com.taskhttpv1.demo.TaskTrackerPro.task;

import com.taskhttpv1.demo.TaskTrackerPro.client.UserDb;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class  Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String title;
    private String description;
    private String email;
    private LocalDateTime dueDate;
    private String userToken;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserDb user;
}