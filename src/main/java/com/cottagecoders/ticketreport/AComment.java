package com.cottagecoders.ticketreport;

public class AComment {
  String author;
  long time;
  String text;

  public AComment(String author, long time, String text) {
    this.author = author;
    this.time = time;
    this.text = text;

  }

  public String getAuthor() {
    return author;
  }

  public long getTime() {
    return time;
  }

  public String getText() {
    return text;
  }
}

