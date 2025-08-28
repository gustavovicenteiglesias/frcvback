// PersonaService.java
package ar.edu.unsada.frcv.service;

import ar.edu.unsada.frcv.models.Persona;
import java.util.Optional;

public interface PersonaService {
    Iterable<Persona> list();
    Optional<Persona> get(String id);
    Persona create(Persona p);
    Persona update(String id, Persona p);
    void delete(String id); // soft
}
