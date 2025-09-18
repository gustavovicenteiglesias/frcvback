package ar.edu.unsada.frcv.service;

import ar.edu.unsada.frcv.models.Viviendas;

import java.util.Optional;

public interface ViviendasService {
    Iterable<Viviendas> list();
    Optional<Viviendas> get(String id);
    Viviendas create(Viviendas v,String b,String c);
    Viviendas update(String id,Viviendas v,String b,String c);
    void delete(String id);
}
