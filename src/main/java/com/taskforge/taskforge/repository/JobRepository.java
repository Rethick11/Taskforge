package com.taskforge.taskforge.repository;

import com.taskforge.taskforge.model.Job;
import com.taskforge.taskforge.model.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByStatus(JobStatus status);

}
