package com.neu.finalproject.meskot.repository;

import com.neu.finalproject.meskot.model.StoredObject;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StoredObjectRepository extends JpaRepository<StoredObject, Long> {
    Optional<StoredObject> findByObjectKey(String objectKey);
}
