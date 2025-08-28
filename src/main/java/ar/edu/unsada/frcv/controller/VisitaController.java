// VisitaController.java
package ar.edu.unsada.frcv.controller;

import ar.edu.unsada.frcv.models.Visita;
import ar.edu.unsada.frcv.service.VisitaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/visitas")
public class VisitaController {
    private final VisitaService service;
    public VisitaController(VisitaService service) { this.service = service; }

    @GetMapping public Iterable<Visita> list() { return service.list(); }

    @GetMapping("/{id}")
    public ResponseEntity<Visita> get(@PathVariable String id) {
        return service.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Para crear/actualizar sin DTO, paso personaId y responsableId por query o header.
    @PostMapping
    public ResponseEntity<Visita> create(@Valid @RequestBody Visita v,
                                         @RequestParam String personaId,
                                         @RequestParam String responsableId) {
        Visita saved = service.create(v, personaId, responsableId);
        return ResponseEntity.created(URI.create("/api/visitas/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Visita> update(@PathVariable String id,
                                         @Valid @RequestBody Visita v,
                                         @RequestParam(required = false) String personaId,
                                         @RequestParam(required = false) String responsableId) {
        return ResponseEntity.ok(service.update(id, v, personaId, responsableId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
