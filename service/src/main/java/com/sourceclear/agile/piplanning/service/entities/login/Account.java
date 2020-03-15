package com.sourceclear.agile.piplanning.service.entities.login;

import com.sourceclear.agile.piplanning.service.entities.UpdatableTimestampedEntity;
import com.sourceclear.agile.piplanning.service.objects.AccountType;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Entity
@Table(name = "accounts")
public class Account extends UpdatableTimestampedEntity {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private static final long serialVersionUID = -7116313455751498157L;

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @Column(name = "name", nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private AccountType accountType;

  @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Membership> memberships = new ArrayList<>();

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //------------------------ Implements:

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  //---------------------------- Getters/Setters ------------------------------


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public AccountType getAccountType() {
    return accountType;
  }

  public void setAccountType(AccountType accountType) {
    this.accountType = accountType;
  }

  public List<Membership> getMemberships() {
    return memberships;
  }

  public void setMemberships(List<Membership> memberships) {
    this.memberships.clear();
    if (isNotEmpty(memberships)) {
      this.memberships.addAll(memberships);
    }
  }
}
