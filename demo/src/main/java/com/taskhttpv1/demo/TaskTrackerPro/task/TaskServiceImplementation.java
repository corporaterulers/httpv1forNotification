package com.taskhttpv1.demo.TaskTrackerPro.task;

import org.springframework.stereotype.Service;

@Service
public class TaskServiceImplementation implements TaskService {
    private final TaskRepository taskRepository;

    public TaskServiceImplementation(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public Task save(Task task) {
        // Extract email and token from the user before saving
        if (task.getUser() != null) {
            task.setEmail(task.getUser().getEmail());
            task.setUserToken(task.getUser().getFcmToken());
        }

        return taskRepository.save(task);
    }
}
