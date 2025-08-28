package ar.edu.unsada.frcv.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity {
    @Id
    @Column(name="id", length = 36)   // ahora Hibernate espera VARCHAR(36)
    private String id;

    @Column(name="google_sub", unique = true)
    private String googleSub;

    @Column(unique = true, nullable = false)
    private String email;

    private String nombre;
    private String fotoUrl;
    private Boolean activo = true;

    private Long lastModified;
    private Boolean sqlDeleted = false;

    private LocalDateTime ultimoLogin;

    // getters/setters
}
