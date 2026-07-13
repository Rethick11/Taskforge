package com.taskforge.taskforge.dto;

import com.taskforge.taskforge.model.Priority;
import lombok.Data;

@Data
public class JobRequest {
    private String payload;
    private String type;
    private Priority priority;
}
