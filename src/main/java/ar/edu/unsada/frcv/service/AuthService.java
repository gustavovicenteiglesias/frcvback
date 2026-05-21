package ar.edu.unsada.frcv.service;

import ar.edu.unsada.frcv.models.UserEntity;
import ar.edu.unsada.frcv.repository.UserRepo;
import ar.edu.unsada.frcv.repository.UserRoleRepo;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepo userRepo;
    private final UserRoleRepo userRoleRepo;
    private final JwtService jwtService;

    public AuthService(UserRepo userRepo, UserRoleRepo userRoleRepo, JwtService jwtService) {
        this.userRepo = userRepo;
        this.userRoleRepo = userRoleRepo;
        this.jwtService = jwtService;
    }

    @Transactional
    public Map<String,Object> authenticateWithGooglePayload(GoogleIdToken.Payload payload) {
        String sub   = payload.getSubject();
        String email = payload.getEmail();
        String name  = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        UserEntity user = userRepo.findByGoogleSub(sub)
                .or(() -> userRepo.findByEmail(email))
                .orElseGet(() -> {
                    UserEntity u = new UserEntity();
                    u.setId(UUID.randomUUID().toString());
                    u.setGoogleSub(sub);
                    u.setEmail(email);
                    u.setNombre(name);
                    u.setFotoUrl(picture);
                    u.setActivo(true);
                    u.setLastModified(Instant.now().getEpochSecond());
                    return userRepo.save(u);
                });

        List<String> roles = userRoleRepo.findRoleCodesByUserId(user.getId());

        user.setUltimoLogin(LocalDateTime.now());
        user.setLastModified(Instant.now().getEpochSecond());
        userRepo.save(user);

        String jwt = jwtService.issue(user.getId(), user.getEmail(), roles);

        Map<String,Object> resp = new LinkedHashMap<>();
        resp.put("jwt", jwt);
        resp.put("userId", user.getId());
        resp.put("email", user.getEmail());
        resp.put("name",user.getNombre());
        resp.put("picture",user.getFotoUrl());
        resp.put("roles", roles);
        resp.put("exp", Instant.now().plusSeconds(28800).getEpochSecond());
        return resp;
    }
}
