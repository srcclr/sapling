package com.sourceclear.agile.piplanning.service.configs;

import com.sourceclear.agile.piplanning.service.components.JwtTokenProvider;
import com.sourceclear.agile.piplanning.service.filters.JwtTokenFilter;
import com.sourceclear.agile.piplanning.service.services.TokenService;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class JwtConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private final JwtTokenProvider jwtTokenProvider;

  private final TokenService tokenService;

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  public JwtConfigurer(JwtTokenProvider jwtTokenProvider,
                       TokenService tokenService) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.tokenService = tokenService;
  }

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //------------------------ Implements:

  //------------------------ Overrides:

  @Override
  public void configure(HttpSecurity http) {
    final JwtTokenFilter customFilter = new JwtTokenFilter(jwtTokenProvider, tokenService);
    http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
  }

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  //---------------------------- Getters/Setters ------------------------------

}
