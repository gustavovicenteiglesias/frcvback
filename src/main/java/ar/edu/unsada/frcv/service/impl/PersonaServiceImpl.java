package ar.edu.unsada.frcv.service.impl;

// PersonaServiceImpl.java


import ar.edu.unsada.frcv.models.Persona;
import ar.edu.unsada.frcv.repository.PersonaRepository;
import ar.edu.unsada.frcv.service.PersonaService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
public class PersonaServiceImpl implements PersonaService {
    private final PersonaRepository repo;
    public PersonaServiceImpl(PersonaRepository repo) { this.repo = repo; }

    @Override public Iterable<Persona> list() { return repo.findBySqlDeletedFalse(); }

    @Override public Optional<Persona> get(String id) { return repo.findById(id).filter(p -> !Boolean.TRUE.equals(p.getSqlDeleted())); }

    @Override public Persona create(Persona p) {
        p.setSqlDeleted(false);
        p.setLastModified(Instant.now().toEpochMilli());
        if (p.getId() == null || p.getId().isBlank()) p.setId(java.util.UUID.randomUUID().toString());
        return repo.save(p);
    }

    @Override public Persona update(String id, Persona p) {
        Persona db = get(id).orElseThrow(() -> new NoSuchElementException("Persona no encontrada"));
        // copiamos campos editables
        db.setApellido(p.getApellido());
        db.setNombre(p.getNombre());
        db.setDni(p.getDni());
        db.setSexo(p.getSexo());
        db.setFechaNac(p.getFechaNac());
        db.setTelefono(p.getTelefono());
        db.setCoberturaSalud(p.getCoberturaSalud());
        //db.setDireccion(p.getDireccion());
        db.setLastModified(Instant.now().toEpochMilli());
        return repo.save(db);
    }

    @Override public void delete(String id) {
        Persona db = get(id).orElseThrow(() -> new NoSuchElementException("Persona no encontrada"));
        db.setSqlDeleted(true);
        db.setLastModified(Instant.now().toEpochMilli());
        repo.save(db);
    }
}
