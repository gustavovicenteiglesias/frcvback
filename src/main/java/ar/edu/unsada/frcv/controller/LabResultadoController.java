// LabResultadoController.java
package ar.edu.unsada.frcv.controller;

import ar.edu.unsada.frcv.models.LabResultado;
import ar.edu.unsada.frcv.service.LabResultadoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/lab/resultados")
public class LabResultadoController {
    private final LabResultadoService service;
    public LabResultadoController(LabResultadoService service) { this.service = service; }

    @GetMapping public Iterable<LabResultado> list() { return service.list(); }

    @GetMapping("/{id}")
    public ResponseEntity<LabResultado> get(@PathVariable String id) {
        return service.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<LabResultado> create(@Valid @RequestBody LabResultado r,
                                               @RequestParam String personaId,
                                               @RequestParam String tipoId,
                                               @RequestParam(required = false) String visitaId) {
        LabResultado saved = service.create(r, personaId, tipoId, visitaId);
        return ResponseEntity.created(URI.create("/api/lab/resultados/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LabResultado> update(@PathVariable String id,
                                               @Valid @RequestBody LabResultado r,
                                               @RequestParam(required = false) String personaId,
                                               @RequestParam(required = false) String tipoId,
                                               @RequestParam(required = false) String visitaId) {
        return ResponseEntity.ok(service.update(id, r, personaId, tipoId, visitaId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
