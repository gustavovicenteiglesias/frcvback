// ControlDomicilioService.java
package ar.edu.unsada.frcv.service;

import ar.edu.unsada.frcv.models.ControlDomicilio;

import java.util.Optional;

public interface ControlDomicilioService {
    Iterable<ControlDomicilio> list();
    Optional<ControlDomicilio> get(String id);
    ControlDomicilio create(ControlDomicilio c, String visitaId);
    ControlDomicilio update(String id, ControlDomicilio c, String visitaId);
    void delete(String id);
}
