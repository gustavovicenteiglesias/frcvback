// AntecedenteService.java
package ar.edu.unsada.frcv.service;

import ar.edu.unsada.frcv.models.Antecedente;

import java.util.Optional;

public interface AntecedenteService {
    Iterable<Antecedente> list();
    Iterable<Antecedente> listByPersona(String personaId);
    Optional<Antecedente> get(String id);
    Antecedente create(Antecedente a, String personaId, String visitaId);
    Antecedente update(String id, Antecedente a, String personaId, String visitaId);
    void delete(String id);
}
