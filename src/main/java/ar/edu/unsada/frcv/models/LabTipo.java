package ar.edu.unsada.frcv.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "lab_tipos",
        uniqueConstraints = @UniqueConstraint(name="uk_labtip_codigo", columnNames = "codigo"),
        indexes = {
                @Index(name="idx_labtip_lm", columnList="last_modified"),
                @Index(name="idx_labtip_sd", columnList="sql_deleted")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LabTipo extends BaseEntity {

    @Id
    @Column(name="id", length = 36)
    private String id;

    @Column(name="codigo", length = 32, nullable = false)
    private String codigo;

    @Column(name="nombre", length = 128, nullable = false)
    private String nombre;

    @Column(name="unidad_default", length = 32)
    private String unidadDefault;

    @Column(name="ref_min")
    private BigDecimal refMin;

    @Column(name="ref_max")
    private BigDecimal refMax;
}
