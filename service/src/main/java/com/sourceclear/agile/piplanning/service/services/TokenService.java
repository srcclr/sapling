package com.sourceclear.agile.piplanning.service.services;

import com.sourceclear.agile.piplanning.service.entities.login.InvalidatedToken;
import com.sourceclear.agile.piplanning.service.entities.login.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface TokenService {
  @Transactional
  void invalidateTokenForUser(String token, User user);

  @Transactional(readOnly = true)
  Optional<InvalidatedToken> findInvalidatedToken(String value);
}
