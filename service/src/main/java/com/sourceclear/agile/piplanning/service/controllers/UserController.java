package com.sourceclear.agile.piplanning.service.controllers;

import com.sourceclear.agile.piplanning.service.components.JwtTokenProvider;
import com.sourceclear.agile.piplanning.service.entities.login.User;
import com.sourceclear.agile.piplanning.service.objects.AuthRequest;
import com.sourceclear.agile.piplanning.service.objects.BasicUserInfo;
import com.sourceclear.agile.piplanning.service.objects.UserRegistration;
import com.sourceclear.agile.piplanning.service.services.TokenService;
import com.sourceclear.agile.piplanning.service.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class UserController {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private final UserService userService;

  private final JwtTokenProvider jwtTokenProvider;

  private final AuthenticationManager authenticationManager;

  private final TokenService tokenService;

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @Autowired
  public UserController(UserService userService,
                        JwtTokenProvider jwtTokenProvider,
                        AuthenticationManager authenticationManager,
                        TokenService tokenService) {
    this.userService = userService;
    this.jwtTokenProvider = jwtTokenProvider;
    this.authenticationManager = authenticationManager;
    this.tokenService = tokenService;
  }

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @PostMapping("/register")
  public ResponseEntity<String> register(@RequestBody UserRegistration userRegistration) {
    final User user = userService.register(userRegistration);
    return tokenResponse(user.getEmail());
  }

  @PostMapping("/login")
  public ResponseEntity<String> getToken(@RequestBody AuthRequest authRequest) {
    try {
      final String email = authRequest.email();
      authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, authRequest.password()));
      final User user = userService.getUserByEmail(email);
      return tokenResponse(user.getEmail());

    } catch (AuthenticationException ex) {
      throw new BadCredentialsException("Incorrect username/password supplied", ex);
    }
  }

  @GetMapping("/me")
  public BasicUserInfo currentUser(@AuthenticationPrincipal User user) {
    return user.basicUserInfo();
  }

  @PostMapping("/token/invalidate")
  public void invalidateToken(@AuthenticationPrincipal User user,
                              @RequestHeader(JwtTokenProvider.AUTHORIZATION_HEADER) String authorization) {
    final Optional<String> token = jwtTokenProvider.resolveToken(authorization);
    token.ifPresent(t -> {
      // we don't need to validate the token because the fact that this method was entered means the token has been
      // validated based on how we set up WebSecurityConfig and JwtTokenFilter.
      tokenService.invalidateTokenForUser(t, user);
    });
  }

  //------------------------ Implements:

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  /**
   * Returns a 200 response with the generated JWT in the header.
   */
  private ResponseEntity<String> tokenResponse(String email) {
    return ResponseEntity.ok()
        .header("access_token", jwtTokenProvider.createToken(email))
        .header("Access-Control-Expose-Headers", "access_token")
        .build();
  }

  //---------------------------- Getters/Setters ------------------------------

}
