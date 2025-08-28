package ar.edu.unsada.frcv.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lab_resultados",
        indexes = {
                @Index(name="idx_lr_lm", columnList="last_modified"),
                @Index(name="idx_lr_sd", columnList="sql_deleted")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LabResultado extends BaseEntity {

    @Id
    @Column(name="id", length = 36)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @ManyToOne
    @JoinColumn(name = "visita_id")
    private Visita visita;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tipo_id", nullable = false)
    private LabTipo tipo;

    @Column(name="fecha_realizado", nullable = false)
    private LocalDateTime fechaRealizado;

    @Column(name="valor_num")
    private BigDecimal valorNum;

    @Column(name="valor_texto", length = 256)
    private String valorTexto;

    @Column(name="unidad", length = 32)
    private String unidad;

    @Column(name="laboratorio", length = 128)
    private String laboratorio;

    @Lob
    @Column(name="observaciones")
    private String observaciones;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private UserEntity createdBy;
}

