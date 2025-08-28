package ar.edu.unsada.frcv.repository;

import ar.edu.unsada.frcv.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<UserEntity,String> {
    Optional<UserEntity> findByGoogleSub(String sub);
    Optional<UserEntity> findByEmail(String email);
}
