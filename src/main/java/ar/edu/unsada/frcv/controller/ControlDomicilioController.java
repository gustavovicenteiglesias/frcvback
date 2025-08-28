// ControlDomicilioController.java
package ar.edu.unsada.frcv.controller;

import ar.edu.unsada.frcv.models.ControlDomicilio;
import ar.edu.unsada.frcv.service.ControlDomicilioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/controles/domicilio")
public class ControlDomicilioController {
    private final ControlDomicilioService service;
    public ControlDomicilioController(ControlDomicilioService service) { this.service = service; }

    @GetMapping public Iterable<ControlDomicilio> list() { return service.list(); }

    @GetMapping("/{id}")
    public ResponseEntity<ControlDomicilio> get(@PathVariable String id) {
        return service.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ControlDomicilio> create(@Valid @RequestBody ControlDomicilio c,
                                                   @RequestParam String visitaId) {
        ControlDomicilio saved = service.create(c, visitaId);
        return ResponseEntity.created(URI.create("/api/controles/domicilio/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ControlDomicilio> update(@PathVariable String id,
                                                   @Valid @RequestBody ControlDomicilio c,
                                                   @RequestParam(required = false) String visitaId) {
        return ResponseEntity.ok(service.update(id, c, visitaId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
