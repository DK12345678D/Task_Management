package com.app.DTO;

import java.time.OffsetDateTime;




public record SyncStatusResponse(
int pending_sync_count,
OffsetDateTime last_sync_timestamp,
boolean is_online,
int sync_queue_size
) {}