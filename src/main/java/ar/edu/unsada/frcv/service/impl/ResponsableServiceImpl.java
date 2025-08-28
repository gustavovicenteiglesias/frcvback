// ResponsableServiceImpl.java
package ar.edu.unsada.frcv.service.impl;

import ar.edu.unsada.frcv.models.Responsable;
import ar.edu.unsada.frcv.repository.ResponsableRepository;
import ar.edu.unsada.frcv.service.ResponsableService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
public class ResponsableServiceImpl implements ResponsableService {
    private final ResponsableRepository repo;
    public ResponsableServiceImpl(ResponsableRepository repo) { this.repo = repo; }

    @Override public Iterable<Responsable> list() { return repo.findBySqlDeletedFalse(); }

    @Override public Optional<Responsable> get(String id) { return repo.findById(id).filter(r -> !Boolean.TRUE.equals(r.getSqlDeleted())); }

    @Override public Responsable create(Responsable r) {
        r.setSqlDeleted(false);
        r.setLastModified(Instant.now().toEpochMilli());
        if (r.getId() == null || r.getId().isBlank()) r.setId(java.util.UUID.randomUUID().toString());
        return repo.save(r);
    }

    @Override public Responsable update(String id, Responsable r) {
        Responsable db = get(id).orElseThrow(() -> new NoSuchElementException("Responsable no encontrado"));
        db.setNombre(r.getNombre());
        db.setMatricula(r.getMatricula());
        db.setEmail(r.getEmail());
        db.setActivo(r.getActivo());
        db.setUser(r.getUser());
        db.setLastModified(Instant.now().toEpochMilli());
        return repo.save(db);
    }

    @Override public void delete(String id) {
        Responsable db = get(id).orElseThrow(() -> new NoSuchElementException("Responsable no encontrado"));
        db.setSqlDeleted(true);
        db.setLastModified(Instant.now().toEpochMilli());
        repo.save(db);
    }
}
