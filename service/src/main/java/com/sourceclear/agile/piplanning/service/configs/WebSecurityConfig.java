package com.sourceclear.agile.piplanning.service.configs;

import com.sourceclear.agile.piplanning.service.components.JwtTokenProvider;
import com.sourceclear.agile.piplanning.service.services.TokenService;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private final JwtTokenProvider jwtTokenProvider;

  private final TokenService tokenService;

  private final BeanFactory beans;

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @Autowired
  public WebSecurityConfig(JwtTokenProvider jwtTokenProvider, BeanFactory beans,
                           TokenService tokenService) {
    super();
    this.jwtTokenProvider = jwtTokenProvider;
    this.beans = beans;
    this.tokenService = tokenService;
  }

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  //------------------------ Implements:

  //------------------------ Overrides:

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable()
        .sessionManagement()
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .exceptionHandling()
        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
        .and()
        .authorizeRequests()
        .antMatchers("/register").permitAll()
        .antMatchers("/login").permitAll()
        .antMatchers("/ws").permitAll() // TODO
        .antMatchers("/actuator/**").permitAll() // spring-boot actuator endpoints - exposed on a private port
        .anyRequest().authenticated()
        .and()
        .addFilter(new CorsFilter(corsConfigurationSource()))
        .apply(new JwtConfigurer(jwtTokenProvider, tokenService));
  }

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  private CorsConfigurationSource corsConfigurationSource() {
    final DynamicCORSConfiguration configuration = beans.getBean(DynamicCORSConfiguration.class);
    return (request) -> configuration;
  }

  //---------------------------- Getters/Setters ------------------------------

}
