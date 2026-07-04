package com.taskforge.taskforge.controller;


import com.taskforge.taskforge.dto.WebhookRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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


    @PostMapping("/aibwebhook")
    public String webhook(@RequestBody WebhookRequest payload){


        if (Objects.equals(payload.getData(), "code101.aibbank.com")){
            return "yes your webhook got submitted and valid aib credentials proceed with your request";
        }

        return "failure";


    }
}
