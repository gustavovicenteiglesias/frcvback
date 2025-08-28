package ar.edu.unsada.frcv.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "control_domicilio",
        uniqueConstraints = @UniqueConstraint(name="uk_cd_visita", columnNames = "visita_id"),
        indexes = {
                @Index(name="idx_cd_lm", columnList="last_modified"),
                @Index(name="idx_cd_sd", columnList="sql_deleted")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ControlDomicilio extends BaseEntity {

    @Id
    @Column(name="id", length = 36)
    private String id;

    @OneToOne(optional = false)
    @JoinColumn(name = "visita_id", nullable = false, unique = true)
    private Visita visita;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private UserEntity createdBy;

    @Column(name="ta_sistolica")
    private Integer taSistolica;

    @Column(name="ta_diastolica")
    private Integer taDiastolica;

    @Column(name="frecuencia_cardiaca")
    private Integer frecuenciaCardiaca;

    @Column(name="peso")  private BigDecimal peso;   // (6,2)
    @Column(name="talla") private BigDecimal talla;  // (5,2)
    @Column(name="imc")   private BigDecimal imc;    // (5,2)

    @Column(name="riesgo_cv")
    private Byte riesgoCv;

    @Column(name="derivar_a_consultorio", columnDefinition = "TINYINT(1)")
    private Boolean derivarAConsultorio;

    @Lob
    @Column(name="observaciones")
    private String observaciones;
}
