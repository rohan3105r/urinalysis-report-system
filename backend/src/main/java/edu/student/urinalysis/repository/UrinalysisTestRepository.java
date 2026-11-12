package edu.student.urinalysis.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import edu.student.urinalysis.model.UrinalysisTest;

public interface UrinalysisTestRepository extends MongoRepository<UrinalysisTest, String> {
    boolean existsBySampleId(String sampleId);
}
