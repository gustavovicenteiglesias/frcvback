package ar.edu.unsada.frcv.models;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @EqualsAndHashCode
public class AntMedHtaId implements Serializable {
    private String antecedenteId;
    private String medicacionId;
}
