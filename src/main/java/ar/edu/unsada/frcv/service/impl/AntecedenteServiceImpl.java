// AntecedenteServiceImpl.java
package ar.edu.unsada.frcv.service.impl;

import ar.edu.unsada.frcv.models.*;
import ar.edu.unsada.frcv.repository.AntecedenteRepository;
import ar.edu.unsada.frcv.repository.PersonaRepository;
import ar.edu.unsada.frcv.repository.VisitaRepository;
import ar.edu.unsada.frcv.service.AntecedenteService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service @Transactional
public class AntecedenteServiceImpl implements AntecedenteService {
    private final AntecedenteRepository repo;
    private final PersonaRepository personaRepo;
    private final VisitaRepository visitaRepo;

    public AntecedenteServiceImpl(AntecedenteRepository repo, PersonaRepository personaRepo, VisitaRepository visitaRepo) {
        this.repo = repo; this.personaRepo = personaRepo; this.visitaRepo = visitaRepo;
    }

    @Override public Iterable<Antecedente> list() { return repo.findBySqlDeletedFalse(); }

    @Override public Iterable<Antecedente> listByPersona(String personaId) {
        Persona p = personaRepo.findById(personaId).filter(x -> !Boolean.TRUE.equals(x.getSqlDeleted()))
                .orElseThrow(() -> new NoSuchElementException("Persona no encontrada"));
        return repo.findByPersonaAndSqlDeletedFalse(p);
    }

    @Override public Optional<Antecedente> get(String id) {
        return repo.findById(id).filter(a -> !Boolean.TRUE.equals(a.getSqlDeleted()));
    }

    @Override public Antecedente create(Antecedente a, String personaId, String visitaId) {
        Persona p = personaRepo.findById(personaId).filter(x -> !Boolean.TRUE.equals(x.getSqlDeleted()))
                .orElseThrow(() -> new NoSuchElementException("Persona no encontrada"));
        a.setPersona(p);

        if (visitaId != null) {
            Visita v = visitaRepo.findById(visitaId).filter(x -> !Boolean.TRUE.equals(x.getSqlDeleted()))
                    .orElseThrow(() -> new NoSuchElementException("Visita no encontrada"));
            a.setVisita(v);
        }

        a.setSqlDeleted(false);
        a.setLastModified(Instant.now().toEpochMilli());
        if (a.getId() == null || a.getId().isBlank()) a.setId(java.util.UUID.randomUUID().toString());
        return repo.save(a);
    }

    @Override public Antecedente update(String id, Antecedente a, String personaId, String visitaId) {
        Antecedente db = get(id).orElseThrow(() -> new NoSuchElementException("Antecedente no encontrado"));

        if (personaId != null) {
            Persona p = personaRepo.findById(personaId).filter(x -> !Boolean.TRUE.equals(x.getSqlDeleted()))
                    .orElseThrow(() -> new NoSuchElementException("Persona no encontrada"));
            db.setPersona(p);
        }
        if (visitaId != null) {
            Visita v = visitaRepo.findById(visitaId).filter(x -> !Boolean.TRUE.equals(x.getSqlDeleted()))
                    .orElseThrow(() -> new NoSuchElementException("Visita no encontrada"));
            db.setVisita(v);
        }

        db.setCurrent(a.getCurrent());
        db.setValidFrom(a.getValidFrom());
        db.setValidTo(a.getValidTo());

        db.setTabaquismo(a.getTabaquismo());
        db.setExTabaquista(a.getExTabaquista());
        db.setDiabetes(a.getDiabetes());
        db.setDislipemia(a.getDislipemia());
        db.setHtaPrevia(a.getHtaPrevia());
        db.setEnfCardiovascular(a.getEnfCardiovascular());
        db.setEnfRenalCronica(a.getEnfRenalCronica());
        db.setFamCvdPrecoz(a.getFamCvdPrecoz());
        db.setAlcoholRiesgo(a.getAlcoholRiesgo());
        db.setActividadFisicaBaja(a.getActividadFisicaBaja());
        db.setObesidad(a.getObesidad());
        db.setOtros(a.getOtros());

        db.setCreatedBy(a.getCreatedBy());
        db.setLastModified(Instant.now().toEpochMilli());
        return repo.save(db);
    }

    @Override public void delete(String id) {
        Antecedente db = get(id).orElseThrow(() -> new NoSuchElementException("Antecedente no encontrado"));
        db.setSqlDeleted(true);
        db.setLastModified(Instant.now().toEpochMilli());
        repo.save(db);
    }
}
