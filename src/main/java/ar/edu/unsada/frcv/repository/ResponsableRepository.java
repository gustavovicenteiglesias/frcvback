// ResponsableRepository.java
package ar.edu.unsada.frcv.repository;

import ar.edu.unsada.frcv.models.Responsable;
import org.springframework.data.repository.CrudRepository;

public interface ResponsableRepository extends CrudRepository<Responsable, String> {
    Iterable<Responsable> findBySqlDeletedFalse();
}
