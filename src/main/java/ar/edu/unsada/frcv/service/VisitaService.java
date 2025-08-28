package ar.edu.unsada.frcv.service;

import ar.edu.unsada.frcv.models.Visita;
import java.util.Optional;

public interface VisitaService {
    Iterable<Visita> list();
    Optional<Visita> get(String id);
    Visita create(Visita v, String personaId, String responsableId);
    Visita update(String id, Visita v, String personaId, String responsableId);
    void delete(String id);
}