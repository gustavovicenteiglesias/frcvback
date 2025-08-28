package ar.edu.unsada.frcv.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sync")
public class SyncController {

    @GetMapping
    public ResponseEntity<?> pull(@RequestParam long since) {
        // TODO: devolver cambios desde last_modified > since
        return ResponseEntity.ok(Map.of(
                "since", since,
                "now", Instant.now().getEpochSecond(),
                "personas", List.of(),
                "visitas", List.of()
        ));
    }

    @PostMapping
    public ResponseEntity<?> push(@RequestBody List<Map<String,Object>> changes) {
        // TODO: aplicar cambios idempotentes
        return ResponseEntity.ok(Map.of("applied", changes.size(), "conflicts", List.of()));
    }
}
