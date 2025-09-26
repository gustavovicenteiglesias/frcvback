package ar.edu.unsada.frcv.models;

import ar.edu.unsada.frcv.models.enums.CoberturaSalud;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "personas",
        indexes = {
                @Index(name="idx_personas_lm", columnList="last_modified"),
                @Index(name="idx_personas_sd", columnList="sql_deleted")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Persona extends BaseEntity {

    @Id
    @Column(name="id", length = 36)
    private String id;

    @Column(name="dni", length = 32)
    private String dni;

    @NotBlank @Column(name="apellido", length = 128, nullable = false)
    private String apellido;

    @NotBlank @Column(name="nombre", length = 128, nullable = false)
    private String nombre;

    @Column(name="sexo", length = 32)
    private String sexo;

    @Column(name="fecha_nac")
    private LocalDate fechaNac;

    @Column(name="telefono", length = 64)
    private String telefono;

    @Enumerated(EnumType.STRING)
    @Column(name = "cobertura_salud", length = 40)
    //@JsonProperty("cobertura_salud") // si tu JSON va en snake_case; si usás camelCase, podés quitarlo.
    private CoberturaSalud coberturaSalud;

   /* @Column(name="direccion", length = 256)
    private String direccion;*/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viviendas_id")
    private Viviendas viviendas;
}
