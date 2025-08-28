// LabResultadoServiceImpl.java
package ar.edu.unsada.frcv.service.impl;

import ar.edu.unsada.frcv.models.*;
import ar.edu.unsada.frcv.repository.*;
import ar.edu.unsada.frcv.service.LabResultadoService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service @Transactional
public class LabResultadoServiceImpl implements LabResultadoService {
    private final LabResultadoRepository repo;
    private final PersonaRepository personaRepo;
    private final LabTipoRepository tipoRepo;
    private final VisitaRepository visitaRepo;

    public LabResultadoServiceImpl(LabResultadoRepository repo, PersonaRepository personaRepo,
                                   LabTipoRepository tipoRepo, VisitaRepository visitaRepo) {
        this.repo = repo; this.personaRepo = personaRepo; this.tipoRepo = tipoRepo; this.visitaRepo = visitaRepo;
    }

    @Override public Iterable<LabResultado> list() { return repo.findBySqlDeletedFalse(); }

    @Override public Optional<LabResultado> get(String id) {
        return repo.findById(id).filter(r -> !Boolean.TRUE.equals(r.getSqlDeleted()));
    }

    @Override public LabResultado create(LabResultado r, String personaId, String tipoId, String visitaId) {
        Persona persona = personaRepo.findById(personaId).filter(x -> !Boolean.TRUE.equals(x.getSqlDeleted()))
                .orElseThrow(() -> new NoSuchElementException("Persona no encontrada"));
        LabTipo tipo = tipoRepo.findById(tipoId).filter(x -> !Boolean.TRUE.equals(x.getSqlDeleted()))
                .orElseThrow(() -> new NoSuchElementException("Tipo de laboratorio no encontrado"));
        r.setPersona(persona);
        r.setTipo(tipo);

        if (visitaId != null) {
            Visita v = visitaRepo.findById(visitaId).filter(x -> !Boolean.TRUE.equals(x.getSqlDeleted()))
                    .orElseThrow(() -> new NoSuchElementException("Visita no encontrada"));
            r.setVisita(v);
        }

        r.setSqlDeleted(false);
        r.setLastModified(Instant.now().toEpochMilli());
        if (r.getId() == null || r.getId().isBlank()) r.setId(java.util.UUID.randomUUID().toString());
        return repo.save(r);
    }

    @Override public LabResultado update(String id, LabResultado r, String personaId, String tipoId, String visitaId) {
        LabResultado db = get(id).orElseThrow(() -> new NoSuchElementException("Resultado de laboratorio no encontrado"));

        if (personaId != null) {
            Persona persona = personaRepo.findById(personaId).filter(x -> !Boolean.TRUE.equals(x.getSqlDeleted()))
                    .orElseThrow(() -> new NoSuchElementException("Persona no encontrada"));
            db.setPersona(persona);
        }
        if (tipoId != null) {
            LabTipo tipo = tipoRepo.findById(tipoId).filter(x -> !Boolean.TRUE.equals(x.getSqlDeleted()))
                    .orElseThrow(() -> new NoSuchElementException("Tipo de laboratorio no encontrado"));
            db.setTipo(tipo);
        }
        if (visitaId != null) {
            Visita v = visitaRepo.findById(visitaId).filter(x -> !Boolean.TRUE.equals(x.getSqlDeleted()))
                    .orElseThrow(() -> new NoSuchElementException("Visita no encontrada"));
            db.setVisita(v);
        }

        db.setFechaRealizado(r.getFechaRealizado());
        db.setValorNum(r.getValorNum());
        db.setValorTexto(r.getValorTexto());
        db.setUnidad(r.getUnidad());
        db.setLaboratorio(r.getLaboratorio());
        db.setObservaciones(r.getObservaciones());
        db.setCreatedBy(r.getCreatedBy());

        db.setLastModified(Instant.now().toEpochMilli());
        return repo.save(db);
    }

    @Override public void delete(String id) {
        LabResultado db = get(id).orElseThrow(() -> new NoSuchElementException("Resultado de laboratorio no encontrado"));
        db.setSqlDeleted(true);
        db.setLastModified(Instant.now().toEpochMilli());
        repo.save(db);
    }
}
