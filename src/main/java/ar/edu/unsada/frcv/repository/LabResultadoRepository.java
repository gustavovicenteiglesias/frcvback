package ar.edu.unsada.frcv.repository;

import ar.edu.unsada.frcv.models.LabResultado;
import ar.edu.unsada.frcv.models.LabTipo;
import ar.edu.unsada.frcv.models.Persona;
import ar.edu.unsada.frcv.models.Visita;
import org.springframework.data.repository.CrudRepository;
import java.time.LocalDateTime;

public interface LabResultadoRepository extends CrudRepository<LabResultado, String> {
    Iterable<LabResultado> findBySqlDeletedFalse();
    Iterable<LabResultado> findByPersonaAndSqlDeletedFalse(Persona persona);
    Iterable<LabResultado> findByVisitaAndSqlDeletedFalse(Visita visita);
    Iterable<LabResultado> findByTipoAndSqlDeletedFalse(LabTipo tipo);
}