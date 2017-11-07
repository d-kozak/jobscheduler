package io.dkozak.jobscheduler.services;

import io.dkozak.jobscheduler.entity.Task;

import java.util.Optional;

public class EditedTaskService {
    private Task task;

    public void setEditedTask(Task task) {
        this.task = task;
    }

    public Optional<Task> getEditedTask() {
        return Optional.ofNullable(task);
    }

    public void unsetEditedTask() {
        task = null;
    }
}
