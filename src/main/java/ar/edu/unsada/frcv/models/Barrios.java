package ar.edu.unsada.frcv.models;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "barrios",
        indexes = {
                @Index(name="idx_barrios_lm", columnList="last_modified"),
                @Index(name="idx_barrios_sd", columnList="sql_deleted")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Barrios extends BaseEntity{
    @Id
    @Column(name = "id", length = 36)
    private String idBarrios;

    private String nombre;
}
