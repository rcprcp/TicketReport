package com.cottagecoders.ticketreport.pdfreport;

import com.cottagecoders.ticketreport.shared.AuthorCache;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.zendesk.client.v2.Zendesk;
import org.zendesk.client.v2.model.Comment;
import org.zendesk.client.v2.model.Organization;
import org.zendesk.client.v2.model.Ticket;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PdfReport {
  Zendesk zdAPI;
  Organization org;
  List<Long> ticketList;
  Document document;
  AuthorCache cache;
  LineSeparator ls;
  SimpleDateFormat sdf;
  Chunk chunk;

  public PdfReport(Zendesk zdAPI, Organization org, List<Long> tickets) {
    this.zdAPI = zdAPI;
    this.org = org;
    this.ticketList = tickets;
  }
  public void create() {
    cache = new AuthorCache();
    sdf = new SimpleDateFormat("MMM-dd-yyyy HH:mm Z");
    ls = new LineSeparator();
    chunk = new Chunk(ls);

      try {
        // report header
        document = new Document();
        PdfWriter.getInstance(document, Files.newOutputStream(Paths.get("output.pdf")));
        document.open();
        document.add(new Paragraph("Organization: " + org.getName()));
        document.add(new Paragraph("Report Date " + sdf.format(new Date())));
        document.add(new Paragraph("\n\n"));

        if(!ticketList.isEmpty()) {
          for (Long ticket : ticketList) {
            Ticket t = zdAPI.getTicket(ticket);
            if(t.getOrganizationId().equals(org.getId())){
              addTicket(t);
              System.out.println("Adding ticket: " + ticket);
            }
            else {
              System.out.println("Ticket " + ticket + " does not belong to the Org. Skipping.");
            }
          }
        }
        else{
          for (Ticket t : zdAPI.getOrganizationTickets(org.getId())) {
            addTicket(t);
          }
        }
        document.close();

      } catch(IOException | DocumentException ex) {
        System.out.println("exception: " + ex.getMessage());
        ex.printStackTrace();
        System.exit(4);
      }

  }

  public void addTicket(Ticket t) {
    try {

      String authorName;
      if ((authorName = cache.lookup(t.getSubmitterId())) == null) {
        cache.put(t.getSubmitterId(), zdAPI.getUser(t.getSubmitterId()).getName());
        authorName = cache.lookup(t.getSubmitterId());
      }

      // generate ticket header on PDF
      document.add(new Paragraph(String.format("Ticket Number %d, Subject: %s  Status: %s\nDate %s  Requester: %s",
          t.getId(), t.getSubject(), t.getStatus().name(), sdf.format(t.getCreatedAt()), authorName
      )));
      document.add(new Paragraph("\n\n"));

      for (Comment c : zdAPI.getTicketComments(t.getId())) {
        if (c.isPublic()) {
          // format and add comment to PDF file.
          if ((authorName = cache.lookup(c.getAuthorId())) == null) {
            cache.put(c.getAuthorId(), zdAPI.getUser(c.getAuthorId()).getName());
          }
          document.add(new Paragraph(String.format("Comment Date: %s  Comment Author: %s",
              sdf.format(c.getCreatedAt()), authorName
          )));
          // display comments.
          document.add(new Paragraph(" "));
          document.add(new Paragraph(c.getBody()));
          document.add(chunk);
        }
      }
      document.newPage();
    } catch (DocumentException ex) {
      System.out.println("exception: " + ex.getMessage());
      ex.printStackTrace();
      System.exit(4);
    }
  }

}
