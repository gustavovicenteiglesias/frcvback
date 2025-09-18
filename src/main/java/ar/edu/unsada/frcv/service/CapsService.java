package ar.edu.unsada.frcv.service;

import ar.edu.unsada.frcv.models.Caps;

import java.util.Optional;

public interface CapsService {
    Iterable<Caps> list();
    Optional<Caps> get(String id);
    Caps create(Caps c);
    Caps update(String id,Caps c);
    void delete(String id);
}
