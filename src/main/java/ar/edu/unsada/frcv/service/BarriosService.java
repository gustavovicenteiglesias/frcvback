package ar.edu.unsada.frcv.service;

import ar.edu.unsada.frcv.models.Barrios;

import java.util.Optional;

public interface BarriosService {
    Iterable<Barrios> list();
    Optional<Barrios> get(String id);
    Barrios create(Barrios t);
    Barrios update(String id,Barrios t );
    void delete(String id);
}
