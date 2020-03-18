/*
 * © Copyright 2019 -  SourceClear Inc
 */
package com.sourceclear.agile.piplanning.service.entities;

import com.sourceclear.agile.piplanning.objects.TicketI;
import com.sourceclear.agile.piplanning.objects.TicketO;
import com.sourceclear.agile.piplanning.objects.TicketCU;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Set;

@Entity
@Table(name = "tickets")
public class Ticket extends BaseEntity {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "home_board_id")
  private Board origin;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "board_id")
  private Board board;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "epic_id")
  private Epic epic;

  @Column
  private String description;

  @Column
  private int weight;

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  public Ticket() {
  }

  public Ticket(Board board, Epic epic, TicketCU t) {
    this.board = this.origin = board;
    this.epic = epic;
    this.description = t.getSummary();
    this.weight = t.getPoints();
  }

  public Ticket(Board board, Epic epic, TicketI t) {
    this.board = this.origin = board;
    this.epic = epic;
    this.description = t.getDescription();
    this.weight = t.getWeight();
  }

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  public TicketO toModel(Long pin, Set<Long> deps) {
    return new TicketO(getId(), getDescription(), getWeight(), getBoard().equals(getOrigin()),
        getEpic().getId(), pin, deps);
  }

  //------------------------ Implements:

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  //---------------------------- Property Methods -----------------------------

  public Board getOrigin() {
    return origin;
  }

  public void setOrigin(Board origin) {
    this.origin = origin;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public int getWeight() {
    return weight;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }

  public Board getBoard() {
    return board;
  }

  public void setBoard(Board board) {
    this.board = board;
  }

  public Epic getEpic() {
    return epic;
  }

  public void setEpic(Epic epic) {
    this.epic = epic;
  }
}
