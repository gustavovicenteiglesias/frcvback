package ar.edu.unsada.frcv.service.impl;

import ar.edu.unsada.frcv.models.Barrios;
import ar.edu.unsada.frcv.models.Caps;
import ar.edu.unsada.frcv.models.Viviendas;
import ar.edu.unsada.frcv.repository.BarriosRepository;
import ar.edu.unsada.frcv.repository.CapsRepository;
import ar.edu.unsada.frcv.repository.ViviendasRepository;
import ar.edu.unsada.frcv.service.ViviendasService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;
@Service
@Transactional
@RequiredArgsConstructor
public class ViviendasServiceImpl implements ViviendasService {
    private final ViviendasRepository viviendasRepository;
    private final BarriosRepository barriosRepository;
    private final CapsRepository capsRepository;

    @Override
    public Iterable<Viviendas> list() {
        return viviendasRepository.findBySqlDeletedFalse();
    }

    @Override
    public Optional<Viviendas> get(String id) {
        return viviendasRepository.findById(id).filter(v -> !Boolean.TRUE.equals(v.getSqlDeleted()));
    }

    @Override
    public Viviendas create(Viviendas v,String b,String c) {
        Barrios barrio=barriosRepository.findById(b).filter(p -> !Boolean.TRUE.equals(p.getSqlDeleted()))
                .orElseThrow(() -> new NoSuchElementException("Barrio no encontrado"));
        Caps caps=capsRepository.findById(c).filter(p -> !Boolean.TRUE.equals(p.getSqlDeleted()))
                .orElseThrow(() -> new NoSuchElementException("Barrio no encontrado"));
        v.setBarrios(barrio);
        v.setCaps(caps);
        v.setSqlDeleted(false);
        v.setLastModified(Instant.now().toEpochMilli());
        if (v.getId() == null || v.getId().isBlank()) v.setId(java.util.UUID.randomUUID().toString());
        viviendasRepository.save(v);
        return v;
    }

    @Override
    public Viviendas update(String id, Viviendas v, String b, String c) {
        Viviendas db = get(id).orElseThrow(() -> new NoSuchElementException("Visita no encontrada"));
        if(b != null){
            Barrios barrio=barriosRepository.findById(b).filter(p -> !Boolean.TRUE.equals(p.getSqlDeleted()))
                    .orElseThrow(() -> new NoSuchElementException("Barrio no encontrado"));
            db.setBarrios(barrio);
        }
        if(c != null){
            Caps caps=capsRepository.findById(c).filter(p -> !Boolean.TRUE.equals(p.getSqlDeleted()))
                    .orElseThrow(() -> new NoSuchElementException("Barrio no encontrado"));
            db.setCaps(caps);
        }
        db.setLatitud(v.getLatitud());
        db.setLongitud(v.getLongitud());
        db.setFecha(v.getFecha());
        db.setDireccion(v.getDireccion());
        db.setManzana(v.getManzana());
        db.setCasa(v.getCasa());
        db.setAccedio(v.getAccedio());
        db.setLastModified(Instant.now().toEpochMilli());

        return viviendasRepository.save(db) ;
    }


    @Override
    public void delete(String id) {
        Viviendas db = get(id).orElseThrow(() -> new NoSuchElementException("Visita no encontrada"));
        db.setSqlDeleted(true);
        db.setLastModified(Instant.now().toEpochMilli());
        viviendasRepository.save(db);

    }
}
