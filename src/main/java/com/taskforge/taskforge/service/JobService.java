package com.taskforge.taskforge.service;


import com.taskforge.taskforge.dto.JobRequest;
import com.taskforge.taskforge.model.Job;
import com.taskforge.taskforge.model.JobStatus;
import com.taskforge.taskforge.queue.RedisQueue;
import com.taskforge.taskforge.repository.JobRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@AllArgsConstructor
public class JobService {

    private JobRepository jobRepository;
    private RedisQueue redisQueue;

    public Job createJob(JobRequest jobRequest) {
        Job job = new Job();
        job.setStatus(JobStatus.PENDING);
        job.setRetryCount(0);
        job.setType(jobRequest.getType());
        job.setPayload(jobRequest.getPayload());
        Job j =  jobRepository.save(job);
        redisQueue.pushJob(j.getId());
        return j;
    }


    public List<Job> findByStatus(String jsr){

        JobStatus status = JobStatus.valueOf(jsr.toUpperCase());
        return jobRepository.findByStatus(status);
    }
}
