package com.app.DTO;

import java.time.OffsetDateTime;



public record SyncTriggerResponse(
boolean success,
int synced_items,
int failed_items,
java.util.List<SyncError> errors
) {
public record SyncError(String task_id, String operation, String error, OffsetDateTime timestamp) {}
}