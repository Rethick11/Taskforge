package com.taskforge.taskforge.repository;

import com.taskforge.taskforge.model.Job;
import com.taskforge.taskforge.model.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByStatus(JobStatus status);

    @Query("SELECT j FROM Job j WHERE j.status = :status AND j.updatedAt < :threshold")
    List<Job> findStuckJobs(@Param("status") JobStatus status, @Param("threshold") LocalDateTime threshold);

    List<Job> findTop50ByOrderByCreatedAtDesc();
}
