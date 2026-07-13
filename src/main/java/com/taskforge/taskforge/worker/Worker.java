package com.taskforge.taskforge.worker;


import com.taskforge.taskforge.config.JobEventPublisher;
import com.taskforge.taskforge.model.Job;
import com.taskforge.taskforge.model.JobStatus;
import com.taskforge.taskforge.queue.RedisQueue;
import com.taskforge.taskforge.repository.JobRepository;
import com.taskforge.taskforge.worker.handlers.SendWebhookHandler;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
//import com.taskforge.taskforge.model.JobStatus;
import java.util.Optional;

@Component
@AllArgsConstructor
public class Worker {
    private RedisQueue redisQueue;
    private JobRepository jobRepository;
    private SendWebhookHandler webhookHandler;
    private JobEventPublisher jobEventPublisher;


    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void start() {
        System.out.println("Background worker started on thread: " + Thread.currentThread().getName());
        // start a background thread that loops forever

        while(!Thread.currentThread().isInterrupted()){
            Long jobId = redisQueue.popJob();

            try{
                doBackgroundOperations(jobId);
            }
            catch (Exception e){
                handleWorkerException(jobId,e);
            }



        }

        // each iteration:
        // 1. call redisQueue.popJob()
        // 2. if null → sleep 500ms, continue
        // 3. if not null → fetch job from PostgreSQL
        // 4. execute the job
        // 5. update status to COMPLETED or FAILED
    }


    private void doBackgroundOperations(Long jobId) throws InterruptedException {


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
                jobEventPublisher.publishJobUpdate(job);
                Thread.sleep(3000);
                System.out.println("executing the job by " + Thread.currentThread().getName());
                if (job.getType().equals("webhook")){
                    System.out.println(webhookHandler.poseRequest(job.getPayload()));
                    job.setStatus(JobStatus.COMPLETED);

                    jobRepository.save(job);
                    jobEventPublisher.publishJobUpdate(job);
                    redisQueue.acknowledgeJob(job.getId());
                }

                else{
                    job.setStatus(JobStatus.FAILED);

                    jobRepository.save(job);
                    jobEventPublisher.publishJobUpdate(job);
                    redisQueue.acknowledgeJob(job.getId());
                }

            }


        }



    }

    private void handleWorkerException(Long jobId, Exception e){

        if (jobId == null) return;

        Optional<Job> jobOptional =  jobRepository.findById(jobId);
        if (jobOptional.isPresent()){
            Job job = jobOptional.get();
            int maxRetries = 3;
            int retries = job.getRetryCount();
            if (retries < maxRetries){
                job.setRetryCount(job.getRetryCount() + 1);
                long delay = (long) Math.pow(2, job.getRetryCount()) * 1000;
                System.out.println("Job " + jobId + " failed. Retry " + job.getRetryCount() + "/3. Retrying in " + delay/1000 + "s");
                jobRepository.save(job);
                redisQueue.moveToDelayed(jobId, delay);
            } else {
                System.out.println("Job " + jobId + " exhausted all retries. Moving to dead letter queue.");
                redisQueue.acknowledgeJob(jobId);
                redisQueue.pushToDeadLetter(jobId);
                job.setStatus(JobStatus.FAILED);
                jobRepository.save(job);
                jobEventPublisher.publishJobUpdate(job);
            }
        }

        System.out.println("Job " + jobId + " failed: " + e.getMessage());


    }



}
