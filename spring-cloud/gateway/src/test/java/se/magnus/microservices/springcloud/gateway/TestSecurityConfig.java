package se.magnus.microservices.springcloud.gateway;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;


//@EnableWebFluxSecurity
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity httpSecurity){
        httpSecurity.csrf().disable().authorizeExchange().anyExchange().permitAll();
        return httpSecurity.build();
    }

//    @Bean
//    FilterChain springSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
//        httpSecurity.csrf().disable().authorizeRequests().anyRequest().permitAll();
//        return httpSecurity.
//    }
}
