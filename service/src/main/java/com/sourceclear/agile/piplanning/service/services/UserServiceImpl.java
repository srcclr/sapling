package com.sourceclear.agile.piplanning.service.services;

import com.sourceclear.agile.piplanning.service.entities.login.Account;
import com.sourceclear.agile.piplanning.service.entities.login.Membership;
import com.sourceclear.agile.piplanning.service.entities.login.User;
import com.sourceclear.agile.piplanning.service.exceptions.EmailExistsException;
import com.sourceclear.agile.piplanning.service.exceptions.UnauthorizedException;
import com.sourceclear.agile.piplanning.service.objects.AccountType;
import com.sourceclear.agile.piplanning.service.objects.MembershipRole;
import com.sourceclear.agile.piplanning.service.objects.UserRegistration;
import com.sourceclear.agile.piplanning.service.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.sourceclear.agile.piplanning.service.entities.login.User.normalizeEmail;
import static java.lang.String.format;

@Service
public class UserServiceImpl implements UserService {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private final UserRepository userRepository;

  private final PasswordEncoder passwordEncoder;

  private final AccountService accountService;

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @Autowired
  public UserServiceImpl(UserRepository userRepository,
                         PasswordEncoder passwordEncoder,
                         AccountService accountService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.accountService = accountService;
  }

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //------------------------ Implements:

  @Override
  public User register(UserRegistration userRegistration) {
    final String email = normalizeEmail(userRegistration.email());
    if (emailExists(email)) {
      throw new EmailExistsException(format("An account with the email '%s' exists", email));
    }

    final User user = new User();
    user.setEmail(email);
    user.setFirstName(userRegistration.name());
    user.setPasswordHash(passwordEncoder.encode(userRegistration.password()));

    // By default, upon registration, each user has a personal account.
    // Set the name for the user which he/she can update later on.
    final Account account = accountService.createAccount(userRegistration.name(), AccountType.BASIC);

    final Membership membership = new Membership();
    membership.setUser(user);
    membership.setAccount(account);
    // you are the admin of the account you registered for.
    membership.setRole(MembershipRole.ADMIN);

    user.getMemberships().add(membership);
    account.getMemberships().add(membership);

    return userRepository.save(user);
  }

  @Override
  public User getUserByEmail(String email) throws UsernameNotFoundException {
    return userRepository.findByEmailJoinMembershipsAndAccount(email)
        .orElseThrow(() -> new UsernameNotFoundException(format("User '%s' not found", email)));
  }

  @Override
  public void checkUserInAccount(User user, long accountId) {
    user = userRepository.findByIdJoinMembershipsAndAccount(user.getId())
        .orElseThrow(() -> new UsernameNotFoundException("Request user does not exist"));

    final boolean isAccountMember = user.getMemberships().stream()
        .map(Membership::getAccount)
        .anyMatch(account -> account.getId().equals(accountId));

    if (!isAccountMember) {
      throw new UnauthorizedException("User is not a member of account");
    }
  }

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  private boolean emailExists(String email) {
    return userRepository.findByEmailJoinMembershipsAndAccount(email).isPresent();
  }

  //---------------------------- Getters/Setters ------------------------------

}
