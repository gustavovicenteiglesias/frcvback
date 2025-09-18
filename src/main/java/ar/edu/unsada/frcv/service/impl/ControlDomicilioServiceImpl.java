// ControlDomicilioServiceImpl.java
package ar.edu.unsada.frcv.service.impl;

import ar.edu.unsada.frcv.models.ControlDomicilio;
import ar.edu.unsada.frcv.models.Visita;
import ar.edu.unsada.frcv.repository.ControlDomicilioRepository;
import ar.edu.unsada.frcv.repository.VisitaRepository;
import ar.edu.unsada.frcv.service.ControlDomicilioService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service @Transactional
public class ControlDomicilioServiceImpl implements ControlDomicilioService {
    private final ControlDomicilioRepository repo;
    private final VisitaRepository visitaRepo;

    public ControlDomicilioServiceImpl(ControlDomicilioRepository repo, VisitaRepository visitaRepo) {
        this.repo = repo; this.visitaRepo = visitaRepo;
    }

    @Override public Iterable<ControlDomicilio> list() { return repo.findBySqlDeletedFalse(); }

    @Override public Optional<ControlDomicilio> get(String id) {
        return repo.findById(id).filter(c -> !Boolean.TRUE.equals(c.getSqlDeleted()));
    }

    @Override public ControlDomicilio create(ControlDomicilio c, String visitaId) {
        Visita v = visitaRepo.findById(visitaId).filter(x -> !Boolean.TRUE.equals(x.getSqlDeleted()))
                .orElseThrow(() -> new NoSuchElementException("Visita no encontrada"));
        // Garantizar 1–1
        if (repo.existsByVisitaIdAndSqlDeletedFalse(visitaId))
            throw new IllegalStateException("La visita ya tiene un control de domicilio");

        c.setVisita(v);
        c.setSqlDeleted(false);
        c.setLastModified(Instant.now().toEpochMilli());
        if (c.getId() == null || c.getId().isBlank()) c.setId(java.util.UUID.randomUUID().toString());
        return repo.save(c);
    }

    @Override public ControlDomicilio update(String id, ControlDomicilio c, String visitaId) {
        ControlDomicilio db = get(id).orElseThrow(() -> new NoSuchElementException("Control de domicilio no encontrado"));

        if (visitaId != null) {
            if (repo.existsByVisitaIdAndSqlDeletedFalse(visitaId) && !db.getVisita().getId().equals(visitaId))
                throw new IllegalStateException("La visita ya tiene un control de domicilio");
            Visita v = visitaRepo.findById(visitaId).filter(x -> !Boolean.TRUE.equals(x.getSqlDeleted()))
                    .orElseThrow(() -> new NoSuchElementException("Visita no encontrada"));
            db.setVisita(v);
        }

        db.setTaSistolica(c.getTaSistolica());
        db.setTaDiastolica(c.getTaDiastolica());
        //db.setFrecuenciaCardiaca(c.getFrecuenciaCardiaca());
        //db.setPeso(c.getPeso());
        //db.setTalla(c.getTalla());
        //db.setImc(c.getImc());
        //db.setRiesgoCv(c.getRiesgoCv());
        //db.setDerivarAConsultorio(c.getDerivarAConsultorio());
        db.setAceptaLaboratotio(c.getAceptaLaboratotio());
        db.setAccedePrograma(c.getAccedePrograma());
        db.setObservaciones(c.getObservaciones());
        db.setCreatedBy(c.getCreatedBy());

        db.setLastModified(Instant.now().toEpochMilli());
        return repo.save(db);
    }

    @Override public void delete(String id) {
        ControlDomicilio db = get(id).orElseThrow(() -> new NoSuchElementException("Control de domicilio no encontrado"));
        db.setSqlDeleted(true);
        db.setLastModified(Instant.now().toEpochMilli());
        repo.save(db);
    }
}
