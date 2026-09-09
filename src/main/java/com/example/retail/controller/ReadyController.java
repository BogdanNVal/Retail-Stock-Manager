package com.example.retail.controller;

import com.example.retail.config.ReadyState;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * /internal/ready for docker/early_proxy.py. Cheap, unauthenticated, only
 * meant to be called on 127.0.0.1.
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
