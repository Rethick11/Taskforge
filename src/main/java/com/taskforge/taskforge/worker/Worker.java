package com.taskforge.taskforge.worker;


import com.taskforge.taskforge.model.Job;
import com.taskforge.taskforge.model.JobStatus;
import com.taskforge.taskforge.queue.RedisQueue;
import com.taskforge.taskforge.repository.JobRepository;
import com.taskforge.taskforge.worker.handlers.SendWebhookHandler;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@AllArgsConstructor
public class Worker {
    private RedisQueue redisQueue;
    private JobRepository jobRepository;
    private SendWebhookHandler webhookHandler;

    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void start() {
        System.out.println("Background worker started on thread: " + Thread.currentThread().getName());
        // start a background thread that loops forever

        while(!Thread.currentThread().isInterrupted()){

            try{
                doBackgroundOperations(Thread.currentThread());
            }
            catch (Exception e){
                System.out.println("error while running the thread" + e.getMessage());
            }



        }

        // each iteration:
        // 1. call redisQueue.popJob()
        // 2. if null → sleep 500ms, continue
        // 3. if not null → fetch job from PostgreSQL
        // 4. execute the job
        // 5. update status to COMPLETED or FAILED
    }


    private void doBackgroundOperations(Thread thread) throws InterruptedException {

        Long jobId = redisQueue.popJob();
        if (jobId == null){
            Thread.sleep(5000);
        }else{
            Optional<Job> jobOptional =  jobRepository.findById(jobId);
            if (jobOptional.isEmpty()){
                System.out.println("no job found");
            }else{
                Job job =  jobOptional.get();
                job.setStatus(JobStatus.PROCESSING);
                jobRepository.save(job);


                System.out.println("executing the job by " + Thread.currentThread().getName());
                if (job.getType().equals("webhook")){
                    System.out.println(webhookHandler.poseRequest(job.getPayload()));
                    job.setStatus(JobStatus.COMPLETED);
                    jobRepository.save(job);
                    redisQueue.acknowledgeJob(job.getId());
                }

            }


        }



    }



}
