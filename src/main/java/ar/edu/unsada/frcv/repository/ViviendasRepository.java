package ar.edu.unsada.frcv.repository;

import ar.edu.unsada.frcv.models.Viviendas;
import org.springframework.data.repository.CrudRepository;

public interface ViviendasRepository extends CrudRepository<Viviendas,String> {
Iterable<Viviendas> findBySqlDeletedFalse();
}
