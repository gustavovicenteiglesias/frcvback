// AntecedenteRepository.java
package ar.edu.unsada.frcv.repository;

import ar.edu.unsada.frcv.models.Antecedente;
import ar.edu.unsada.frcv.models.Persona;
import org.springframework.data.repository.CrudRepository;

public interface AntecedenteRepository extends CrudRepository<Antecedente, String> {
    Iterable<Antecedente> findBySqlDeletedFalse();
    Iterable<Antecedente> findByPersonaAndSqlDeletedFalse(Persona persona);
    //Iterable<Antecedente> findByPersonaIdAndCurrentTrueAndSqlDeletedFalse(String personaId);
}
