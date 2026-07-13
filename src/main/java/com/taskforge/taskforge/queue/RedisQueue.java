package com.taskforge.taskforge.queue;


import com.taskforge.taskforge.model.Priority;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Set;

@Component
@AllArgsConstructor
public class RedisQueue {
    private RedisTemplate<String, Long> redisTemplate;


    public Long pushHighPriority(Long jobId){
        redisTemplate.opsForList().leftPush("highQueue:pending",jobId);
        return jobId;
    }

    public Long pushNormalPriority(Long jobId){
        redisTemplate.opsForList().leftPush("normalQueue:pending",jobId);
        return jobId;
    }

    public Long pushLowPriority(Long jobId){
        redisTemplate.opsForList().leftPush("lowQueue:pending",jobId);
        return jobId;
    }

    public void pushJob(Long jobId, Priority p){

        switch (p){
            case Priority.HIGH -> this.pushHighPriority(jobId);
            case Priority.NORMAL -> this.pushNormalPriority(jobId);
            case Priority.LOW -> this.pushLowPriority(jobId);

        }


    }



    public Long popHighJob(){
      return (Long) redisTemplate.opsForList()
                .move(ListOperations.MoveFrom.fromTail("highQueue:pending")
                        ,ListOperations.MoveTo.toHead("queue:processing") );
    }

    public Long popNormalJob(){
        return (Long) redisTemplate.opsForList()
                .move(ListOperations.MoveFrom.fromTail("normalQueue:pending")
                        ,ListOperations.MoveTo.toHead("queue:processing") );
    }

    public Long popLowJob(){
        return (Long) redisTemplate.opsForList()
                .move(ListOperations.MoveFrom.fromTail("lowQueue:pending")
                        ,ListOperations.MoveTo.toHead("queue:processing") );
    }


    public Long popJob(){
        Long h_size = redisTemplate.opsForList().size("highQueue:pending");
        Long n_size = redisTemplate.opsForList().size("normalQueue:pending");

        if (h_size > 0){
            return this.popHighJob();
        }else if (n_size > 0){
            return this.popNormalJob();
        }else{
            return this.popLowJob();
        }
    }

    public void acknowledgeJob(Long jobId) {
        redisTemplate.opsForList().remove("queue:processing", 1, jobId);
    }

    public void moveToDelayed(Long jobId, long delayMs) {
        double score = System.currentTimeMillis() + delayMs;
        redisTemplate.opsForList().remove("queue:processing", 1, jobId);
        redisTemplate.opsForZSet().add("queue:delayed", jobId, score);
    }

    public Set<Long> getReadyJobs() {
        return redisTemplate.opsForZSet().rangeByScore("queue:delayed", 0, System.currentTimeMillis());
    }

     public void removeFromDelayed(Long jobId){
         redisTemplate.opsForZSet().remove("queue:delayed", jobId);
     }

    public void pushToDeadLetter(Long jobId) {
        redisTemplate.opsForList().leftPush("queue:deadletter", jobId);
    }

}
