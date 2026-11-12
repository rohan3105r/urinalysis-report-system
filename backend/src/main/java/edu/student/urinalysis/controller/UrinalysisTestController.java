package edu.student.urinalysis.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import edu.student.urinalysis.model.UrinalysisTest;
import edu.student.urinalysis.repository.UrinalysisTestRepository;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tests")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://localhost:5174",
    "http://127.0.0.1:5173",
    "http://127.0.0.1:5174"
})
public class UrinalysisTestController {
    private final UrinalysisTestRepository repository;

    public UrinalysisTestController(UrinalysisTestRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<UrinalysisTest> getAll() {
        return repository.findAll().stream()
            .sorted((a, b) -> {
                if (a.getTestDate() == null) return 1;
                if (b.getTestDate() == null) return -1;
                return b.getTestDate().compareTo(a.getTestDate());
            }).toList();
    }

    @GetMapping("/{id}")
    public UrinalysisTest getOne(@PathVariable String id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Test record not found"));
    }

    @PostMapping
    public ResponseEntity<UrinalysisTest> create(@Valid @RequestBody UrinalysisTest test) {
        if (test.getId() != null) test.setId(null);
        test.prepareForSave();
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(test));
    }

    @PutMapping("/{id}")
    public UrinalysisTest update(@PathVariable String id, @Valid @RequestBody UrinalysisTest input) {
        UrinalysisTest existing = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Test record not found"));
        existing.setSampleId(input.getSampleId());
        existing.setTestDate(input.getTestDate());
        existing.setPh(input.getPh());
        existing.setSpecificGravity(input.getSpecificGravity());
        existing.setGlucose(input.getGlucose());
        existing.setProtein(input.getProtein());
        existing.setKetones(input.getKetones());
        existing.setBlood(input.getBlood());
        existing.setNitrite(input.getNitrite());
        existing.setLeukocytes(input.getLeukocytes());
        existing.setNotes(input.getNotes());
        existing.prepareForSave();
        return repository.save(existing);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (!repository.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Test record not found");
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public Map<String, Object> stats() {
        List<UrinalysisTest> all = repository.findAll();
        long flagged = all.stream().filter(t -> "Review recommended".equals(t.getReviewStatus())).count();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalTests", all.size());
        result.put("flaggedTests", flagged);
        result.put("clearTests", all.size() - flagged);
        result.put("recentTests", all.stream().sorted((a,b) -> b.getCreatedAt().compareTo(a.getCreatedAt())).limit(5).toList());
        return result;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validationError(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }
}
