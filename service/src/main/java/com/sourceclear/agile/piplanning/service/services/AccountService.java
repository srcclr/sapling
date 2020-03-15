package com.sourceclear.agile.piplanning.service.services;

import com.sourceclear.agile.piplanning.service.entities.login.Account;
import com.sourceclear.agile.piplanning.service.objects.AccountType;
import org.springframework.transaction.annotation.Transactional;

public interface AccountService {
  @Transactional
  Account createAccount(String name, AccountType accountType);
}
