package com.taskforge.taskforge.dto;

import lombok.Data;

@Data
public class WebhookResponse {
    private String url;
    private String data;
}
