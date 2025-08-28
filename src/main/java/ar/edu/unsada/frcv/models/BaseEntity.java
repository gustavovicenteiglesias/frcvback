package ar.edu.unsada.frcv.models;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter @Setter
public abstract class BaseEntity {
    @NotNull
    @Column(name = "last_modified", nullable = false)
    private Long lastModified;

    @NotNull
    @Column(name = "sql_deleted", nullable = false)
    private Boolean sqlDeleted = false;
}
