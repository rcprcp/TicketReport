package com.cottagecoders.ticketreport;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.streamsets.supportlibrary.aws.AWSSecret;
import com.streamsets.supportlibrary.zendesk.ZendeskAPI;
import org.zendesk.client.v2.model.Comment;
import org.zendesk.client.v2.model.Organization;
import org.zendesk.client.v2.model.Ticket;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class TicketReport {

  // cli options:

  Map<Long, String> authors = new HashMap<>();
  @Parameter(names = {"-o", "--org"}, required = true, description = "Partial organization name (case insensitive)")
  private String org;

  @Parameter(names = "--help", help = true)
  private boolean help;

  public static void main(String[] args) {
    TicketReport tr = new TicketReport();
    tr.run(args);
    System.exit(0);

  }

  private void run(String[] args) {
    // process command line args.
    JCommander.newBuilder().addObject(this).build().parse(args);

    //get zendesk connection
    AWSSecret secret = new AWSSecret();

    Map<String, String> creds = null;
    try {
      creds = secret.getSecret(System.getenv("SECRET_NAME"), System.getenv("AWS_REGION"));
    } catch (Exception ex) {
      System.out.println("Exception: " + ex.getMessage());
      ex.printStackTrace();
      System.exit(1);
    }

    ZendeskAPI zdAPI = new ZendeskAPI(creds);

    int orgCount = 0;
    Organization myOrg = null;
    for (Organization o : zdAPI.getAllOrganizations()) {
      if (o.getName().toLowerCase().contains(org.toLowerCase())) {
        //count matching
        ++orgCount;
        myOrg = o;
        System.out.println("Organization: " + o.getName());
      }

    }

    if (orgCount > 1) {
      System.out.println("organization must be unique");
      System.exit(4);

    }

    TheReport report = new TheReport(myOrg.getName());

    for (Ticket t : zdAPI.getTickets(myOrg)) {
      ATicket tickets = new ATicket(t);
      for (Comment c : zdAPI.getComments(t.getId())) {
        // verify it's not internal
        if (c.isPublic()) {
          // Map authorId to name.
          if (!authors.containsKey(c.getAuthorId())) {
            authors.put(c.getAuthorId(), zdAPI.getUser(c.getAuthorId()));
          }
          AComment comment = new AComment(authors.get(c.getAuthorId()),
              c.getCreatedAt().getTime(),
              c.getHtmlBody()
          );
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
