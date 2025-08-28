package ar.edu.unsada.frcv.repository;

import ar.edu.unsada.frcv.models.LabTipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LabTipoRepository extends JpaRepository<LabTipo, String> {
    Iterable<LabTipo> findBySqlDeletedFalse();
    boolean existsByCodigoAndSqlDeletedFalse(String codigo);
    Optional<LabTipo> findByCodigoAndSqlDeletedFalse(String codigo);
}
