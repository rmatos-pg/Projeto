package com.portfolio.etl.order.repository;

import com.portfolio.etl.order.domain.JobRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface JobRunRepository extends JpaRepository<JobRun, UUID> {

    Optional<JobRun> findByJobIdAndRunKey(String jobId, String runKey);
}
