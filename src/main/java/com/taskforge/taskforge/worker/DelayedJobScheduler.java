package com.taskforge.taskforge.worker;


import com.taskforge.taskforge.dto.JobRequest;
import com.taskforge.taskforge.model.Job;
import com.taskforge.taskforge.queue.RedisQueue;
import com.taskforge.taskforge.repository.JobRepository;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
@AllArgsConstructor
public class DelayedJobScheduler {

    private RedisQueue redisQueue;
    private JobRepository jobRepository;

    @Scheduled(fixedDelay = 1000)

    public void runWithDelay(){

        Set<Long> delayedJobs = redisQueue.getReadyJobs();

        if(!delayedJobs.isEmpty()){
            System.out.println("DelayedJobScheduler: found " + delayedJobs.size() + " ready jobs");
            for (Long jobId : delayedJobs){
                Optional<Job> j = jobRepository.findById(jobId);
                System.out.println("Moving job " + jobId + " from delayed to pending");
                redisQueue.removeFromDelayed(jobId);
                j.ifPresent(job -> redisQueue.pushJob(job.getId(), job.getPriority()));
            }
        }

    }

}
