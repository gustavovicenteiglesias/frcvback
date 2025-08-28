// LabTipoServiceImpl.java
package ar.edu.unsada.frcv.service.impl;

import ar.edu.unsada.frcv.models.LabTipo;
import ar.edu.unsada.frcv.repository.LabTipoRepository;
import ar.edu.unsada.frcv.service.LabTipoService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service @Transactional
public class LabTipoServiceImpl implements LabTipoService {
    private final LabTipoRepository repo;
    public LabTipoServiceImpl(LabTipoRepository repo) { this.repo = repo; }

    @Override public Iterable<LabTipo> list() { return repo.findBySqlDeletedFalse(); }

    @Override public Optional<LabTipo> get(String id) { return repo.findById(id).filter(t -> !Boolean.TRUE.equals(t.getSqlDeleted())); }

    @Override public LabTipo create(LabTipo t) {
        if (repo.existsByCodigoAndSqlDeletedFalse(t.getCodigo()))
            throw new IllegalArgumentException("Ya existe un tipo de laboratorio con ese código");
        t.setSqlDeleted(false);
        t.setLastModified(Instant.now().toEpochMilli());
        if (t.getId() == null || t.getId().isBlank()) t.setId(java.util.UUID.randomUUID().toString());
        return repo.save(t);
    }

    @Override public LabTipo update(String id, LabTipo t) {
        LabTipo db = get(id).orElseThrow(() -> new NoSuchElementException("Tipo de laboratorio no encontrado"));

        if (t.getCodigo() != null && !t.getCodigo().equals(db.getCodigo())) {
            if (repo.existsByCodigoAndSqlDeletedFalse(t.getCodigo()))
                throw new IllegalArgumentException("Ya existe un tipo de laboratorio con ese código");
            db.setCodigo(t.getCodigo());
        }
        db.setNombre(t.getNombre());
        db.setUnidadDefault(t.getUnidadDefault());
        db.setRefMin(t.getRefMin());
        db.setRefMax(t.getRefMax());
        db.setLastModified(Instant.now().toEpochMilli());
        return repo.save(db);
    }

    @Override public void delete(String id) {
        LabTipo db = get(id).orElseThrow(() -> new NoSuchElementException("Tipo de laboratorio no encontrado"));
        db.setSqlDeleted(true);
        db.setLastModified(Instant.now().toEpochMilli());
        repo.save(db);
    }
}
