package com.sourceclear.agile.piplanning.service.services;

import com.sourceclear.agile.piplanning.service.entities.login.InvalidatedToken;
import com.sourceclear.agile.piplanning.service.entities.login.User;
import com.sourceclear.agile.piplanning.service.repositories.InvalidatedTokenRepository;
import com.sourceclear.agile.piplanning.service.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

@TestPropertySource(locations = "classpath:/tests.properties")
@DataJpaTest
@EnableJpaAuditing
public class TokenServiceTest extends AbstractTransactionalTestNGSpringContextTests {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @Autowired
  private InvalidatedTokenRepository invalidatedTokenRepository;

  @Autowired
  private UserRepository userRepository;

  private TokenService tokenService;

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @BeforeClass
  public void setup() {
    this.tokenService = new TokenServiceImpl(invalidatedTokenRepository, userRepository);
  }

  @Test
  public void testInvalidateTokenForUser() {
    final User user = new User();
    user.setEmail("h@h.com");
    user.setFirstName("h");
    user.setPasswordHash("qwe");
    userRepository.save(user);

    final String token = "foo";
    tokenService.invalidateTokenForUser(token, user);
    assertEquals(1, invalidatedTokenRepository.findAll().size());
    // invalidate the same token should have no effect
    tokenService.invalidateTokenForUser(token, user);
    assertEquals(1, invalidatedTokenRepository.findAll().size());
  }

  @Test
  public void testFindInvalidatedToken() {
    final String token = "foo";

    assertTrue(tokenService.findInvalidatedToken(token).isEmpty());

    final User user = new User();
    user.setEmail("h@h.com");
    user.setFirstName("h");
    user.setPasswordHash("qwe");
    userRepository.save(user);
    final InvalidatedToken invalidatedToken = new InvalidatedToken();
    invalidatedToken.setUser(user);
    invalidatedToken.setValue(token);
    invalidatedTokenRepository.save(invalidatedToken);

    assertTrue(tokenService.findInvalidatedToken(token).isPresent());
    assertTrue(tokenService.findInvalidatedToken("idontexist").isEmpty());
  }

  //------------------------ Implements:

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  //---------------------------- Getters/Setters ------------------------------

}
