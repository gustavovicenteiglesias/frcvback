package ar.edu.unsada.frcv.service.impl;

import ar.edu.unsada.frcv.models.Barrios;
import ar.edu.unsada.frcv.models.LabTipo;
import ar.edu.unsada.frcv.repository.BarriosRepository;
import ar.edu.unsada.frcv.service.BarriosService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
public class BarriosServiceImpl implements BarriosService {
    private final BarriosRepository repo;
    public BarriosServiceImpl( BarriosRepository repo) {
        this.repo = repo;
    }

    @Override
    public Iterable<Barrios> list() {
        return repo.findBySqlDeletedFalse();
    }

    @Override
    public Optional<Barrios> get(String id) {
        return repo.findById(id).filter(a -> !Boolean.TRUE.equals(a.getSqlDeleted()));

    }

    @Override
    public Barrios create(Barrios t) {
        t.setSqlDeleted(false);
        t.setLastModified(Instant.now().toEpochMilli());
        if (t.getIdBarrios() == null || t.getIdBarrios().isBlank()) t.setIdBarrios(java.util.UUID.randomUUID().toString());
        return repo.save(t);
    }

    @Override
    public Barrios update(String id, Barrios t) {
        Barrios db= repo.findById(id).filter(a -> !Boolean.TRUE.equals(a.getSqlDeleted()))
                .orElseThrow(() -> new NoSuchElementException("Barrio no encontrado"));
        db.setLastModified(Instant.now().toEpochMilli());
        db.setNombre(t.getNombre());
        return repo.save(db);
    }

    @Override
    public void delete(String id) {
        Barrios db = get(id).orElseThrow(() -> new NoSuchElementException("Barrio no encontrado"));
        db.setSqlDeleted(true);
        db.setLastModified(Instant.now().toEpochMilli());
        repo.save(db);

    }
}
