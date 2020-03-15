package com.sourceclear.agile.piplanning.service.repositories;

import com.sourceclear.agile.piplanning.service.entities.login.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
}
