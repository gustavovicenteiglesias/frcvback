package ar.edu.unsada.frcv.service;

import ar.edu.unsada.frcv.models.LabTipo;

import java.util.List;
import java.util.Optional;

public interface LabTipoService {
    Iterable<LabTipo> list();
    Optional<LabTipo> get(String id);
    LabTipo create(LabTipo t);
    LabTipo update(String id, LabTipo t);
    void delete(String id);
}
