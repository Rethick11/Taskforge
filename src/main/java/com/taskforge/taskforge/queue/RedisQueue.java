package com.taskforge.taskforge.queue;


import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RedisQueue {
    private RedisTemplate<String, Long> redisTemplate;


    public Long pushJob(Long jobId){
        redisTemplate.opsForList().leftPush("queue:pending",jobId);
        return jobId;
    }

    public Long popJob(){
      return (Long) redisTemplate.opsForList()
                .move(ListOperations.MoveFrom.fromTail("queue:pending")
                        ,ListOperations.MoveTo.toHead("queue:processing") );


    }

    public void acknowledgeJob(Long jobId) {
        redisTemplate.opsForList().remove("queue:processing", 1, jobId);
    }
}
