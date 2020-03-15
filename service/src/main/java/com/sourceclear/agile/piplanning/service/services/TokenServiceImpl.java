package com.sourceclear.agile.piplanning.service.services;

import com.sourceclear.agile.piplanning.service.entities.login.InvalidatedToken;
import com.sourceclear.agile.piplanning.service.entities.login.User;
import com.sourceclear.agile.piplanning.service.repositories.InvalidatedTokenRepository;
import com.sourceclear.agile.piplanning.service.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TokenServiceImpl implements TokenService {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private static final Logger LOGGER = LoggerFactory.getLogger(TokenServiceImpl.class);

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private final InvalidatedTokenRepository invalidatedTokenRepository;

  private final UserRepository userRepository;

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @Autowired
  public TokenServiceImpl(InvalidatedTokenRepository invalidatedTokenRepository,
                          UserRepository userRepository) {
    this.invalidatedTokenRepository = invalidatedTokenRepository;
    this.userRepository = userRepository;
  }

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //------------------------ Implements:

  @Override
  public void invalidateTokenForUser(String token, User user) {
    user = userRepository.getOne(user.getId());
    final Optional<InvalidatedToken> invalidatedToken = invalidatedTokenRepository.findByValueEquals(token);
    if (invalidatedToken.isPresent()) {
      LOGGER.info("Token value already present in invalidated tokens table with id {}. Not storing again.", invalidatedToken.get().getId());
      return;
    }

    final InvalidatedToken newInvalidatedToken = new InvalidatedToken();
    newInvalidatedToken.setUser(user);
    newInvalidatedToken.setValue(token);
    invalidatedTokenRepository.save(newInvalidatedToken);
  }

  @Override
  public Optional<InvalidatedToken> findInvalidatedToken(String value) {
    return invalidatedTokenRepository.findByValueEquals(value);
  }

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  //---------------------------- Getters/Setters ------------------------------

}
