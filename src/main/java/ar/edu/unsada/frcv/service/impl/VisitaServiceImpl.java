// VisitaServiceImpl.java
package ar.edu.unsada.frcv.service.impl;

import ar.edu.unsada.frcv.models.*;
import ar.edu.unsada.frcv.repository.PersonaRepository;
import ar.edu.unsada.frcv.repository.ResponsableRepository;
import ar.edu.unsada.frcv.repository.VisitaRepository;
import ar.edu.unsada.frcv.service.VisitaService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
public class VisitaServiceImpl implements VisitaService {
    private final VisitaRepository repo;
    private final PersonaRepository personaRepo;
    private final ResponsableRepository responsableRepo;

    public VisitaServiceImpl(VisitaRepository repo, PersonaRepository personaRepo, ResponsableRepository responsableRepo) {
        this.repo = repo; this.personaRepo = personaRepo; this.responsableRepo = responsableRepo;
    }

    @Override public Iterable<Visita> list() { return repo.findBySqlDeletedFalse(); }

    @Override public Optional<Visita> get(String id) { return repo.findById(id).filter(v -> !Boolean.TRUE.equals(v.getSqlDeleted())); }

    @Override public Visita create(Visita v, String personaId, String responsableId) {
        Persona persona = personaRepo.findById(personaId).filter(p -> !Boolean.TRUE.equals(p.getSqlDeleted()))
                .orElseThrow(() -> new NoSuchElementException("Persona no encontrada"));
        Responsable resp = responsableRepo.findById(responsableId).filter(r -> !Boolean.TRUE.equals(r.getSqlDeleted()))
                .orElseThrow(() -> new NoSuchElementException("Responsable no encontrado"));

        v.setPersona(persona);
        v.setResponsable(resp);
        v.setSqlDeleted(false);
        v.setLastModified(Instant.now().toEpochMilli());
        if (v.getId() == null || v.getId().isBlank()) v.setId(java.util.UUID.randomUUID().toString());
        return repo.save(v);
    }

    @Override public Visita update(String id, Visita v, String personaId, String responsableId) {
        Visita db = get(id).orElseThrow(() -> new NoSuchElementException("Visita no encontrada"));

        if (personaId != null) {
            Persona persona = personaRepo.findById(personaId).filter(p -> !Boolean.TRUE.equals(p.getSqlDeleted()))
                    .orElseThrow(() -> new NoSuchElementException("Persona no encontrada"));
            db.setPersona(persona);
        }
        if (responsableId != null) {
            Responsable resp = responsableRepo.findById(responsableId).filter(r -> !Boolean.TRUE.equals(r.getSqlDeleted()))
                    .orElseThrow(() -> new NoSuchElementException("Responsable no encontrado"));
            db.setResponsable(resp);
        }

        db.setTipo(v.getTipo());
        db.setFecha(v.getFecha());
        db.setUbicacionGps(v.getUbicacionGps());
        db.setObservaciones(v.getObservaciones());
        db.setCreatedBy(v.getCreatedBy()); // opcional
        db.setLastModified(Instant.now().toEpochMilli());
        return repo.save(db);
    }

    @Override public void delete(String id) {
        Visita db = get(id).orElseThrow(() -> new NoSuchElementException("Visita no encontrada"));
        db.setSqlDeleted(true);
        db.setLastModified(Instant.now().toEpochMilli());
        repo.save(db);
    }
}
