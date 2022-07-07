package com.cottagecoders.ticketreport.shared;

import java.util.HashMap;
import java.util.Map;

public class AuthorCache {
  Map<Long, String> authors = new HashMap<>();

  public AuthorCache() {

  }
  public void put(Long authorId, String authorName) {
    authors.put(authorId, authorName);
  }

  public String lookup(Long authorId) {
    return authors.get(authorId);
  }
}
