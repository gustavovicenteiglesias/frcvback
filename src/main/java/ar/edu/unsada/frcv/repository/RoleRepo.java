package ar.edu.unsada.frcv.repository;

import ar.edu.unsada.frcv.models.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepo extends JpaRepository<RoleEntity,String> {
    Optional<RoleEntity> findByCode(String code);
}
