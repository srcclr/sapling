package com.sourceclear.agile.piplanning.service.repositories;

import com.sourceclear.agile.piplanning.service.entities.Epic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EpicRepository extends JpaRepository<Epic, Long> {
}
