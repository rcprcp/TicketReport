package com.cottagecoders.ticketreport;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.cottagecoders.ticketreport.jsonreport.AComment;
import com.cottagecoders.ticketreport.jsonreport.ATicket;
import com.cottagecoders.ticketreport.jsonreport.JsonReport;
import com.cottagecoders.ticketreport.jsonreport.TheReport;
import com.cottagecoders.ticketreport.pdfreport.PdfReport;
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

  @Parameter(names = {"-o", "--org"}, required = true, description = "Partial organization name (case insensitive)")
  private String org;

  @Parameter(names = {"-p", "--pdf"}, description = "Create PDF file")
  private Boolean doPdf = false;

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

    if(doPdf) {
      PdfReport pdf = new PdfReport(zdAPI, myOrg);
      pdf.create();
    } else {
      //JSON
      JsonReport json = new JsonReport(zdAPI, myOrg);
      json.create();
    }

  }
}
