package com.taskforge.taskforge.worker;
import com.taskforge.taskforge.model.Job;
import com.taskforge.taskforge.model.JobStatus;
import com.taskforge.taskforge.queue.RedisQueue;
import com.taskforge.taskforge.repository.JobRepository;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@AllArgsConstructor
public class WatchdogService {

    private JobRepository jobRepository;
    private RedisQueue redisQueue;
    @Scheduled(fixedDelay = 60000)
    public void runWatchDog(){
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        List<Job> stuckJobs = jobRepository.findStuckJobs(JobStatus.PROCESSING , threshold);

        for (Job j : stuckJobs){
            Long id = j.getId();
            System.out.println("Watchdog recovered stuck job: " + id);
            j.setStatus(JobStatus.PENDING);
            jobRepository.save(j);
            redisQueue.acknowledgeJob(id);
            redisQueue.pushJob(id , j.getPriority());
        }



        List<Job> lostJobs = jobRepository.findStuckJobs(JobStatus.PENDING, threshold);
        for (Job j : lostJobs) {
            redisQueue.pushJob(j.getId(), j.getPriority());
            System.out.println("Watchdog re-queued lost job: " + j.getId());
        }


    }
}
