package com.codeit.sb01otbooteam06.domain.clothes.service;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Service
public class JsoupService {

  public Document getDocument(String url) throws IOException {
    return Jsoup.connect(url).get();
  }
}