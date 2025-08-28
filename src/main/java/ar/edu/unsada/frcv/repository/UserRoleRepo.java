package ar.edu.unsada.frcv.repository;

import ar.edu.unsada.frcv.models.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRoleRepo extends JpaRepository<UserRoleEntity,String> {
    @Query("select r.code from RoleEntity r where r.id in (select ur.roleId from UserRoleEntity ur where ur.userId=:userId and ur.sqlDeleted=false)")
    List<String> findRoleCodesByUserId(@Param("userId") String userId);
}