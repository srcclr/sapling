package com.sourceclear.agile.piplanning.service.services;

import com.sourceclear.agile.piplanning.service.entities.login.User;
import com.sourceclear.agile.piplanning.service.objects.UserRegistration;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

public interface UserService {
  @Transactional
  User register(UserRegistration userRegistration);

  @Transactional(readOnly = true)
  User getUserByEmail(String email) throws UsernameNotFoundException;

  @Transactional(readOnly = true)
  void checkUserInAccount(User user, long accountId);
}
