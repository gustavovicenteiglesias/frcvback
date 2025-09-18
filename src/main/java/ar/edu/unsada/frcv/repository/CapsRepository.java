package ar.edu.unsada.frcv.repository;

import ar.edu.unsada.frcv.models.Caps;

import org.springframework.data.repository.CrudRepository;

public interface CapsRepository extends CrudRepository<Caps,String> {
    Iterable<Caps> findBySqlDeletedFalse();
}
