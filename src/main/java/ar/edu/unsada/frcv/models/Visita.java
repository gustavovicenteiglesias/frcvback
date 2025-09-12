package ar.edu.unsada.frcv.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "visitas",
        indexes = {
                @Index(name="idx_visitas_persona", columnList="persona_id"),
                @Index(name="idx_visitas_tipo", columnList="tipo"),
                @Index(name="idx_visitas_lm", columnList="last_modified"),
                @Index(name="idx_visitas_sd", columnList="sql_deleted")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Visita extends BaseEntity {

    @Id
    @Column(name="id", length = 36)
    private String id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "persona_id", nullable = false)
    private Persona persona;

    @ManyToOne(optional = false)
    @JoinColumn(name = "responsable_id", nullable = false)
    private Responsable responsable;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private UserEntity createdBy;

    public enum Tipo { domicilio, consultorio }

    @Enumerated(EnumType.STRING)
    @NotNull @Column(name="tipo", nullable = false, length = 16)
    private Tipo tipo;

    @NotNull @Column(name="fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name="ubicacion_gps", length = 128)
    private String ubicacionGps;

    //@Column(name="acceso",columnDefinition = "TINYINT(1)")
   // private Boolean acceso;

    @Lob
    @Column(name="observaciones")
    private String observaciones;

    @OneToOne(mappedBy = "visita")
    private ControlDomicilio controlDomicilio;

    @OneToOne(mappedBy = "visita")
    private ControlConsultorio controlConsultorio;
}
