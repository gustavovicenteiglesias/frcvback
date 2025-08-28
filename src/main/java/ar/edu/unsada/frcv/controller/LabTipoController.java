// LabTipoController.java
package ar.edu.unsada.frcv.controller;

import ar.edu.unsada.frcv.models.LabTipo;
import ar.edu.unsada.frcv.service.LabTipoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/lab/tipos")
public class LabTipoController {
    private final LabTipoService service;
    public LabTipoController(LabTipoService service) { this.service = service; }

    @GetMapping public Iterable<LabTipo> list() { return service.list(); }

    @GetMapping("/{id}")
    public ResponseEntity<LabTipo> get(@PathVariable String id) {
        return service.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<LabTipo> create(@Valid @RequestBody LabTipo t) {
        LabTipo saved = service.create(t);
        return ResponseEntity.created(URI.create("/api/lab/tipos/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LabTipo> update(@PathVariable String id, @Valid @RequestBody LabTipo t) {
        return ResponseEntity.ok(service.update(id, t));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

