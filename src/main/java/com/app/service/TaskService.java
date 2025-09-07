package com.app.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.DTO.BatchRequest;
import com.app.DTO.BatchResponse;
import com.app.DTO.TaskRequest;
import com.app.DTO.TaskResponse;
import com.app.entity.Task;
import com.app.exception.ResourceNotFoundException;
import com.app.repo.TaskRepository;

@Service
public class TaskService {

    private final TaskRepository repo;

    public TaskService(TaskRepository repo) {
        this.repo = repo;
    }

    public List<TaskResponse> getAll() {
        return repo.findByIsDeletedFalse().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TaskResponse getById(UUID id) {
        Task t = repo.findById(id)
                .filter(task -> !task.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        return toResponse(t);
    }

    public TaskResponse create(TaskRequest req) {
        Task t = new Task();
        t.setTitle(req.getTitle());
        t.setDescription(req.getDescription());
        if (req.getCompleted() != null) t.setCompleted(req.getCompleted());
        t.setSyncStatus("pending");
        Task saved = repo.save(t);
        return toResponse(saved);
    }

    @Transactional
    public TaskResponse update(UUID id, TaskRequest req) {
        Task t = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        if (t.isDeleted()) throw new ResourceNotFoundException("Task not found");

        if (req.getTitle() != null) t.setTitle(req.getTitle());
        if (req.getDescription() != null) t.setDescription(req.getDescription());
        if (req.getCompleted() != null) t.setCompleted(req.getCompleted());
        t.setSyncStatus("pending");
        Task saved = repo.save(t);
        return toResponse(saved);
    }

    @Transactional
    public void softDelete(UUID id) {
        Task t = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        if (t.isDeleted()) return;
        t.setDeleted(true);
        t.setSyncStatus("pending");
        repo.save(t);
    }

    public long pendingSyncCount() {
        return repo.countBySyncStatusNot("synced");
    }

    public Map<String,Object> triggerSync() {
        List<Task> pending = repo.findBySyncStatusNot("synced");
        int success = 0;
        int failed = 0;
        List<Map<String,Object>> errors = new ArrayList<>();

        for (Task t : pending) {
            try {
                // Here we simulate server sync: set server_id if missing and mark as synced
                if (t.getServerId() == null) {
                    t.setServerId("srv_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
                }
                t.setSyncStatus("synced");
                t.setLastSyncedAt(Instant.now());
                repo.save(t);
                success++;
            } catch (Exception ex) {
                failed++;
                Map<String,Object> error = new HashMap<>();
                error.put("task_id", t.getId().toString());
                error.put("operation", "update");
                error.put("error", ex.getMessage());
                error.put("timestamp", Instant.now().toString());
                errors.add(error);
            }
        }

        Map<String,Object> out = new HashMap<>();
        out.put("success", failed == 0);
        out.put("synced_items", success);
        out.put("failed_items", failed);
        out.put("errors", errors);
        return out;
    }

    public Map<String, Object> status() {
        Map<String,Object> s = new HashMap<>();
        long pending = repo.countBySyncStatusNot("synced");
        Optional<Instant> lastSync = repo.findAll().stream()
                .map(Task::getLastSyncedAt)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder());
        s.put("pending_sync_count", pending);
        s.put("last_sync_timestamp", lastSync.map(Instant::toString).orElse(null));
        s.put("is_online", true);
        s.put("sync_queue_size", pending);
        return s;
    }

    public BatchResponse processBatch(BatchRequest batch) {
        List<BatchResponse.Processed> processed = new ArrayList<>();
        if (batch.getItems() == null) {
            BatchResponse resp = new BatchResponse();
            resp.setProcessed_items(processed);
            return resp;
        }

        for (BatchRequest.Item item : batch.getItems()) {
            BatchResponse.Processed p = new BatchResponse.Processed();
            p.client_id = item.id;
            try {
                if ("create".equalsIgnoreCase(item.operation)) {
                    Task t = new Task();
                    if (item.data != null) {
                        Object title = item.data.get("title");
                        if (title != null) t.setTitle(title.toString());
                        Object desc = item.data.get("description");
                        if (desc != null) t.setDescription(desc.toString());
                    }
                    t.setSyncStatus("synced");
                    t.setServerId("srv_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
                    t.setLastSyncedAt(Instant.now());
                    Task saved = repo.save(t);
                    p.server_id = saved.getServerId();
                    p.status = "success";
                    Map<String,Object> resolved = new HashMap<>();
                    resolved.put("id", saved.getServerId());
                    resolved.put("title", saved.getTitle());
                    resolved.put("description", saved.getDescription());
                    resolved.put("completed", saved.isCompleted());
                    resolved.put("created_at", saved.getCreatedAt().toString());
                    resolved.put("updated_at", saved.getUpdatedAt().toString());
                    p.resolved_data = resolved;
                } else if ("update".equalsIgnoreCase(item.operation)) {
                    // best-effort update using task_id -> server id or local id
                    // try find by server_id first
                    Task target = null;
                    if (item.task_id != null) {
                        // try UUID local id
                        try {
                            UUID localId = UUID.fromString(item.task_id);
                            target = repo.findById(localId).orElse(null);
                        } catch (IllegalArgumentException ex) {
                            // not a local uuid
                        }
                    }
                    if (target == null) {
                        // cannot resolve: skip
                        p.status = "failed";
                        p.server_id = null;
                        p.resolved_data = Map.of("error", "could not resolve task for update");
                    } else {
                        if (item.data != null) {
                            Object title = item.data.get("title");
                            if (title != null) target.setTitle(title.toString());
                            Object desc = item.data.get("description");
                            if (desc != null) target.setDescription(desc.toString());
                            Object completed = item.data.get("completed");
                            if (completed != null) target.setCompleted(Boolean.parseBoolean(completed.toString()));
                        }
                        target.setSyncStatus("synced");
                        target.setLastSyncedAt(Instant.now());
                        Task saved = repo.save(target);
                        p.status = "success";
                        p.server_id = saved.getServerId();
                        Map<String,Object> resolved = new HashMap<>();
                        resolved.put("id", saved.getServerId());
                        resolved.put("title", saved.getTitle());
                        resolved.put("description", saved.getDescription());
                        p.resolved_data = resolved;
                    }
                } else {
                    p.status = "failed";
                    p.resolved_data = Map.of("error", "unsupported operation");
                }
            } catch (Exception e) {
                p.status = "failed";
                p.resolved_data = Map.of("error", e.getMessage());
            }
            processed.add(p);
        }

        BatchResponse resp = new BatchResponse();
        resp.setProcessed_items(processed);
        return resp;
    }

    private TaskResponse toResponse(Task t) {
        TaskResponse r = new TaskResponse();
        r.setId(t.getId());
        r.setTitle(t.getTitle());
        r.setDescription(t.getDescription());
        r.setCompleted(t.isCompleted());
        r.setCreated_at(t.getCreatedAt());
        r.setUpdated_at(t.getUpdatedAt());
        r.setIs_deleted(t.isDeleted());
        r.setSync_status(t.getSyncStatus());
        r.setServer_id(t.getServerId());
        r.setLast_synced_at(t.getLastSyncedAt());
        return r;
    }

	public static List<TaskResponse> createTasks(List<TaskRequest> taskRequests) {
		// TODO Auto-generated method stub
		return null;
	}
}
