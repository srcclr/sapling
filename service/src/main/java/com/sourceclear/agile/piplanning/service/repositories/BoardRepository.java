package com.sourceclear.agile.piplanning.service.repositories;

import com.sourceclear.agile.piplanning.service.entities.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

import javax.persistence.QueryHint;
import java.util.Optional;
import java.util.Set;

public interface BoardRepository extends JpaRepository<Board, Long> {
  @QueryHints(value = {@QueryHint(name = org.hibernate.annotations.QueryHints.PASS_DISTINCT_THROUGH, value = "false")})
  @Query("SELECT DISTINCT b " +
      "FROM Board b " +
      "LEFT JOIN FETCH b.sprints " +
      "LEFT JOIN FETCH b.deps " +
      "LEFT JOIN FETCH b.pins " +
      "LEFT JOIN FETCH b.epics e " +
      "LEFT JOIN FETCH e.tickets " +
      "LEFT JOIN FETCH b.tickets " +
      "JOIN FETCH b.owner " +
      "WHERE b.id = ?1")
  Optional<Board> findToSolve(long id);

  @QueryHints(value = {@QueryHint(name = org.hibernate.annotations.QueryHints.PASS_DISTINCT_THROUGH, value = "false")})
  @Query("SELECT DISTINCT b " +
      "FROM Board b " +
      "JOIN FETCH b.owner")
  Set<Board> findAllWithOwner();

  @QueryHints(value = {@QueryHint(name = org.hibernate.annotations.QueryHints.PASS_DISTINCT_THROUGH, value = "false")})
  @Query("SELECT DISTINCT b " +
      "FROM Board b " +
      "LEFT JOIN FETCH b.deps where b.id = ?1")
  Optional<Board> findWithDeps(long id);

  @QueryHints(value = {@QueryHint(name = org.hibernate.annotations.QueryHints.PASS_DISTINCT_THROUGH, value = "false")})
  @Query("SELECT DISTINCT b " +
      "FROM Board b " +
      "LEFT JOIN FETCH b.pins where b.id = ?1")
  Optional<Board> findWithPins(long id);
}
