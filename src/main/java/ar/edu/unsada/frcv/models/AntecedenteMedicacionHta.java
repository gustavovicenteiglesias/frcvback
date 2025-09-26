// src/main/java/ar/edu/unsada/frcv/models/AntecedenteMedicacionHta.java
package ar.edu.unsada.frcv.models;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "antecedente_has_medicacion_hta",
        indexes = {
                @Index(name="idx_ant_medhta_ant", columnList="antecedente_id"),
                @Index(name="idx_ant_medhta_med", columnList="medicacion_id"),
                @Index(name="idx_ant_medhta_lm", columnList="last_modified"),
                @Index(name="idx_ant_medhta_sd", columnList="sql_deleted")
        },
        uniqueConstraints = {
                @UniqueConstraint(name="uk_ant_medhta", columnNames = {"antecedente_id","medicacion_id"})
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AntecedenteMedicacionHta {

    @EmbeddedId
    private AntMedHtaId id;

    @MapsId("antecedenteId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "antecedente_id", nullable = false)
    private Antecedente antecedente;

    @MapsId("medicacionId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "medicacion_id", nullable = false)
    private MedicacionHta medicacion;

    @Column(name="last_modified", nullable = false)
    private Long lastModified; // en segundos

    @Column(name="sql_deleted", nullable = false)
    private Boolean sqlDeleted;

    @PrePersist @PreUpdate
    public void touch() {
        if (lastModified == null) lastModified = System.currentTimeMillis() / 1000L;
        else lastModified = System.currentTimeMillis() / 1000L;
        if (sqlDeleted == null) sqlDeleted = false;
    }

    public static AntecedenteMedicacionHta link(Antecedente a, MedicacionHta m) {
        return AntecedenteMedicacionHta.builder()
                .id(new AntMedHtaId(a.getId(), m.getId()))
                .antecedente(a)
                .medicacion(m)
                .lastModified(System.currentTimeMillis()/1000L)
                .sqlDeleted(false)
                .build();
    }
}
