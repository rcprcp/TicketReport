package com.cottagecoders.ticketreport.jsonreport;

import com.cottagecoders.ticketreport.jsonreport.AComment;
import org.zendesk.client.v2.model.Ticket;

import java.util.ArrayList;
import java.util.List;

public class ATicket {
  long ticketNumber;
  long createdUTC;
  String currentStatus;
  List<AComment> comments;

  public ATicket(Ticket ticket) {
    this.ticketNumber = ticket.getId();
    this.createdUTC = ticket.getCreatedAt().getTime();
    this.currentStatus = ticket.getStatus().name();

    comments = new ArrayList<>();
  }

  public void addComment(AComment c) {
    comments.add(c);
  }

  public long getTicketNumber() {
    return ticketNumber;
  }

  public long getCreatedUTC() {
    return createdUTC;
  }

  public List<AComment> getComments() {
    return comments;
  }

  public String getCurrentStatus() {
    return currentStatus;
  }
}
