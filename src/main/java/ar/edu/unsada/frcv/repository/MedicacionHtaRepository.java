package ar.edu.unsada.frcv.repository;

import ar.edu.unsada.frcv.models.MedicacionHta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicacionHtaRepository extends JpaRepository<MedicacionHta, String> {
    Optional<MedicacionHta> findByNombreIgnoreCase(String nombre);
}