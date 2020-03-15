package com.sourceclear.agile.piplanning.service.repositories;

import com.sourceclear.agile.piplanning.service.entities.login.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import javax.persistence.QueryHint;
import java.util.Optional;

/**
 * See https://in.relation.to/2016/08/04/introducing-distinct-pass-through-query-hint/ for why DISTINCT keyword
 * is needed, the issues with it, and how QueryHint fixes the issues.
 */
public interface UserRepository extends JpaRepository<User, Long> {
  @QueryHints(value = {@QueryHint(name = org.hibernate.annotations.QueryHints.PASS_DISTINCT_THROUGH, value = "false")})
  @Query("SELECT DISTINCT user FROM User user LEFT JOIN FETCH user.memberships ms LEFT JOIN FETCH ms.account WHERE user.email = ?1")
  Optional<User> findByEmailJoinMembershipsAndAccount(String email);

  @QueryHints(value = {@QueryHint(name = org.hibernate.annotations.QueryHints.PASS_DISTINCT_THROUGH, value = "false")})
  @Query("SELECT DISTINCT user FROM User user LEFT JOIN FETCH user.memberships ms LEFT JOIN FETCH ms.account WHERE user.id = ?1")
  Optional<User> findByIdJoinMembershipsAndAccount(long id);
}
