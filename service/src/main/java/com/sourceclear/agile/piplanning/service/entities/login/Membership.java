package com.sourceclear.agile.piplanning.service.entities.login;

import com.sourceclear.agile.piplanning.service.entities.UpdatableTimestampedEntity;
import com.sourceclear.agile.piplanning.service.objects.MembershipRole;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "memberships")
public class Membership extends UpdatableTimestampedEntity {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private static final long serialVersionUID = -4912294122171028982L;

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id", nullable = false)
  private Account account;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private MembershipRole role;

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //------------------------ Implements:

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  //---------------------------- Getters/Setters ------------------------------

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Account getAccount() {
    return account;
  }

  public void setAccount(Account account) {
    this.account = account;
  }

  public MembershipRole getRole() {
    return role;
  }

  public void setRole(MembershipRole role) {
    this.role = role;
  }
}
