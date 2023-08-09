package com.cottagecoders.ticketreport;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.cottagecoders.ticketreport.jsonreport.JsonReport;
import com.cottagecoders.ticketreport.pdfreport.PdfReport;
import org.zendesk.client.v2.Zendesk;
import org.zendesk.client.v2.model.Organization;

import java.util.ArrayList;
import java.util.List;

public class TicketReport {

  // cli options:

  @Parameter(names = {"-o", "--org"}, required = true, description = "Partial organization name (case insensitive)")
  private String org;

  @Parameter(names = {"-p", "--pdf"}, description = "Create PDF file")
  private Boolean doPdf = false;

  @Parameter(names = "--help", help = true)
  private boolean help;

  @Parameter(names = {"-t", "--tickets"}, description = "List of tickets to generate a report from")
  private List<Long> tickets = new ArrayList();

  public static void main(String[] args) {
    TicketReport tr = new TicketReport();
    tr.run(args);
    System.exit(0);

  }

  private void run(String[] args) {
    // process command line args.
    JCommander.newBuilder().addObject(this).build().parse(args);


    Zendesk zd = null;
    try {
      zd = new Zendesk.Builder(System.getenv("ZENDESK_URL")).setUsername(System.getenv("ZENDESK_EMAIL")).setToken(System.getenv("ZENDESK_TOKEN")).build();
    } catch (Exception ex) {
      System.out.println("Exception: " + ex.getMessage());
      ex.printStackTrace();
      System.exit(1);
    }

    int orgCount = 0;
    Organization myOrg = null;
    for (Organization o : zd.getOrganizations()) {
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

    if (doPdf) {
      PdfReport pdf = new PdfReport(zd, myOrg, tickets);
      pdf.create();
    } else {
      //JSON
      JsonReport json = new JsonReport(zd, myOrg);
      json.create();
    }

  }
}
