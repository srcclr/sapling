package com.sourceclear.agile.piplanning.service.repositories;

import com.sourceclear.agile.piplanning.service.entities.Board;
import com.sourceclear.agile.piplanning.service.entities.Solution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

public interface SolutionRepository extends JpaRepository<Solution, Long> {
  @Query("SELECT s " +
      "FROM Solution s " +
      "JOIN FETCH s.board b " +
      "LEFT JOIN FETCH s.sprint " +
      "JOIN FETCH s.ticket t " +
      "LEFT JOIN FETCH t.origin " +
      "LEFT JOIN FETCH t.board " +
      "LEFT JOIN FETCH b.owner " +
      "WHERE s.board.id = ?1 and s.preview = false")
  Set<Solution> findCurrentSolution(long id);

  @Query("SELECT s " +
      "FROM Solution s " +
      "JOIN FETCH s.board b " +
      "LEFT JOIN FETCH s.sprint " +
      "JOIN FETCH s.ticket t " +
      "LEFT JOIN FETCH t.origin " +
      "LEFT JOIN FETCH t.board " +
      "LEFT JOIN FETCH b.owner " +
      "WHERE s.board.id = ?1 and s.preview = true")
  Set<Solution> findCurrentPreview(long id);

  /**
   * Hibernate reorders SQL statements without regard for commutativity, because of course it does.
   * To successfully delete, then insert, we can either call entityManager.flush() or use a manual query
   * like this (which Hibernate has to flush). This solution has the bonus of avoiding an unnecessary select.
   *
   * https://stackoverflow.com/a/34154539
   * https://stackoverflow.com/a/31350749
   */
  @Modifying
  @Transactional
  @Query("delete from Solution s where s.board = ?1 and s.preview = false")
  void deleteRealSolution(Board board);

  @Modifying
  @Transactional
  @Query("delete from Solution s where s.board = ?1 and s.preview = true")
  void deletePreviewSolution(Board board);

  @Modifying
  @Transactional
  @Query("update Solution s set s.preview = false where s.board = ?1")
  void acceptPreviewSolution(Board board);
}
