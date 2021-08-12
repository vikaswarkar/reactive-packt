package se.magnus.microservices.composite.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeExchange()
                .pathMatchers("/actuators/**").permitAll()
                .pathMatchers(HttpMethod.DELETE.POST, "/product-composite/**").hasAnyAuthority("SCOPE_product:write")
                .pathMatchers(HttpMethod.DELETE, "/product-composite/**").hasAnyAuthority("SCOPE_product:write")
                .pathMatchers(HttpMethod.GET,"/product-composite/**").hasAnyAuthority("SCOPE_product:read")
                .anyExchange().authenticated()
                .and()
                .oauth2ResourceServer()
                .jwt();
        return httpSecurity.build();
    }
}
