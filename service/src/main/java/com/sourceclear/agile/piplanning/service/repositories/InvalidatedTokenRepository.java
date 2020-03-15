package com.sourceclear.agile.piplanning.service.repositories;

import com.sourceclear.agile.piplanning.service.entities.login.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, Long> {
  Optional<InvalidatedToken> findByValueEquals(String value);
}
