package com.cottagecoders.ticketreport.jsonreport;

import com.cottagecoders.ticketreport.shared.AuthorCache;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.zendesk.client.v2.Zendesk;
import org.zendesk.client.v2.model.Comment;
import org.zendesk.client.v2.model.Organization;
import org.zendesk.client.v2.model.Ticket;

import java.io.IOException;
import java.io.PrintWriter;

public class JsonReport {
  Organization org;
  Zendesk zdAPI;

  public JsonReport(Zendesk zdAPI, Organization org) {
    this.zdAPI = zdAPI;
    this.org = org;
  }

  public void create() {

    TheReport report = new TheReport(org.getName());
    AuthorCache cache = new AuthorCache();

    for (Ticket t : zdAPI.getOrganizationTickets(org.getId())) {
      ATicket tickets = new ATicket(t);
      for (Comment c : zdAPI.getTicketComments(t.getId())) {
        // verify it's not internal
        if (c.isPublic()) {
          // Map authorId to name.
          String authorName;
          if ((authorName = cache.lookup(c.getAuthorId())) == null) {
            cache.put(c.getAuthorId(), zdAPI.getUser(c.getAuthorId()).getName());
          }
          AComment comment = new AComment(authorName, c.getCreatedAt().getTime(), c.getHtmlBody());
          tickets.addComment(comment);

        } else {
          System.out.println("Not Public comment: " + c.getBody());
        }
      }
      report.addTicket(tickets);
    }

    try (final PrintWriter pw = new PrintWriter("output.json")) {

      String reportPrettyJson = null;
      // create JSON here.
      try {
        ObjectMapper mapper = new ObjectMapper();
        reportPrettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(report);

      } catch (JsonProcessingException ex) {
        System.out.println("exception: " + ex.getMessage());
        ex.printStackTrace();
        System.exit(3);
      }

      pw.print(reportPrettyJson);

    } catch (IOException ex) {
      System.out.println("exception: " + ex.getMessage());
      ex.printStackTrace();
      System.exit(2);
    }
  }
}
