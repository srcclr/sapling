/*
 * © Copyright 2019 -  SourceClear Inc
 */
package com.sourceclear.agile.piplanning.service.entities;

import com.sourceclear.agile.piplanning.objects.StoryRequestO;
import com.sourceclear.agile.piplanning.objects.TicketCU;
import com.sourceclear.agile.piplanning.objects.TicketI;
import com.sourceclear.agile.piplanning.objects.TicketO;
import com.sourceclear.agile.piplanning.service.jooq.tables.records.StoryRequestsRecord;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "tickets")
public class Ticket extends BaseEntity {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

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
    this.board = board;
    this.epic = epic;
    this.description = t.getSummary();
    this.weight = t.getPoints();
  }

  public Ticket(Board board, Epic epic, TicketI t) {
    this.board = board;
    this.epic = epic;
    this.description = t.getDescription();
    this.weight = t.getWeight();
  }

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  public TicketO toModel(Long pin, Set<Long> deps, List<StoryRequestsRecord> storyRequests, boolean blocking) {
    Set<StoryRequestO> requests = storyRequests.stream()
        .map(s -> new StoryRequestO(
            s.getId(),
            s.getState(),
            s.getToBoardId(),
            s.getToTicketId(),
            s.getToTicketDescription(),
            s.getToTicketWeight(),
            s.getToTicketEpicId(),
            s.getToTicketSprintId(),
            s.getNotes()))
        .collect(Collectors.toSet());

    return new TicketO(getId(), getDescription(), getWeight(),
        getEpic().getId(), pin, deps, requests, blocking);
  }

  //------------------------ Implements:

  //------------------------ Overrides:

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  //---------------------------- Property Methods -----------------------------

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
