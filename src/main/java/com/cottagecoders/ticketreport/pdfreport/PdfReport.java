package com.cottagecoders.ticketreport.pdfreport;

import com.cottagecoders.ticketreport.shared.AuthorCache;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.streamsets.supportlibrary.zendesk.ZendeskAPI;
import org.zendesk.client.v2.model.Comment;
import org.zendesk.client.v2.model.Organization;
import org.zendesk.client.v2.model.Ticket;

import java.io.DataInput;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PdfReport {
  ZendeskAPI zdAPI;
  Organization org;

  public PdfReport(ZendeskAPI zdAPI, Organization org) {
    this.zdAPI = zdAPI;
    this.org = org;
  }
  public void create(){
    AuthorCache cache = new AuthorCache();
    SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd-yyyy HH:mm Z");
    LineSeparator ls = new LineSeparator();
    Chunk chunk = new Chunk(ls);

      try {
        // report header
        final Document document = new Document();
        PdfWriter.getInstance(document, Files.newOutputStream(Paths.get("output.pdf")));
        document.open();
        document.add(new Paragraph("Organization: " + org.getName()));
        document.add(new Paragraph("Report Date " + sdf.format(new Date())));
        document.add(new Paragraph("\n\n"));

        for (Ticket t : zdAPI.getTickets(org)) {

          String authorName;
          if ((authorName = cache.lookup(t.getSubmitterId())) == null) {
            cache.put(t.getSubmitterId(), zdAPI.getUser(t.getSubmitterId()));
            authorName = cache.lookup(t.getSubmitterId());
          }

          // generate ticket header on PDF
          document.add(new Paragraph(String.format("Ticket Number %d   Status: %s\nDate %s  Requester: %s",
              t.getId(),  t.getStatus().name(), sdf.format(t.getCreatedAt()), authorName)));
          document.add(new Paragraph("\n\n"));

          for (Comment c : zdAPI.getComments(t.getId())) {
            if (c.isPublic()) {
              // format and add comment to PDF file.
              if ((authorName = cache.lookup(c.getAuthorId())) == null) {
                cache.put(c.getAuthorId(), zdAPI.getUser(c.getAuthorId()));
              }
              document.add(new Paragraph(String.format("Comment Date: %s  Comment Author: %s",
                  sdf.format(c.getCreatedAt()), authorName)));
              // display comments.
              document.add(new Paragraph(" "));
              document.add(new Paragraph(c.getBody()));
              document.add(chunk);
            }
          }
          document.newPage();
        }
        document.close();

      } catch(IOException | DocumentException ex) {
        System.out.println("exception: " + ex.getMessage());
        ex.printStackTrace();
        System.exit(4);
      }

  }
}
