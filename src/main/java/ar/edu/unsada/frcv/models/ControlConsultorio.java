package ar.edu.unsada.frcv.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "control_consultorio",
        uniqueConstraints = @UniqueConstraint(name="uk_cc_visita", columnNames = "visita_id"),
        indexes = {
                @Index(name="idx_cc_lm", columnList="last_modified"),
                @Index(name="idx_cc_sd", columnList="sql_deleted")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ControlConsultorio extends BaseEntity {

    @Id
    @Column(name="id", length = 36)
    private String id;

    @OneToOne(optional = false)
    @JoinColumn(name = "visita_id", nullable = false, unique = true)
    private Visita visita;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private UserEntity createdBy;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "asistencia",columnDefinition = "TINYINT(1)")
    private Boolean asistencia;

     @Column(name="ta_sistolica")
    private Integer taSistolica;

    @Column(name="ta_diastolica")
    private Integer taDiastolica;

    @Column(name = "confirm_hta",columnDefinition = "TINYINT(1)")
    private Boolean confirm_hta;

    @Column(name="peso")  private BigDecimal peso;   // (6,2)
    @Column(name="talla") private BigDecimal talla;  // (5,2)
    @Column(name="imc")   private BigDecimal imc;

    @Column(name = "circ_cintura")
    private Integer circCintura;

    @Column(name = "entrega_medicacion",columnDefinition = "TINYINT(1)")
    private Boolean entregaMedicacion;

    @Column(name = "control_medicacion",columnDefinition = "TINYINT(1)")
    private Boolean controlMedicacion;// Bueno o malo

    @Column(name = "conducta",columnDefinition = "TINYINT(1)")
    private Boolean conducta;

    @Column(name="eventos",columnDefinition = "TINYINT(1)")
    private Boolean eventos;
    @Column(name = "observaciones_eventos", columnDefinition = "TEXT")
    private String observaciones_eventos;

    @Column(name = "derivacion",columnDefinition = "TINYINT(1)")
    private Boolean derivacion;
    @Column(name = "observaciones_derivacion", columnDefinition = "TEXT")
    private String observaciones_derivacion;

    @Column(name="fumador")
    private Boolean fumador;
    @Column(name = "Observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "medicacion", columnDefinition = "text")
    private String medicacion;

    // Nuevos modelos relacionales
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "motivo_no_medicacion_id")
    private MotivoNoMedicacion motivoNoMedicacion;

    @OneToMany(mappedBy = "controlConsultorio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ControlConsultorioEvento> eventosConsultorio = new ArrayList<>();

    @OneToMany(mappedBy = "controlConsultorio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ControlConsultorioDerivacion> derivacionesConsultorio = new ArrayList<>();

}
