package com.neu.finalproject.meskot.repository;

import com.neu.finalproject.meskot.model.UploadJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UploadJobRepository extends JpaRepository<UploadJob, String> {
}