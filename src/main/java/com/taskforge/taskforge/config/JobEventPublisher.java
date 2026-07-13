package com.taskforge.taskforge.config;

import com.taskforge.taskforge.model.Job;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;



@Component
@AllArgsConstructor
public class JobEventPublisher {

    private SimpMessagingTemplate messagingTemplate;

    public void publishJobUpdate(Job job) {
        messagingTemplate.convertAndSend("/topic/jobs", job);
    }
}

