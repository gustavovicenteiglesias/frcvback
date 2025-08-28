// ControlConsultorioController.java
package ar.edu.unsada.frcv.controller;

import ar.edu.unsada.frcv.models.ControlConsultorio;
import ar.edu.unsada.frcv.service.ControlConsultorioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/controles/consultorio")
public class ControlConsultorioController {
    private final ControlConsultorioService service;
    public ControlConsultorioController(ControlConsultorioService service) { this.service = service; }

    @GetMapping public Iterable<ControlConsultorio> list() { return service.list(); }

    @GetMapping("/{id}")
    public ResponseEntity<ControlConsultorio> get(@PathVariable String id) {
        return service.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ControlConsultorio> create(@Valid @RequestBody ControlConsultorio c,
                                                     @RequestParam String visitaId) {
        ControlConsultorio saved = service.create(c, visitaId);
        return ResponseEntity.created(URI.create("/api/controles/consultorio/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ControlConsultorio> update(@PathVariable String id,
                                                     @Valid @RequestBody ControlConsultorio c,
                                                     @RequestParam(required = false) String visitaId) {
        return ResponseEntity.ok(service.update(id, c, visitaId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
