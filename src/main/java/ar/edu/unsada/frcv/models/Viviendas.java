package ar.edu.unsada.frcv.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "viviendas",
        indexes = {
                @Index(name="idx_viviendas_lm", columnList="last_modified"),
                @Index(name="idx_viviendas_sd", columnList="sql_deleted")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder

public class Viviendas extends BaseEntity{
    @Id
    @Column(name = "id", length = 36)
    private String id;

    private String longitud;
    private String latitud;

    private LocalDate fecha;

    @Lob
    @Column(name="direccion")
    private String direccion;

    private String manzana;
    private String casa;

    @Column(name="accedio", columnDefinition = "TINYINT(1)")
    private Boolean accedio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caps_id")
    private Caps caps;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barrios_id")
    private  Barrios barrios;
}
