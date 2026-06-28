package com.taskforge.taskforge.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class HealthController {

  @GetMapping("/health")
  public Map<String, String> healthChecker(){


      Map<String, String > data = new HashMap<>() ;

      data.put("status", "ok");
      data.put("app", "TaskForge");

      return data;

  }
}
