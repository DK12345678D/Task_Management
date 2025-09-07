package com.app.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.DTO.BatchRequest;
import com.app.DTO.BatchResponse;
import com.app.service.TaskService;

@RestController
@RequestMapping("/api")
public class SyncController {

    private final TaskService svc;

    public SyncController(TaskService svc) {
        this.svc = svc;
    }

    @PostMapping("/sync")
    public ResponseEntity<Map<String,Object>> triggerSync() {
        return ResponseEntity.ok(svc.triggerSync());
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String,Object>> status() {
        return ResponseEntity.ok(svc.status());
    }

    @PostMapping("/batch")
    public ResponseEntity<BatchResponse> batch(@RequestBody BatchRequest batch) {
        return ResponseEntity.ok(svc.processBatch(batch));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String,Object>> health() {
        return ResponseEntity.ok(Map.of("status", "ok", "timestamp", java.time.Instant.now().toString()));
    }
}

