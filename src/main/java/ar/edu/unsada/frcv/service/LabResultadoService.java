package ar.edu.unsada.frcv.service;

import ar.edu.unsada.frcv.models.LabResultado;
import java.time.LocalDateTime;
import java.util.Optional;

public interface LabResultadoService {
    Iterable<LabResultado> list();
    Optional<LabResultado> get(String id);
    LabResultado create(LabResultado r, String personaId, String tipoId, String visitaId);
    LabResultado update(String id, LabResultado r, String personaId, String tipoId, String visitaId);
    void delete(String id);
}