package com.sourceclear.agile.piplanning.service.filters;

import com.sourceclear.agile.piplanning.service.components.JwtTokenProvider;
import com.sourceclear.agile.piplanning.service.services.TokenService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class JwtTokenFilter extends GenericFilterBean {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private final JwtTokenProvider jwtTokenProvider;

  private final TokenService tokenService;

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  public JwtTokenFilter(JwtTokenProvider jwtTokenProvider,
                        TokenService tokenService) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.tokenService = tokenService;
  }

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //------------------------ Implements:

  //------------------------ Overrides:

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain) throws IOException, ServletException {
    try {
      final Optional<String> tokenOpt = jwtTokenProvider.resolveToken((HttpServletRequest) req);
      tokenOpt.ifPresent(token -> {
        // if findInvalidatedToken() != empty, that means this token has been invalidated and we should not let it pass.
        if (jwtTokenProvider.validateToken(token) && tokenService.findInvalidatedToken(token).isEmpty()) {
          final Authentication auth = jwtTokenProvider.getAuthentication(token);
          SecurityContextHolder.getContext().setAuthentication(auth);
        }
      });
    } catch (AuthenticationException ex) {
      final HttpServletResponse httpServletResponse = (HttpServletResponse) res;
      httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      httpServletResponse.setContentType("text/plain");
      httpServletResponse.getWriter().write(ex.getMessage());
      return;
    }

    filterChain.doFilter(req, res);
  }

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  //---------------------------- Getters/Setters ------------------------------

}
