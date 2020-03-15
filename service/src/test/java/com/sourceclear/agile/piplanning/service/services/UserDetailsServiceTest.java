package com.sourceclear.agile.piplanning.service.services;

import com.sourceclear.agile.piplanning.service.entities.login.User;
import com.sourceclear.agile.piplanning.service.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

@TestPropertySource(locations = "classpath:/tests.properties")
@DataJpaTest
@EnableJpaAuditing
public class UserDetailsServiceTest extends AbstractTransactionalTestNGSpringContextTests {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @Autowired
  private UserRepository userRepository;

  private UserDetailsServiceImpl userDetailsService;

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @BeforeClass
  public void setup() {
    this.userDetailsService = new UserDetailsServiceImpl(userRepository);
  }

  @Test
  public void testLoadByUsername() {
    User user = new User();
    user.setEmail("email@email.com");
    user.setFirstName("fname");
    user.setPasswordHash("password");
    user = userRepository.save(user);

    final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
    assertEquals("email@email.com", userDetails.getUsername());
    assertEquals("password", userDetails.getPassword());
    assertEquals(List.of(), userDetails.getAuthorities());
    assertTrue(userDetails.isAccountNonExpired());
    assertTrue(userDetails.isAccountNonLocked());
    assertTrue(userDetails.isCredentialsNonExpired());
    assertTrue(userDetails.isEnabled());
  }

  //------------------------ Implements:

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  //---------------------------- Getters/Setters ------------------------------

}
