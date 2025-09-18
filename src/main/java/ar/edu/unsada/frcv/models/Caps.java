package ar.edu.unsada.frcv.models;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "caps",
        indexes = {
                @Index(name="idx_caps_lm", columnList="last_modified"),
                @Index(name="idx_caps_sd", columnList="sql_deleted")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Caps extends BaseEntity{
    @Id
    @Column(name = "id_caps", length = 36)
    private String idcaps;

    private String nombre;
}