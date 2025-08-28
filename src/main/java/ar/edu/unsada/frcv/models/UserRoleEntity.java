package ar.edu.unsada.frcv.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="user_roles", uniqueConstraints = @UniqueConstraint(columnNames={"user_id","role_id"}))
@Getter
@Setter
public class UserRoleEntity {
    @Id
    private String id;

    @Column(name="user_id", nullable=false, length=36)
    private String userId;

    @Column(name="role_id", nullable=false, length=36)
    private String roleId;

    private Long lastModified;
    private Boolean sqlDeleted = false;
    // getters/setters
}
