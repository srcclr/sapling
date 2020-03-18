/*
 * © Copyright 2019 -  SourceClear Inc
 */
package com.sourceclear.agile.piplanning.service.entities;

import com.sourceclear.agile.piplanning.objects.DependencyP;
import com.sourceclear.agile.piplanning.objects.EpicP;
import com.sourceclear.agile.piplanning.objects.PinP;
import com.sourceclear.agile.piplanning.objects.Problem;
import com.sourceclear.agile.piplanning.objects.SprintP;
import com.sourceclear.agile.piplanning.objects.TicketP;
import com.sourceclear.agile.piplanning.service.entities.login.User;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "boards")
public class Board extends BaseEntity {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @OneToMany(mappedBy = "board", cascade = {CascadeType.ALL}, orphanRemoval = true)
  private Set<Sprint> sprints = new HashSet<>();

  @OneToMany(mappedBy = "board", cascade = {CascadeType.ALL}, orphanRemoval = true)
  private Set<Ticket> tickets = new HashSet<>();

  @OneToMany(mappedBy = "origin", cascade = {CascadeType.ALL}, orphanRemoval = true)
  private Set<Ticket> ownedTickets = new HashSet<>();

  @OneToMany(mappedBy = "board", cascade = {CascadeType.ALL}, orphanRemoval = true)
  private Set<Epic> epics = new HashSet<>();

  @Column(name = "name")
  private String name;

  @ElementCollection
  @CollectionTable(name = "ticket_pins", joinColumns = @JoinColumn(name = "board_id"))
  private Set<Pin> pins = new HashSet<>();

  @ElementCollection
  @CollectionTable(name = "ticket_deps", joinColumns = @JoinColumn(name = "board_id"))
  private Set<Dependency> deps = new HashSet<>();

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "owner")
  private User owner;

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  public Board() {
  }

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  public Problem toProblem() {
    var epics = getEpics().stream().map(e -> new EpicP(e.getId(),
        e.getTickets().stream().map(t -> new TicketP(t.getId(), t.getWeight())).collect(Collectors.toSet()),
        e.getPriority())).collect(Collectors.toSet());

    var tickets = getTickets().stream().map(t -> new TicketP(t.getId(), t.getWeight())).collect(Collectors.toSet());
    var sprints = getSprints().stream().map(s -> new SprintP(s.getId(), s.getCapacity())).collect(Collectors.toSet());
    var pins = getPins().stream().map(p -> new PinP(p.getTicketId(), p.getSprintId())).collect(Collectors.toSet());
    var deps = getDeps().stream().map(d -> new DependencyP(d.getFromTicketId(), d.getToTicketId())).collect(Collectors.toSet());

    return new Problem(epics, tickets, sprints, pins, deps);
  }

  //------------------------ Implements:

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  //---------------------------- Property Methods -----------------------------

  public Set<Sprint> getSprints() {
    return sprints;
  }

  public void setSprints(Set<Sprint> sprints) {
    this.sprints = sprints;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Set<Ticket> getOwnedTickets() {
    return ownedTickets;
  }

  public User getOwner() {
    return owner;
  }

  public void setOwner(User owner) {
    this.owner = owner;
  }

  public void setOwnedTickets(Set<Ticket> ownedTickets) {
    this.ownedTickets = ownedTickets;
  }

  public Set<Pin> getPins() {
    return pins;
  }

  public void setPins(Set<Pin> pins) {
    this.pins = pins;
  }

  public Set<Dependency> getDeps() {
    return deps;
  }

  public void setDeps(Set<Dependency> deps) {
    this.deps = deps;
  }

  public Set<Epic> getEpics() {
    return epics;
  }

  public void setEpics(Set<Epic> epics) {
    this.epics = epics;
  }

  public Set<Ticket> getTickets() {
    return tickets;
  }

  public void setTickets(Set<Ticket> tickets) {
    this.tickets = tickets;
  }
}

