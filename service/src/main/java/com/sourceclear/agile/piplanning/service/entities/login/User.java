package com.sourceclear.agile.piplanning.service.entities.login;

import com.sourceclear.agile.piplanning.service.entities.Board;
import com.sourceclear.agile.piplanning.service.entities.UpdatableTimestampedEntity;
import com.sourceclear.agile.piplanning.service.objects.BasicUserInfo;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Entity
@Table(name = "users")
public class User extends UpdatableTimestampedEntity implements UserDetails {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  private static final long serialVersionUID = 2115561962819297601L;

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  public static String normalizeEmail(String email) {
    if (email == null) {
      return email;
    }
    return email.toLowerCase().trim();
  }

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @Column(name = "email", unique = true, nullable = false)
  private String email;

  @Column(name = "first_name", nullable = false)
  private String firstName;

  @Column(name = "last_name")
  private String lastName;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Membership> memberships = new ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<InvalidatedToken> invalidatedTokens = new ArrayList<>();

  @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Board> boards = new HashSet<>();

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @PreUpdate
  @PrePersist
  private void validate() {
    email = normalizeEmail(email);
  }

  public BasicUserInfo basicUserInfo() {
    return new BasicUserInfo.Builder()
        .email(email)
        .firstName(firstName)
        .nullableLastName(lastName)
        .id(getId())
        .build();
  }

  //------------------------ Implements: org.springframework.security.core.userdetails.UserDetails

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of();
  }

  @Override
  public String getPassword() {
    return passwordHash;
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  //---------------------------- Getters/Setters ------------------------------

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public Optional<String> getLastName() {
    return Optional.ofNullable(lastName);
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
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

  public List<InvalidatedToken> getInvalidatedTokens() {
    return invalidatedTokens;
  }

  public Set<Board> getBoards() {
    return boards;
  }

  public void setBoards(Set<Board> boards) {
    this.boards = boards;
  }

  public void setInvalidatedTokens(List<InvalidatedToken> invalidatedTokens) {
    this.invalidatedTokens.clear();
    if (isNotEmpty(invalidatedTokens)) {
      this.invalidatedTokens.addAll(invalidatedTokens);
    }
  }
}
