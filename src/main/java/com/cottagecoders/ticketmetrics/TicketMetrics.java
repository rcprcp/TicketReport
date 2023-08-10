package com.cottagecoders.ticketmetrics;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import org.zendesk.client.v2.Zendesk;
import org.zendesk.client.v2.model.Audit;
import org.zendesk.client.v2.model.Comment;
import org.zendesk.client.v2.model.Status;
import org.zendesk.client.v2.model.Ticket;
import org.zendesk.client.v2.model.User;
import org.zendesk.client.v2.model.events.AgentMacroReferenceEvent;
import org.zendesk.client.v2.model.events.CommentEvent;
import org.zendesk.client.v2.model.events.CommentPrivacyChangeEvent;
import org.zendesk.client.v2.model.events.CommentRedactionEvent;
import org.zendesk.client.v2.model.events.CreateEvent;
import org.zendesk.client.v2.model.events.ErrorEvent;
import org.zendesk.client.v2.model.events.Event;
import org.zendesk.client.v2.model.events.NotificationEvent;
import org.zendesk.client.v2.model.events.OrganizationActivityEvent;
import org.zendesk.client.v2.model.events.UnknownEvent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TicketMetrics {

  @Parameter(names = {"-s", "--start"}, description = "Start date (oldest)")
  private String start = "";
  @Parameter(names = {"-e", "--end"}, description = "End data (most recent or future)")
  private String end = "";

  public static void main(String[] args) {
    TicketMetrics tm = new TicketMetrics();
    tm.run(args);
    System.exit(0);

  }

  private void run(String[] args) {
    // process command line args.
    JCommander.newBuilder().addObject(this).build().parse(args);

    // process command line dates..
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Date startDate = null;
    Date endDate = null;
    try {
      startDate = sdf.parse(start);
      endDate = sdf.parse(end);
    } catch (ParseException ex) {
      System.out.println("Parse exception ");
      System.exit(6);
    }

    Zendesk zd = null;
    try {
      // set up Zendesk connection
      zd = new Zendesk.Builder(System.getenv("ZENDESK_URL")).setUsername(System.getenv("ZENDESK_EMAIL")).setToken(System.getenv("ZENDESK_TOKEN")).build();

    } catch (Exception ex) {
      System.out.println("Exception: " + ex.getMessage());
      ex.printStackTrace();
      System.exit(1);
    }

    System.out.println("Gather tickets");
    System.out.println("Dates: " + startDate.toString() + "  and " + endDate.toString());

    Map<Long, Integer> userCounts = new HashMap<>();
    int ticketCount = 0;
    for (Ticket t : zd.getTickets()) {
      // backwards logic, because we want to include the specified dates.
      // eg. there is only < or > no <= >=    :)
      if (t.getCreatedAt().before(startDate) || t.getCreatedAt().after(endDate)) {
        continue;
      }

      // add additional criteria here.
      if (t.getStatus().equals(Status.CLOSED) || t.getStatus().equals(Status.SOLVED)) {
        ++ticketCount;
        if (userCounts.get(t.getAssigneeId()) == null) {
          userCounts.put(t.getAssigneeId(), 1);
        } else {
          userCounts.put(t.getAssigneeId(), userCounts.get(t.getAssigneeId()) + 1);
        }
      }

      //TODO:   this is event debugging code.
      if (1 == 0) {
        //TODO remove this someday.
        Iterable<Audit> audits = zd.getTicketAudits(t.getId());
        for (Audit a : audits) {
          System.out.println("Audit: " + a.getCreatedAt() + " " + a.getTicketId());
          List<Event> events = a.getEvents();
          for (Event e : events) {
            if (e instanceof CommentEvent) {
              System.out.println("CommentEvent: ");
            } else if (e instanceof CreateEvent) {
              System.out.println("CreateEvent: ");
            } else if (e instanceof NotificationEvent) {
              System.out.println("NotificationEvent: ");
            } else if (e instanceof UnknownEvent) {
              System.out.println("UnknownEvent: ");
            } else if (e instanceof OrganizationActivityEvent) {
              System.out.println("OrganizationActivityEvent: ");
            } else if (e instanceof CommentPrivacyChangeEvent) {
              System.out.println("CommentPrivacyChangeEvent: ");
            } else if (e instanceof AgentMacroReferenceEvent) {
              System.out.println("AgentMacroReferenceEvent: ");
            } else if (e instanceof ErrorEvent) {
              System.out.println("ErrorEvent: ");
            } else if (e instanceof CommentRedactionEvent) {
              System.out.println("CommentRedactionEvent: ");
            } else {
              System.out.println("unknown event type " + e.getClass().getName());
            }
          }
        }
      }
    }


    System.out.println("" + ticketCount + " tickets processed.");


    Map<Integer, String> last = new TreeMap<>();
    for (Map.Entry<Long, Integer> ent : userCounts.entrySet()) {
      if (ent.getKey() == null) {
        continue;
      }

      User u = zd.getUser(ent.getKey());
      if (u == null) {
        continue;
      } else if (u.getName() == null) {
        continue;
      }
      last.put(ent.getValue(), u.getName());
    }

    // finally, the results.
    for (Map.Entry<Integer, String> u : last.entrySet()) {
      System.out.println("" + u.getKey() + " " + u.getValue());
    }
  }
}
