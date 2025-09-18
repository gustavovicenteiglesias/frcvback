package ar.edu.unsada.frcv.service.impl;

import ar.edu.unsada.frcv.models.Barrios;
import ar.edu.unsada.frcv.models.Caps;
import ar.edu.unsada.frcv.repository.CapsRepository;
import ar.edu.unsada.frcv.service.CapsService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;
@Service
@Transactional
public class CapsServiceImpl implements CapsService {
    private CapsRepository repo;
    public CapsServiceImpl(CapsRepository repo) {
        this.repo = repo;
    }
    @Override
    public Iterable<Caps> list() {
        return repo.findBySqlDeletedFalse();
    }

    @Override
    public Optional<Caps> get(String id) {
        return repo.findById(id).filter(a -> !Boolean.TRUE.equals(a.getSqlDeleted()));
    }

    @Override
    public Caps create(Caps t) {
        t.setSqlDeleted(false);
        t.setLastModified(Instant.now().toEpochMilli());
        if (t.getIdcaps() == null || t.getIdcaps().isBlank()) t.setIdcaps(java.util.UUID.randomUUID().toString());
        return repo.save(t);
    }

    @Override
    public Caps update(String id, Caps t) {
        Caps db= repo.findById(id).filter(a -> !Boolean.TRUE.equals(a.getSqlDeleted()))
                .orElseThrow(() -> new NoSuchElementException("Caps no encontrado"));
        db.setLastModified(Instant.now().toEpochMilli());
        db.setNombre(t.getNombre());
        return repo.save(db);
    }

    @Override
    public void delete(String id) {
        Caps db = get(id).orElseThrow(() -> new NoSuchElementException("Caps no encontrado"));
        db.setSqlDeleted(true);
        db.setLastModified(Instant.now().toEpochMilli());
        repo.save(db);
    }
}
