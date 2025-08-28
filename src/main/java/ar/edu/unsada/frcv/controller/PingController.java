package ar.edu.unsada.frcv.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class PingController {
    @GetMapping("/ping")
    public Map<String,Object> ping() {
        return Map.of("ok", true, "ts", Instant.now().toString());
    }
}

