/*
 * © Copyright 2019 -  SourceClear Inc
 */
package com.sourceclear.agile.piplanning.service.entities;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class Pin {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @Column(name = "sprint_id")
  private long sprintId;

  @Column(name = "ticket_id")
  private long ticketId;

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  public Pin(long sprintId, long ticketId) {
    this.sprintId = sprintId;
    this.ticketId = ticketId;
  }

  public Pin() {
  }

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //------------------------ Implements:

  //------------------------ Overrides:

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Pin pin = (Pin) o;
    return sprintId == pin.sprintId &&
        ticketId == pin.ticketId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(sprintId, ticketId);
  }

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  //---------------------------- Property Methods -----------------------------

  public long getSprintId() {
    return sprintId;
  }

  public void setSprintId(long sprintId) {
    this.sprintId = sprintId;
  }

  public long getTicketId() {
    return ticketId;
  }

  public void setTicketId(long ticketId) {
    this.ticketId = ticketId;
  }
}
