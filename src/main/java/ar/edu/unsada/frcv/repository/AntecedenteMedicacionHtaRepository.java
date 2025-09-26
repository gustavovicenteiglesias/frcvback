// AntecedenteMedicacionHtaRepository.java
package ar.edu.unsada.frcv.repository;

import ar.edu.unsada.frcv.models.AntecedenteMedicacionHta;
import ar.edu.unsada.frcv.models.AntMedHtaId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AntecedenteMedicacionHtaRepository extends JpaRepository<AntecedenteMedicacionHta, AntMedHtaId> {
    List<AntecedenteMedicacionHta> findByAntecedente_Id(String antecedenteId);
}
