/*
 * © Copyright 2019 -  SourceClear Inc
 */
package com.sourceclear.agile.piplanning.service.entities;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class Dependency {

  ///////////////////////////// Class Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  ////////////////////////////// Class Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //////////////////////////////// Attributes \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  @Column(name = "from_ticket_id")
  private long fromTicketId;

  @Column(name = "to_ticket_id")
  private long toTicketId;

  /////////////////////////////// Constructors \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\


  public Dependency(long fromTicketId, long toTicketId) {
    this.fromTicketId = fromTicketId;
    this.toTicketId = toTicketId;
  }

  public Dependency() {
  }

  ////////////////////////////////// Methods \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

  //------------------------ Implements:

  //------------------------ Overrides:

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Dependency that = (Dependency) o;
    return fromTicketId == that.fromTicketId &&
        toTicketId == that.toTicketId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(fromTicketId, toTicketId);
  }

  //---------------------------- Abstract Methods -----------------------------

  //---------------------------- Utility Methods ------------------------------

  //---------------------------- Property Methods -----------------------------

  public long getFromTicketId() {
    return fromTicketId;
  }

  public void setFromTicketId(long fromTicketId) {
    this.fromTicketId = fromTicketId;
  }

  public long getToTicketId() {
    return toTicketId;
  }

  public void setToTicketId(long toTicketId) {
    this.toTicketId = toTicketId;
  }
}
