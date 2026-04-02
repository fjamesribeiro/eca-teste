package br.com.prognum.gestaoobras.modules.ace.infrastructure.adapter.in.web.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

/**
 * Configuração do Spring Security 6 (ET_ACE seção 6.2).
 *
 * - OAuth2 Resource Server com JWT
 * - Stateless (sem sessão HTTP — sessões gerenciadas no banco)
 * - Endpoints públicos: /api/v1/auth/**
 * - Endpoints admin: /api/v1/usuarios/** requer ADMINISTRADOR
 * - EmpreendimentoSecurityFilter posicionado antes do AuthorizationFilter
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final EmpreendimentoSecurityFilter empreendimentoSecurityFilter;

    public SecurityConfig(EmpreendimentoSecurityFilter empreendimentoSecurityFilter) {
        this.empreendimentoSecurityFilter = empreendimentoSecurityFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/usuarios/**").hasAuthority("ROLE_ADMINISTRADOR")
                        .requestMatchers("/api/v1/ace/**").hasAuthority("ROLE_ADMINISTRADOR")
                        .anyRequest().authenticated())
                .addFilterBefore(empreendimentoSecurityFilter, AuthorizationFilter.class);

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        var grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("perfil");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        var jwtAuthConverter = new JwtAuthenticationConverter();
        jwtAuthConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthConverter;
    }
}
