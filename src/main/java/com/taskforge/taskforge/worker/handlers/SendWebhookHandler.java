package com.taskforge.taskforge.worker.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskforge.taskforge.dto.WebhookResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;


@Component
@AllArgsConstructor
public class SendWebhookHandler {

    private RestTemplate restTemplate;

    public String  poseRequest(String payload){

        ObjectMapper objectMapper = new ObjectMapper();
        WebhookResponse rs = null;
        try{
             rs = objectMapper.readValue(payload, WebhookResponse.class);

        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("data", rs.getData()); // Becomes {"message": "xyz"}

        HttpEntity<Map<String, String>> request = new HttpEntity<>(bodyMap, headers);


        return restTemplate.postForObject(rs.getUrl(), request, String.class);


    }

}
