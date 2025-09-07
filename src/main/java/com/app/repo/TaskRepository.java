package com.app.repo;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.entity.Task;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByIsDeletedFalse();
    List<Task> findBySyncStatusNot(String syncStatus);
    long countBySyncStatusNot(String syncStatus);
}
