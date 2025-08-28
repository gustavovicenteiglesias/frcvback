// VisitaRepository.java
package ar.edu.unsada.frcv.repository;

import ar.edu.unsada.frcv.models.Visita;
import ar.edu.unsada.frcv.models.Persona;
import ar.edu.unsada.frcv.models.Responsable;
import org.springframework.data.repository.CrudRepository;

public interface VisitaRepository extends CrudRepository<Visita, String> {
    Iterable<Visita> findBySqlDeletedFalse();
    Iterable<Visita> findByPersonaAndSqlDeletedFalse(Persona persona);
    Iterable<Visita> findByResponsableAndSqlDeletedFalse(Responsable responsable);
}
