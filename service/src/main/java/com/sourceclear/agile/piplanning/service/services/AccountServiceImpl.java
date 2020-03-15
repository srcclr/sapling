package com.sourceclear.agile.piplanning.service.services;

import com.sourceclear.agile.piplanning.service.entities.login.Account;
import com.sourceclear.agile.piplanning.service.objects.AccountType;
import com.sourceclear.agile.piplanning.service.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl implements AccountService {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private final AccountRepository accountRepository;

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @Autowired
  public AccountServiceImpl(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //------------------------ Implements:

  @Override
  public Account createAccount(String name, AccountType accountType) {
    final Account account = new Account();
    account.setName(name);
    account.setAccountType(accountType);
    return accountRepository.save(account);
  }


  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  //---------------------------- Getters/Setters ------------------------------

}
