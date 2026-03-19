// ControlConsultorioServiceImpl.java
package ar.edu.unsada.frcv.service.impl;

import ar.edu.unsada.frcv.models.ControlConsultorio;
import ar.edu.unsada.frcv.models.ControlConsultorioDerivacion;
import ar.edu.unsada.frcv.models.ControlConsultorioEvento;
import ar.edu.unsada.frcv.models.Visita;
import ar.edu.unsada.frcv.repository.ControlConsultorioRepository;
import ar.edu.unsada.frcv.repository.VisitaRepository;
import ar.edu.unsada.frcv.service.ControlConsultorioService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ControlConsultorioServiceImpl implements ControlConsultorioService {
    private final ControlConsultorioRepository repo;
    private final VisitaRepository visitaRepo;

    public ControlConsultorioServiceImpl(ControlConsultorioRepository repo, VisitaRepository visitaRepo) {
        this.repo = repo;
        this.visitaRepo = visitaRepo;
    }

    @Override
    public Iterable<ControlConsultorio> list() {
        return repo.findBySqlDeletedFalse();
    }

    @Override
    public Optional<ControlConsultorio> get(String id) {
        return repo.findById(id).filter(c -> !Boolean.TRUE.equals(c.getSqlDeleted()));
    }

    @Override
    public ControlConsultorio create(ControlConsultorio c, String visitaId) {
        Visita v = visitaRepo.findById(visitaId).filter(x -> !Boolean.TRUE.equals(x.getSqlDeleted()))
                .orElseThrow(() -> new NoSuchElementException("Visita no encontrada"));
        if (repo.existsByVisitaIdAndSqlDeletedFalse(visitaId))
            throw new IllegalStateException("La visita ya tiene un control de consultorio");

        c.setVisita(v);
        c.setSqlDeleted(false);
        c.setLastModified(Instant.now().toEpochMilli());
        if (c.getId() == null || c.getId().isBlank()) c.setId(UUID.randomUUID().toString());
        hydrateChildren(c, c.getLastModified());
        return repo.save(c);
    }

    @Override
    public ControlConsultorio update(String id, ControlConsultorio c, String visitaId) {
        ControlConsultorio db = get(id).orElseThrow(() -> new NoSuchElementException("Control de consultorio no encontrado"));

        if (visitaId != null) {
            if (repo.existsByVisitaIdAndSqlDeletedFalse(visitaId) && !db.getVisita().getId().equals(visitaId))
                throw new IllegalStateException("La visita ya tiene un control de consultorio");
            Visita v = visitaRepo.findById(visitaId).filter(x -> !Boolean.TRUE.equals(x.getSqlDeleted()))
                    .orElseThrow(() -> new NoSuchElementException("Visita no encontrada"));
            db.setVisita(v);
        }
        db.setFecha(c.getFecha());
        db.setAsistencia(c.getAsistencia());
        db.setTaSistolica(c.getTaSistolica());
        db.setTaDiastolica(c.getTaDiastolica());
        db.setConfirm_hta(c.getConfirm_hta());
        db.setPeso(c.getPeso());
        db.setTalla(c.getTalla());
        db.setImc(c.getImc());
        db.setCircCintura(c.getCircCintura());
        db.setEntregaMedicacion(c.getEntregaMedicacion());
        db.setControlMedicacion(c.getControlMedicacion());
        db.setConducta(c.getConducta());
        db.setEventos(c.getEventos());
        db.setObservaciones_eventos(c.getObservaciones_eventos());
        db.setDerivacion(c.getDerivacion());
        db.setObservaciones_derivacion(c.getObservaciones_derivacion());
        db.setFumador(c.getFumador());
        db.setObservaciones(c.getObservaciones());
        db.setMedicacion(c.getMedicacion());
        //db.setMedicacionJson(c.getMedicacionJson());
        db.setConducta(c.getConducta());
        db.setCreatedBy(c.getCreatedBy());

        long now = Instant.now().toEpochMilli();
        db.setLastModified(now);
        syncChildren(db, c, now);
        return repo.save(db);
    }

    @Override
    public void delete(String id) {
        ControlConsultorio db = get(id).orElseThrow(() -> new NoSuchElementException("Control de consultorio no encontrado"));
        db.setSqlDeleted(true);
        db.setLastModified(Instant.now().toEpochMilli());
        repo.save(db);
    }

    private void hydrateChildren(ControlConsultorio c, long timestamp) {
        if (c.getMotivoNoMedicacion() != null) {
            if (c.getMotivoNoMedicacion().getId() == null || c.getMotivoNoMedicacion().getId().isBlank()) {
                c.getMotivoNoMedicacion().setId(UUID.randomUUID().toString());
            }
            c.getMotivoNoMedicacion().setLastModified(timestamp);
            c.getMotivoNoMedicacion().setSqlDeleted(false);
        }

        if (c.getEventosConsultorio() != null) {
            c.getEventosConsultorio().forEach(e -> {
                if (e.getId() == null || e.getId().isBlank()) e.setId(UUID.randomUUID().toString());
                e.setControlConsultorio(c);
                e.setLastModified(timestamp);
                e.setSqlDeleted(false);
            });
        }

        if (c.getDerivacionesConsultorio() != null) {
            c.getDerivacionesConsultorio().forEach(d -> {
                if (d.getId() == null || d.getId().isBlank()) d.setId(UUID.randomUUID().toString());
                d.setControlConsultorio(c);
                d.setLastModified(timestamp);
                d.setSqlDeleted(false);
            });
        }
    }

    private void syncChildren(ControlConsultorio target, ControlConsultorio incoming, long timestamp) {
        // Motivo (uno a uno)
        target.setMotivoNoMedicacion(incoming.getMotivoNoMedicacion());
        hydrateChildren(target, timestamp);

        // Eventos (reemplazo completo para simplicidad)
        target.getEventosConsultorio().clear();
        if (incoming.getEventosConsultorio() != null) {
            List<ControlConsultorioEvento> events = new ArrayList<>(incoming.getEventosConsultorio());
            target.getEventosConsultorio().addAll(events);
        }

        // Derivaciones (reemplazo completo)
        target.getDerivacionesConsultorio().clear();
        if (incoming.getDerivacionesConsultorio() != null) {
            List<ControlConsultorioDerivacion> derivs = new ArrayList<>(incoming.getDerivacionesConsultorio());
            target.getDerivacionesConsultorio().addAll(derivs);
        }

        hydrateChildren(target, timestamp);
    }
}
