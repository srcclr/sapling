package com.sourceclear.agile.piplanning.service.components;

import com.sourceclear.agile.piplanning.service.exceptions.JwtAuthenticationException;
import com.sourceclear.agile.piplanning.service.services.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtTokenProvider {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  public static final String AUTHORIZATION_HEADER = "Authorization";

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  // private key for signing, public key for verifying
  private final KeyPair jwtSigningKeyPair;

  private final UserDetailsService userDetailsService;

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @Autowired
  public JwtTokenProvider(AppProperties appProperties,
                          UserDetailsServiceImpl userDetailsService) {
    try {
      this.jwtSigningKeyPair = new KeyPair(
          getPublic(appProperties.getJwtPublicKey()),
          getPrivate(appProperties.getJwtPrivateKey())
      );
    } catch (Exception ex) {
      throw new RuntimeException("Unable to generate JWT signing key pair: " + ex.getMessage(), ex);
    }

    this.userDetailsService = userDetailsService;
  }


  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  public String createToken(String email) {
    final Claims claims = Jwts.claims().setSubject(email);
    // When we start setting expiry dates, we should also update the process of invalidating tokens
    // to ignore those that have expired. This will make the process of checking whether a token is invalidated
    // faster since we will be storing less tokens in the invalidated table.
    return Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(new Date())
        .signWith(SignatureAlgorithm.RS256, jwtSigningKeyPair.getPrivate())
        .compact();
  }

  public Authentication getAuthentication(String token) {
    final UserDetails userDetails = userDetailsService.loadUserByUsername(getEmail(token));
    return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
  }

  public Optional<String> resolveToken(HttpServletRequest req) {
    final String bearerToken = req.getHeader(AUTHORIZATION_HEADER);
    return resolveToken(bearerToken);
  }

  public Optional<String> resolveToken(String authorizationHeaderValue) {
    if (authorizationHeaderValue != null && authorizationHeaderValue.startsWith("Bearer ")) {
      return Optional.of(authorizationHeaderValue.substring(7));
    }
    return Optional.empty();
  }

  public boolean validateToken(String token) {
    // if no exception is thrown by this method, that means token is valid.
    parseClaims(token);
    return true;
  }

  //------------------------ Implements:

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  private Jws<Claims> parseClaims(String token) throws JwtAuthenticationException {
    try {
      // this is for verifying JWTs; hence setSigningKey() is called with the public key.
      // Jwts.parser() is not set as a static variable because I'm worried it is not thread-safe and it holds internal states.
      return Jwts.parser().setSigningKey(jwtSigningKeyPair.getPublic()).parseClaimsJws(token);
    } catch (JwtException | IllegalArgumentException e) {
      throw new JwtAuthenticationException("Expired/invalid JWT token");
    }
  }

  private String getEmail(String token) {
    return parseClaims(token).getBody().getSubject();
  }

  private static PrivateKey getPrivate(String value) throws Exception {
    value = value.replace("-----BEGIN PRIVATE KEY-----", "");
    value = value.replace("-----END PRIVATE KEY-----", "");
    value = value.replaceAll("\\s+", "");
    final byte[] keyBytes = Base64.getDecoder().decode(value);
    final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
    final KeyFactory kf = KeyFactory.getInstance("RSA");
    return kf.generatePrivate(spec);
  }

  private static PublicKey getPublic(String value) throws Exception {
    value = value.replace("-----BEGIN PUBLIC KEY-----", "");
    value = value.replace("-----END PUBLIC KEY-----", "");
    value = value.replaceAll("\\s+", "");
    final byte[] keyBytes = Base64.getDecoder().decode(value);
    final X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
    final KeyFactory kf = KeyFactory.getInstance("RSA");
    return kf.generatePublic(spec);
  }

  //---------------------------- Getters/Setters ------------------------------

}
