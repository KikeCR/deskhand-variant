package com.deskhand.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * The Java equivalent of the original's {@code GET /api/health}. The ported frontend's
 * {@code useBackendStatus} hook polls this before claiming a run will take its usual 15-40
 * seconds, distinguishing "backend is genuinely down" from "backend hasn't started yet."
 */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
