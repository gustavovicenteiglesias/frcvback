// ControlDomicilioRepository.java
package ar.edu.unsada.frcv.repository;

import ar.edu.unsada.frcv.models.ControlDomicilio;
import ar.edu.unsada.frcv.models.Visita;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ControlDomicilioRepository extends CrudRepository<ControlDomicilio, String> {
    Iterable<ControlDomicilio> findBySqlDeletedFalse();
    Optional<ControlDomicilio> findByVisitaAndSqlDeletedFalse(Visita visita);
    boolean existsByVisitaIdAndSqlDeletedFalse(String visitaId);
}
