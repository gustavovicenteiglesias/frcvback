// ControlConsultorioService.java
package ar.edu.unsada.frcv.service;

import ar.edu.unsada.frcv.models.ControlConsultorio;

import java.util.Optional;

public interface ControlConsultorioService {
    Iterable<ControlConsultorio> list();
    Optional<ControlConsultorio> get(String id);
    ControlConsultorio create(ControlConsultorio c, String visitaId);
    ControlConsultorio update(String id, ControlConsultorio c, String visitaId);
    void delete(String id);
}
