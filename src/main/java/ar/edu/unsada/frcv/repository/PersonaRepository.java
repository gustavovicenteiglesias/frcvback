// PersonaRepository.java
package ar.edu.unsada.frcv.repository;

import ar.edu.unsada.frcv.models.Persona;
import org.springframework.data.repository.CrudRepository;

public interface PersonaRepository extends CrudRepository<Persona, String> {
    Iterable<Persona> findBySqlDeletedFalse();
    boolean existsByDniAndSqlDeletedFalse(String dni);
}
