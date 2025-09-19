package ar.edu.unsada.frcv.controller;

import ar.edu.unsada.frcv.models.Viviendas;
import ar.edu.unsada.frcv.service.ViviendasService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/vivivienda")
@RequiredArgsConstructor
public class ViviendasController {
    private final ViviendasService service;

    @GetMapping
    public ResponseEntity<Iterable<Viviendas>> findAll() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Viviendas> findById(@PathVariable String id) {
        return service.get(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @PostMapping("/{b}/{c}")
    public ResponseEntity<Viviendas> save(@Valid @RequestBody Viviendas v, @PathVariable String b, @PathVariable String c) {
        Viviendas saved=service.create(v,b,c);
        return ResponseEntity.created(URI.create("/api/vivivienda/*"+ saved.getId())).body(saved);
    }

    @PutMapping("/{id}/{b}/{c}")
    public ResponseEntity<Viviendas>  update(@PathVariable String id, @Valid @RequestBody Viviendas t, @PathVariable String b, @PathVariable String c) {
        return ResponseEntity.ok(service.update(id, t, b, c));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
