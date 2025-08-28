package ar.edu.unsada.frcv.controller;

import ar.edu.unsada.frcv.security.JwtUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class MeController {
    @GetMapping("/me")
    public Map<String,Object> me(@AuthenticationPrincipal JwtUser user) {
        return Map.of(
                "userId", user.getUserId(),
                "email", user.getEmail(),
                "roles", user.getRoles()
        );
    }
}
