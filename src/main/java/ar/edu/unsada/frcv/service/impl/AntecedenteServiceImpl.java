// AntecedenteServiceImpl.java
package ar.edu.unsada.frcv.service.impl;

import ar.edu.unsada.frcv.models.*;
import ar.edu.unsada.frcv.models.AntMedHtaId; // <-- ojo el paquete
import ar.edu.unsada.frcv.repository.AntecedenteMedicacionHtaRepository;
import ar.edu.unsada.frcv.repository.AntecedenteRepository;
import ar.edu.unsada.frcv.repository.MedicacionHtaRepository;
import ar.edu.unsada.frcv.repository.PersonaRepository;
import ar.edu.unsada.frcv.repository.VisitaRepository;
import ar.edu.unsada.frcv.service.AntecedenteService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AntecedenteServiceImpl implements AntecedenteService {
    private final AntecedenteRepository repo;
    private final PersonaRepository personaRepo;
    private final VisitaRepository visitaRepo;
    private final MedicacionHtaRepository medRepo;
    private final AntecedenteMedicacionHtaRepository linkRepo;

    public AntecedenteServiceImpl(AntecedenteRepository repo,
                                  PersonaRepository personaRepo,
                                  VisitaRepository visitaRepo,
                                  MedicacionHtaRepository medRepo,
                                  AntecedenteMedicacionHtaRepository linkRepo) {
        this.repo = repo;
        this.personaRepo = personaRepo;
        this.visitaRepo = visitaRepo;
        this.medRepo = medRepo;
        this.linkRepo = linkRepo;
    }

    // ----------------- helpers -----------------

    /** Convierte Set<AntecedenteMedicacionHta> (links del payload) a Set<MedicacionHta> del catálogo. */
    private Set<MedicacionHta> resolveMedicacionesFromLinks(Set<AntecedenteMedicacionHta> incomingLinks) {
        Set<MedicacionHta> out = new LinkedHashSet<>();
        if (incomingLinks == null || incomingLinks.isEmpty()) return out;

        for (AntecedenteMedicacionHta link : incomingLinks) {
            if (link == null) continue;

            // 1) Si viene la entidad medicación embebida
            MedicacionHta m = link.getMedicacion();
            if (m != null) {
                if (m.getId() != null && !m.getId().isBlank()) {
                    medRepo.findById(m.getId()).ifPresent(out::add);
                    continue;
                }
                if (m.getNombre() != null && !m.getNombre().isBlank()) {
                    medRepo.findByNombreIgnoreCase(m.getNombre().trim()).ifPresent(out::add);
                    continue;
                }
            }

            // 2) Si sólo viene la PK compuesta (antecedente_id, medicacion_id)
            if (link.getId() != null && link.getId().getMedicacionId() != null) {
                String mid = link.getId().getMedicacionId();
                if (!mid.isBlank()) medRepo.findById(mid).ifPresent(out::add);
            }
        }
        return out;
    }

    private long nowSec() { return Instant.now().getEpochSecond(); }

    /** Resuelve el set entrante (ids o nombres) a entidades gestionadas (catálogo). */
    private Set<MedicacionHta> resolveMedicaciones(Set<MedicacionHta> incoming) {
        if (incoming == null || incoming.isEmpty()) return new LinkedHashSet<>();
        Set<MedicacionHta> out = new LinkedHashSet<>();
        for (MedicacionHta m : incoming) {
            if (m == null) continue;
            String id = m.getId();
            if (id != null && !id.isBlank()) {
                medRepo.findById(id).ifPresent(out::add);
                continue;
            }
            String nombre = m.getNombre();
            if (nombre != null && !nombre.isBlank()) {
                medRepo.findByNombreIgnoreCase(nombre.trim()).ifPresent(out::add);
            }
        }
        return out;
    }

    /** Upsert de la tabla puente con soft-delete + last_modified. Reemplaza selección (multiselect). */
    private void upsertLinks(Antecedente db, Set<MedicacionHta> target) {
        long ts = nowSec();

        // links actuales (activos o no)
        List<AntecedenteMedicacionHta> existingAll = linkRepo.findByAntecedente_Id(db.getId()); // <-- método correcto
        Map<String, AntecedenteMedicacionHta> byMedId = existingAll.stream()
                .collect(Collectors.toMap(l -> l.getMedicacion().getId(), l -> l, (a, b) -> a, LinkedHashMap::new));

        // target ids
        Set<String> targetIds = (target == null) ? Set.of() :
                target.stream()
                        .filter(Objects::nonNull)
                        .map(MedicacionHta::getId)
                        .filter(Objects::nonNull)
                        .filter(s -> !s.isBlank())
                        .collect(Collectors.toCollection(LinkedHashSet::new));

        // habilitar/crear los que deben quedar
        for (String medId : targetIds) {
            AntecedenteMedicacionHta link = byMedId.get(medId);
            if (link == null) {
                MedicacionHta med = medRepo.findById(medId)
                        .orElseThrow(() -> new NoSuchElementException("Medicacion HTA no encontrada: " + medId));
                link = AntecedenteMedicacionHta.builder()
                        .id(new AntMedHtaId(db.getId(), med.getId()))
                        .antecedente(db)
                        .medicacion(med)
                        .lastModified(ts)
                        .sqlDeleted(false)
                        .build();
                linkRepo.save(link);
            } else {
                if (Boolean.TRUE.equals(link.getSqlDeleted())) {
                    link.setSqlDeleted(false);
                }
                link.setLastModified(ts);
                linkRepo.save(link);
            }
        }

        // dar de baja (soft-delete) los que ya no están en target
        for (AntecedenteMedicacionHta link : existingAll) {
            String mid = link.getMedicacion().getId();
            if (!targetIds.contains(mid) && !Boolean.TRUE.equals(link.getSqlDeleted())) {
                link.setSqlDeleted(true);
                link.setLastModified(ts);
                linkRepo.save(link);
            }
        }
    }
    // -------------------------------------------

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
        a.setLastModified(nowSec());
        if (a.getId() == null || a.getId().isBlank()) a.setId(java.util.UUID.randomUUID().toString());

        // Guardamos primero el antecedente para asegurar PK
        Antecedente db = repo.save(a);

        // Si tu payload trae Set<MedicacionHta> en Antecedente:
        Set<MedicacionHta> target = resolveMedicacionesFromLinks(a.getMedicacionesHta());
        upsertLinks(db, target);

        // tocar LM del antecedente para que el delta lo levante
        db.setLastModified(nowSec());
        return repo.save(db);
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

        db.setTabaquismo(a.getTabaquismo());
        db.setDiabetes(a.getDiabetes());
        db.setTratamientoEnfDiabetes(a.getTratamientoEnfDiabetes());
        db.setDescTratEnfDiabetes(a.getDescTratEnfDiabetes());
        db.setDislipemia(a.getDislipemia());
        db.setTratamientoEnfDislipemia(a.getTratamientoEnfDislipemia());
        db.setDescTratEnfDislipemia(a.getDescTratEnfDislipemia());
        db.setHtaPrevia(a.getHtaPrevia());
        db.setTratamientoHtaPrevia(a.getTratamientoHtaPrevia());
        db.setDescTratHtaPrevia(a.getDescTratHtaPrevia());
        db.setEnfRenalCronica(a.getEnfRenalCronica());
        db.setTratamientoEnfRenal(a.getTratamientoEnfRenal());
        db.setDescTratEnfRenal(a.getDescTratEnfRenal());
        db.setOtros(a.getOtros());
        db.setCreatedBy(a.getCreatedBy());

        // Reemplazo de selección de medicaciones → upsert en puente
        Set<MedicacionHta> target = resolveMedicacionesFromLinks(a.getMedicacionesHta());
        upsertLinks(db, target);

        db.setLastModified(nowSec());
        return repo.save(db);
    }

    @Override public void delete(String id) {
        Antecedente db = get(id).orElseThrow(() -> new NoSuchElementException("Antecedente no encontrado"));
        db.setSqlDeleted(true);
        db.setLastModified(nowSec());
        repo.save(db);

        // Baja lógica de TODAS las relaciones en la tabla puente (idempotente)
        List<AntecedenteMedicacionHta> links = linkRepo.findByAntecedente_Id(id); // <-- método correcto
        long ts = nowSec();
        for (AntecedenteMedicacionHta l : links) {
            if (!Boolean.TRUE.equals(l.getSqlDeleted())) {
                l.setSqlDeleted(true);
                l.setLastModified(ts);
                linkRepo.save(l);
            }
        }
    }
}
