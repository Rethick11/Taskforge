package com.taskforge.taskforge.controller;


import com.taskforge.taskforge.dto.JobRequest;

import com.taskforge.taskforge.model.Job;


import com.taskforge.taskforge.service.JobService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@AllArgsConstructor
public class JobController {
    private JobService jobService;
    @PostMapping("/jobs")
    public Job postJob(@RequestBody JobRequest jobRequest){
        return jobService.createJob(jobRequest);

    }

    @GetMapping("/jobs")
    public List<Job> findByStatus(@RequestParam String jsr) {
        return jobService.findByStatus(jsr);
    }

    @GetMapping("/jobs/recent")
    public List<Job> getRecentJobs() {
        return jobService.getRecentJobs();
    }
}
