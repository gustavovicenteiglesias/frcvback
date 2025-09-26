package ar.edu.unsada.frcv.models;

import ar.edu.unsada.frcv.models.enums.TratamientoDiabetes;
import ar.edu.unsada.frcv.models.enums.TratamientoHta;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "antecedentes",
        indexes = {
               // @Index(name="idx_ant_persona_current", columnList="persona_id,is_current"),
                //@Index(name="idx_ant_from_to", columnList="persona_id,valid_from,valid_to"),
                @Index(name="idx_ant_lm", columnList="last_modified"),
                @Index(name="idx_ant_sd", columnList="sql_deleted")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Antecedente extends BaseEntity {

    @Id
    @Column(name="id", length = 36)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @ManyToOne
    @JoinColumn(name = "visita_id")
    @JsonIgnore
    private Visita visita;



    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private UserEntity createdBy;


    @Column(name="tabaquismo", columnDefinition = "TINYINT(1)")
    private Boolean tabaquismo;


    //Agregué al la columna de enfermedad deabetes un check de tratamiento y medicacion
    @Column(name="diabetes", columnDefinition = "TINYINT(1)")
    private Boolean diabetes;
    @Column(name="tratamiento_enf_diabetes", columnDefinition = "TINYINT(1)")
    private Boolean tratamientoEnfDiabetes;
    @Column(name = "desc_trat_enf_diabetes",length = 32)
    @Enumerated(EnumType.STRING)
    private TratamientoDiabetes descTratEnfDiabetes;

    //Agregué al la columna de enfermedad dislipemia un check de tratamiento y medicacion
    @Column(name="dislipemia", columnDefinition = "TINYINT(1)")
    private Boolean dislipemia;
    @Column(name="tratamiento_enf_dislipemia", columnDefinition = "TINYINT(1)")
    private Boolean tratamientoEnfDislipemia;
    @Column(name = "desc_trat_enf_dislipemia")
    private String descTratEnfDislipemia;

    @Column(name="hta_previa", columnDefinition = "TINYINT(1)")
    private Boolean htaPrevia;
    @Column(name="tratamiento_hta_previa", columnDefinition = "TINYINT(1)")
    private Boolean tratamientoHtaPrevia;
    @Column(name = "desc_trat_hta_previa",length = 32)
    @Enumerated(EnumType.STRING)
    private TratamientoHta descTratHtaPrevia;

    // en Antecedente.java
    @OneToMany(mappedBy = "antecedente", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    private java.util.Set<AntecedenteMedicacionHta> medicacionesHta = new java.util.LinkedHashSet<>();

    public void setMedicacionesHtaSet(java.util.Set<MedicacionHta> nuevas) {
        // reemplazo semántica de multiselect
        var links = new java.util.LinkedHashSet<AntecedenteMedicacionHta>();
        if (nuevas != null) {
            for (MedicacionHta m : nuevas) {
                if (m != null && m.getId() != null && !m.getId().isBlank()) {
                    links.add(AntecedenteMedicacionHta.link(this, m));
                }
            }
        }
        this.medicacionesHta.clear();
        this.medicacionesHta.addAll(links);
    }

    //Agregué al la columna de enfermedad cardiovascular un check de tratamiento y medicacion
    @Column(name="enf_cardiovascular", columnDefinition = "TINYINT(1)")
    private Boolean enfCardiovascular;
    @Column(name="tratamiento_enf_cardiovascular", columnDefinition = "TINYINT(1)")
    private Boolean tratamientoEnfCardiovascular;
    @Column(name = "desc_trat_enf_cardiovascular")
    private String descTratEnfCardiovascular;


    //Agregué al la columna de enfermedad renal cronica un check de tratamiento y medicacion
    @Column(name="enf_renal_cronica", columnDefinition = "TINYINT(1)")
    private Boolean enfRenalCronica;
    @Column(name="tratamiento_enf_renal", columnDefinition = "TINYINT(1)")
    private Boolean tratamientoEnfRenal;
    @Column(name = "desc_trat_enf_renal")
    private String descTratEnfRenal;



    @Lob
    @Column(name="otros")
    private String otros;
}

