// ControlConsultorioServiceImpl.java
package ar.edu.unsada.frcv.service.impl;

import ar.edu.unsada.frcv.models.ControlConsultorio;
import ar.edu.unsada.frcv.models.Visita;
import ar.edu.unsada.frcv.repository.ControlConsultorioRepository;
import ar.edu.unsada.frcv.repository.VisitaRepository;
import ar.edu.unsada.frcv.service.ControlConsultorioService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service @Transactional
public class ControlConsultorioServiceImpl implements ControlConsultorioService {
    private final ControlConsultorioRepository repo;
    private final VisitaRepository visitaRepo;

    public ControlConsultorioServiceImpl(ControlConsultorioRepository repo, VisitaRepository visitaRepo) {
        this.repo = repo; this.visitaRepo = visitaRepo;
    }

    @Override public Iterable<ControlConsultorio> list() { return repo.findBySqlDeletedFalse(); }

    @Override public Optional<ControlConsultorio> get(String id) {
        return repo.findById(id).filter(c -> !Boolean.TRUE.equals(c.getSqlDeleted()));
    }

    @Override public ControlConsultorio create(ControlConsultorio c, String visitaId) {
        Visita v = visitaRepo.findById(visitaId).filter(x -> !Boolean.TRUE.equals(x.getSqlDeleted()))
                .orElseThrow(() -> new NoSuchElementException("Visita no encontrada"));
        if (repo.existsByVisitaIdAndSqlDeletedFalse(visitaId))
            throw new IllegalStateException("La visita ya tiene un control de consultorio");

        c.setVisita(v);
        c.setSqlDeleted(false);
        c.setLastModified(Instant.now().toEpochMilli());
        if (c.getId() == null || c.getId().isBlank()) c.setId(java.util.UUID.randomUUID().toString());
        return repo.save(c);
    }

    @Override public ControlConsultorio update(String id, ControlConsultorio c, String visitaId) {
        ControlConsultorio db = get(id).orElseThrow(() -> new NoSuchElementException("Control de consultorio no encontrado"));

        if (visitaId != null) {
            if (repo.existsByVisitaIdAndSqlDeletedFalse(visitaId) && !db.getVisita().getId().equals(visitaId))
                throw new IllegalStateException("La visita ya tiene un control de consultorio");
            Visita v = visitaRepo.findById(visitaId).filter(x -> !Boolean.TRUE.equals(x.getSqlDeleted()))
                    .orElseThrow(() -> new NoSuchElementException("Visita no encontrada"));
            db.setVisita(v);
        }

        db.setTaSistolica(c.getTaSistolica());
        db.setTaDiastolica(c.getTaDiastolica());
        db.setMedicacionJson(c.getMedicacionJson());
        db.setConducta(c.getConducta());
        db.setCreatedBy(c.getCreatedBy());

        db.setLastModified(Instant.now().toEpochMilli());
        return repo.save(db);
    }

    @Override public void delete(String id) {
        ControlConsultorio db = get(id).orElseThrow(() -> new NoSuchElementException("Control de consultorio no encontrado"));
        db.setSqlDeleted(true);
        db.setLastModified(Instant.now().toEpochMilli());
        repo.save(db);
    }
}
