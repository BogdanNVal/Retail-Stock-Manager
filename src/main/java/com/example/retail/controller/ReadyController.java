package com.example.retail.controller;

import com.example.retail.config.ReadyState;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Loopback readiness probe for {@code docker/early_proxy.py}. Not for public use
 * (the proxy only calls 127.0.0.1), but left unauthenticated and cheap.
 */
@RestController
public class ReadyController {

    private final ReadyState readyState;

    public ReadyController(ReadyState readyState) {
        this.readyState = readyState;
    }

    @GetMapping("/internal/ready")
    public ResponseEntity<String> ready() {
        if (!readyState.isReady()) {
            return ResponseEntity.status(503).body("starting");
        }
        return ResponseEntity.ok("ok");
    }
}
