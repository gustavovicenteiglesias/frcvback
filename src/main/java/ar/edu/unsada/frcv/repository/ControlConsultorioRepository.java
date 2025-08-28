package ar.edu.unsada.frcv.repository;

import ar.edu.unsada.frcv.models.ControlConsultorio;
import ar.edu.unsada.frcv.models.Visita;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ControlConsultorioRepository extends CrudRepository<ControlConsultorio, String> {
    Iterable<ControlConsultorio> findBySqlDeletedFalse();
    Optional<ControlConsultorio> findByVisitaAndSqlDeletedFalse(Visita visita);
    boolean existsByVisitaIdAndSqlDeletedFalse(String visitaId);
}