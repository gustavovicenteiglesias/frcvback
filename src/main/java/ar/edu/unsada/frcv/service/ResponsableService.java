package ar.edu.unsada.frcv.service;

import ar.edu.unsada.frcv.models.Responsable;
import java.util.Optional;

public interface ResponsableService {
    Iterable<Responsable> list();
    Optional<Responsable> get(String id);
    Responsable create(Responsable r);
    Responsable update(String id, Responsable r);
    void delete(String id);
}

