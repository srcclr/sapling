package com.sourceclear.agile.piplanning.service.repositories;

import com.sourceclear.agile.piplanning.service.entities.login.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
