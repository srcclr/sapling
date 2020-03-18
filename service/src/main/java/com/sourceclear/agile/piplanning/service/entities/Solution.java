/*
 * © Copyright 2019 -  SourceClear Inc
 */
package com.sourceclear.agile.piplanning.service.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Optional;

@Entity
@Table(name = "solutions")
public class Solution extends BaseEntity {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  // See https://stackoverflow.com/a/29952572 for why we need to make these fields read-only
  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "board_id")
  private Board board;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "sprint_id")
  private Sprint sprint;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "ticket_id")
  private Ticket ticket;

  @Column
  private boolean unassigned;

  @Column
  private boolean preview;

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  public Solution() {
  }

  public Solution(Board board, Sprint sprint, Ticket ticket, boolean unassigned, boolean preview) {
    this.board = board;
    this.sprint = sprint;
    this.ticket = ticket;
    this.unassigned = unassigned;
    this.preview = preview;
  }

  public Solution(Board board, Ticket ticket, boolean preview) {
    this(board, null, ticket, true, preview);
  }

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //------------------------ Implements:

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  //---------------------------- Property Methods -----------------------------

  public Board getBoard() {
    return board;
  }

  public void setBoard(Board board) {
    this.board = board;
  }

  public Optional<Sprint> getSprint() {
    return Optional.ofNullable(sprint);
  }

  public void setSprint(Sprint sprint) {
    this.sprint = sprint;
  }

  public Ticket getTicket() {
    return ticket;
  }

  public void setTicket(Ticket ticket) {
    this.ticket = ticket;
  }

  public boolean isUnassigned() {
    return unassigned;
  }

  public void setUnassigned(boolean unassigned) {
    this.unassigned = unassigned;
  }

  public boolean isPreview() {
    return preview;
  }

  public void setPreview(boolean preview) {
    this.preview = preview;
  }
}
