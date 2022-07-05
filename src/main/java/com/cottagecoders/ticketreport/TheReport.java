package com.cottagecoders.ticketreport;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TheReport {
  String organizationName;
  long reportTimeUTC;
  List<ATicket> tickets;

  public TheReport(String organizationName) {
    this.organizationName = organizationName;
    this.reportTimeUTC = new Date().getTime();
    tickets = new ArrayList<>();
  }

  public String getOrganizationName() {
    return organizationName;
  }

  public List<ATicket> getTickets() {
    return tickets;
  }
  public void addTicket(ATicket t) {
    tickets.add(t);
  }

  public long getReportTimeUTC() {
    return reportTimeUTC;
  }
}
