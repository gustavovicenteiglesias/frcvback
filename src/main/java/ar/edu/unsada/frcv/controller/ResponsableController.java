// ResponsableController.java
package ar.edu.unsada.frcv.controller;

import ar.edu.unsada.frcv.models.Responsable;
import ar.edu.unsada.frcv.service.ResponsableService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/responsables")
public class ResponsableController {
    private final ResponsableService service;
    public ResponsableController(ResponsableService service) { this.service = service; }

    @GetMapping public Iterable<Responsable> list() { return service.list(); }

    @GetMapping("/{id}")
    public ResponseEntity<Responsable> get(@PathVariable String id) {
        return service.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Responsable> create(@Valid @RequestBody Responsable r) {
        Responsable saved = service.create(r);
        return ResponseEntity.created(URI.create("/api/responsables/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Responsable> update(@PathVariable String id, @Valid @RequestBody Responsable r) {
        return ResponseEntity.ok(service.update(id, r));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
