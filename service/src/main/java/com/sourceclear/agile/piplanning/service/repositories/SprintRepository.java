package com.sourceclear.agile.piplanning.service.repositories;

import com.sourceclear.agile.piplanning.service.entities.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface SprintRepository extends JpaRepository<Sprint, Long> {
  Set<Sprint> findAllByBoardId(long boardId);
}
