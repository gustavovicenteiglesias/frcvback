package ar.edu.unsada.frcv.controller;

import ar.edu.unsada.frcv.service.AuthService;
import ar.edu.unsada.frcv.service.GoogleTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthController {

    private final GoogleTokenVerifier googleTokenVerifier;
    private final AuthService authService;

    public AuthController(GoogleTokenVerifier googleTokenVerifier, AuthService authService) {
        this.googleTokenVerifier = googleTokenVerifier;
        this.authService = authService;
    }

    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@RequestBody Map<String,String> body) {
        try {
            String idToken = body.get("id_token");
            if (idToken == null || idToken.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error","id_token requerido"));
            }

            GoogleIdToken.Payload payload = googleTokenVerifier.verify(idToken);
            if (payload == null || !Boolean.TRUE.equals(payload.getEmailVerified())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","id_token inválido"));
            }

            return ResponseEntity.ok(authService.authenticateWithGooglePayload(payload));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error","auth_failed","detail", e.getMessage()));
        }
    }
}


