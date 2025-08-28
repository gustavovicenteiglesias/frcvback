package ar.edu.unsada.frcv.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "responsables",
        indexes = {
                @Index(name="idx_resp_lm", columnList="last_modified"),
                @Index(name="idx_resp_sd", columnList="sql_deleted")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Responsable extends BaseEntity {

    @Id
    @Column(name="id", length = 36)
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id") // puede ser NULL
    private UserEntity user;

    @Column(name="nombre", length = 256, nullable = false)
    private String nombre;

    @Column(name="matricula", length = 64)
    private String matricula;

    @Column(name="email", length = 256)
    private String email;

    @Column(name="activo")
    private Boolean activo = true;
}

