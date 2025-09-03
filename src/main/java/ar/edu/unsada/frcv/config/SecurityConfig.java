package ar.edu.unsada.frcv.config;

import ar.edu.unsada.frcv.security.JwtAuthFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final ObjectMapper om = new ObjectMapper();

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    SecurityFilterChain filter(HttpSecurity http) throws Exception {
        http
                // 1) CORS básico (ajustá origins para tu Ionic/host)
                .cors(cors -> cors.configurationSource(req -> {
                    CorsConfiguration c = new CorsConfiguration();
                    c.setAllowedOriginPatterns(List.of("*")); // acepta cualquier origen
                    c.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
                    c.setAllowedHeaders(List.of("*"));
                    c.setAllowCredentials(true);
                    return c;
                }))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 3) Autorización: solo estas rutas están abiertas
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/google",
                                "/actuator/health",
                                "/ping",
                                "/api/**"
                                // si usás OpenAPI/Swagger, abrí también:
                                // "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                // 4) Manejo de errores JSON (401/403)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(401);
                            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            var out = om.writeValueAsString(Map.of(
                                    "error", "unauthorized",
                                    "message", "JWT requerido o inválido"
                            ));
                            res.getOutputStream().write(out.getBytes(StandardCharsets.UTF_8));
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(403);
                            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            var out = om.writeValueAsString(Map.of(
                                    "error", "forbidden",
                                    "message", "Permisos insuficientes"
                            ));
                            res.getOutputStream().write(out.getBytes(StandardCharsets.UTF_8));
                        })
                )

                // 5) Deshabilitar basic/form
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable());

        // 6) Nuestro filtro JWT
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}


