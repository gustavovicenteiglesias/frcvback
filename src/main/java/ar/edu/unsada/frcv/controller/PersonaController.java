// PersonaController.java
package ar.edu.unsada.frcv.controller;

import ar.edu.unsada.frcv.models.Persona;
import ar.edu.unsada.frcv.service.PersonaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/personas")
public class PersonaController {
    private final PersonaService service;
    public PersonaController(PersonaService service) { this.service = service; }

    @GetMapping public Iterable<Persona> list() { return service.list(); }

    @GetMapping("/{id}")
    public ResponseEntity<Persona> get(@PathVariable String id) {
        return service.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Persona> create(@Valid @RequestBody Persona p) {
        Persona saved = service.create(p);
        return ResponseEntity.created(URI.create("/api/personas/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Persona> update(@PathVariable String id, @Valid @RequestBody Persona p) {
        return ResponseEntity.ok(service.update(id, p));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
