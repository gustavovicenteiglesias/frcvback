package ar.edu.unsada.frcv.repository;


import ar.edu.unsada.frcv.models.Barrios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface BarriosRepository extends JpaRepository<Barrios,String> {
    Iterable<Barrios> findBySqlDeletedFalse() ;

}
