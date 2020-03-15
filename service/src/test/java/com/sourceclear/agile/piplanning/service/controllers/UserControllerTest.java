package com.sourceclear.agile.piplanning.service.controllers;

import com.sourceclear.agile.piplanning.service.components.JwtTokenProvider;
import com.sourceclear.agile.piplanning.service.entities.login.Membership;
import com.sourceclear.agile.piplanning.service.entities.login.User;
import com.sourceclear.agile.piplanning.service.exceptions.EmailExistsException;
import com.sourceclear.agile.piplanning.service.objects.AuthRequest;
import com.sourceclear.agile.piplanning.service.objects.BasicUserInfo;
import com.sourceclear.agile.piplanning.service.objects.MembershipRole;
import com.sourceclear.agile.piplanning.service.objects.UserRegistration;
import com.sourceclear.agile.piplanning.service.repositories.AccountRepository;
import com.sourceclear.agile.piplanning.service.repositories.MembershipRepository;
import com.sourceclear.agile.piplanning.service.repositories.UserRepository;
import com.sourceclear.agile.piplanning.service.services.AccountService;
import com.sourceclear.agile.piplanning.service.services.AccountServiceImpl;
import com.sourceclear.agile.piplanning.service.services.TokenService;
import com.sourceclear.agile.piplanning.service.services.UserService;
import com.sourceclear.agile.piplanning.service.services.UserServiceImpl;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

@TestPropertySource(locations = "classpath:/tests.properties")
@DataJpaTest
@EnableJpaAuditing
public class UserControllerTest extends AbstractTransactionalTestNGSpringContextTests {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private AccountRepository accountRepository;

  @Autowired
  private MembershipRepository membershipRepository;

  private JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);

  private PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

  private AuthenticationManager authenticationManager = mock(AuthenticationManager.class);

  private TokenService tokenService = mock(TokenService.class);

  private AccountService accountService;

  private UserService userService;

  private UserController userController;

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @BeforeClass
  public void setup() {
    this.accountService = new AccountServiceImpl(accountRepository);
    this.userService = new UserServiceImpl(userRepository, passwordEncoder, accountService);
    this.userController = new UserController(userService, jwtTokenProvider, authenticationManager, tokenService);
  }

  @Test
  public void testRegister() {
    assertEquals(0, membershipRepository.findAll().size());
    assertEquals(0, accountRepository.findAll().size());

    final UserRegistration registration = new UserRegistration.Builder()
        .email("h@123.com")
        .name("Tony")
        .password("password")
        .build();

    doReturn("tokenvalue").when(jwtTokenProvider).createToken(eq(registration.email()));
    doReturn("encodedpw").when(passwordEncoder).encode(any(String.class));

    final ResponseEntity<String> response = userController.register(registration);
    final ResponseEntity<String> expected = ResponseEntity.ok()
        .header("access_token", "tokenvalue")
        .header("Access-Control-Expose-Headers", "access_token")
        .build();

    assertEquals(expected, response);
    assertTrue(userRepository.findByEmailJoinMembershipsAndAccount(registration.email()).isPresent());
    final List<Membership> memberships = membershipRepository.findAll();
    assertEquals(1, memberships.size());
    assertEquals(1, accountRepository.findAll().size());
    AssertJUnit.assertEquals(MembershipRole.ADMIN, memberships.get(0).getRole());

    // re-register with the same email. this should fail
    boolean emailExists = false;
    try {
      userController.register(registration);
    } catch (EmailExistsException ex) {
      emailExists = true;
    }
    assertTrue(emailExists);
  }

  @Test
  public void testCurrentUser() throws Exception {
    final User user = new User();
    user.setEmail("h@email");
    user.setPasswordHash("foo");
    user.setFirstName("MyName");
    user.setLastName(null);
    FieldUtils.writeField(user, "id", 1L, true);
    final BasicUserInfo expected = new BasicUserInfo.Builder()
        .email("h@email")
        .firstName("MyName")
        .nullableLastName(null)
        .id(1L)
        .build();
    final BasicUserInfo actual = userController.currentUser(user);
    assertEquals(expected, actual);
  }

  @Test
  public void testGetToken() {
    final AuthRequest authRequest = new AuthRequest.Builder()
        .email("h@email.com")
        .password("foo")
        .build();

    final User user = new User();
    user.setEmail("h@email.com");
    user.setFirstName("h");
    user.setPasswordHash("pwhash");
    userRepository.save(user);

    doReturn(new UsernamePasswordAuthenticationToken(authRequest.email(), authRequest.password())).when(authenticationManager).authenticate(any(Authentication.class));
    doReturn("tokenvalue").when(jwtTokenProvider).createToken(eq(authRequest.email()));

    final ResponseEntity<String> response = userController.getToken(authRequest);
    final ResponseEntity<String> expected = ResponseEntity.ok()
        .header("access_token", "tokenvalue")
        .header("Access-Control-Expose-Headers", "access_token")
        .build();

    assertEquals(expected, response);
  }

  private class AuthException extends AuthenticationException {
    private static final long serialVersionUID = -6792064971326171468L;

    private AuthException(String msg, Throwable t) {
      super(msg, t);
    }

    private AuthException(String msg) {
      super(msg);
    }
  }

  @Test(expectedExceptions = BadCredentialsException.class)
  public void testGetTokenFail() {
    final AuthRequest authRequest = new AuthRequest.Builder()
        .email("h@email.com")
        .password("foo")
        .build();

    doThrow(new AuthException("test")).when(authenticationManager).authenticate(any(Authentication.class));
    userController.getToken(authRequest);
  }

  @Test
  public void testInvalidateToken() {
    final User user = new User();
    user.setEmail("h@email.com");
    user.setFirstName("h");
    user.setPasswordHash("pwhash");

    final String auth = "auth";

    doReturn(Optional.of(auth)).when(jwtTokenProvider).resolveToken(eq(auth));
    doNothing().when(tokenService).invalidateTokenForUser(eq(auth), eq(user));

    userController.invalidateToken(user, auth);
  }

  //------------------------ Implements:

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  //---------------------------- Getters/Setters ------------------------------

}
