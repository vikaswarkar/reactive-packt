package se.magnus.springcloud.authserver;

/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * An instance of Legacy Authorization Server (spring-security-oauth2) that uses a single,
 * not-rotating key and exposes a JWK endpoint.
 *
 * See
 * <a
 * 	target="_blank"
 * 	href="https://docs.spring.io/spring-security-oauth2-boot/docs/current-SNAPSHOT/reference/htmlsingle/">
 * 	Spring Security OAuth Autoconfig's documentation</a> for additional detail
 *
 * @author Josh Cummings
 * @since 5.1
 */
@EnableAuthorizationServer
@Configuration
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    AuthenticationManager authenticationManager;
    KeyPair keyPair;
    boolean jwtEnabled;

    public AuthorizationServerConfiguration(
            AuthenticationConfiguration authenticationConfiguration,
            KeyPair keyPair,
            @Value("${security.oauth2.authorizationserver.jwt.enabled:true}") boolean jwtEnabled) throws Exception {

        this.authenticationManager = authenticationConfiguration.getAuthenticationManager();
        this.keyPair = keyPair;
        this.jwtEnabled = jwtEnabled;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients)
            throws Exception {

        // Password is prefixed with {noop} to indicate to DelegatingPasswordEncoder that
        // NoOpPasswordEncoder should be used.
        // This is not safe for production, but makes reading
        // in samples easier.
        // Normally passwords should be hashed using BCrypt

        // @formatter:off
        clients.inMemory()
                .withClient("reader")
                .authorizedGrantTypes("code","authorization_code", "implicit","password")
                .redirectUris("http://my.redirect.uri")
                .secret("{noop}secret")
                .scopes("product:read")
                .accessTokenValiditySeconds(600_000_000)
                .and()
                .withClient("writer")
                .authorizedGrantTypes("code","authorization_code", "implicit","password")
                .redirectUris("http://my.redirect.uri")
                .secret("{noop}secret")
                .scopes("product:read", "product:write")
                .accessTokenValiditySeconds(600_000_000)
                .and()
                .withClient("noscopes")
                .authorizedGrantTypes("code","authorization_code", "implicit","password")
                .redirectUris("http://my.redirect.uri")
                .secret("{noop}secret")
                .scopes("none")
                .accessTokenValiditySeconds(600_000_000);
        // @formatter:on
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        // @formatter:off
        endpoints
                .authenticationManager(this.authenticationManager)
                .tokenStore(tokenStore());

        if (this.jwtEnabled) {
            endpoints
                    .accessTokenConverter(accessTokenConverter());
        }
        // @formatter:on
    }

    @Bean
    public TokenStore tokenStore() {
        if (this.jwtEnabled) {
            return new JwtTokenStore(accessTokenConverter());
        } else {
            return new InMemoryTokenStore();
        }
    }

    @Bean
    public JwtAccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setKeyPair(this.keyPair);

        DefaultAccessTokenConverter accessTokenConverter = new DefaultAccessTokenConverter();
        accessTokenConverter.setUserTokenConverter(new SubjectAttributeUserTokenConverter());
        converter.setAccessTokenConverter(accessTokenConverter);

        return converter;
    }
}

