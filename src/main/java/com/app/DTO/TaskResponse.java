package com.app.DTO;

import java.time.Instant;
import java.util.UUID;

public class TaskResponse {
    private UUID id;
    private String title;
    private String description;
    private boolean completed;
    private Instant created_at;
    private Instant updated_at;
    private boolean is_deleted;
    private String sync_status;
    private String server_id;
    private Instant last_synced_at;

    // getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public Instant getCreated_at() { return created_at; }
    public void setCreated_at(Instant created_at) { this.created_at = created_at; }

    public Instant getUpdated_at() { return updated_at; }
    public void setUpdated_at(Instant updated_at) { this.updated_at = updated_at; }

    public boolean isIs_deleted() { return is_deleted; }
    public void setIs_deleted(boolean is_deleted) { this.is_deleted = is_deleted; }

    public String getSync_status() { return sync_status; }
    public void setSync_status(String sync_status) { this.sync_status = sync_status; }

    public String getServer_id() { return server_id; }
    public void setServer_id(String server_id) { this.server_id = server_id; }

    public Instant getLast_synced_at() { return last_synced_at; }
    public void setLast_synced_at(Instant last_synced_at) { this.last_synced_at = last_synced_at; }
}
