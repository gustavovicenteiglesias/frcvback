// AntecedenteController.java
package ar.edu.unsada.frcv.controller;

import ar.edu.unsada.frcv.models.Antecedente;
import ar.edu.unsada.frcv.service.AntecedenteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/antecedentes")
public class AntecedenteController {
    private final AntecedenteService service;
    public AntecedenteController(AntecedenteService service) { this.service = service; }

    @GetMapping public Iterable<Antecedente> list() { return service.list(); }

    @GetMapping("/persona/{personaId}")
    public Iterable<Antecedente> listByPersona(@PathVariable String personaId) {
        return service.listByPersona(personaId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Antecedente> get(@PathVariable String id) {
        return service.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Antecedente> create(@Valid @RequestBody Antecedente a,
                                              @RequestParam String personaId,
                                              @RequestParam(required = false) String visitaId) {
        Antecedente saved = service.create(a, personaId, visitaId);
        return ResponseEntity.created(URI.create("/api/antecedentes/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Antecedente> update(@PathVariable String id, @Valid @RequestBody Antecedente a,
                                              @RequestParam(required = false) String personaId,
                                              @RequestParam(required = false) String visitaId) {
        return ResponseEntity.ok(service.update(id, a, personaId, visitaId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
