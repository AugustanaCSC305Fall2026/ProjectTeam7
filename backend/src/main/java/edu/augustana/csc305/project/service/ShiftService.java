package edu.augustana.csc305.project.service;

import edu.augustana.csc305.project.dto.ShiftDto;
import edu.augustana.csc305.project.model.Employment;
import edu.augustana.csc305.project.model.Shift;
import edu.augustana.csc305.project.repository.EmploymentRepository;
import edu.augustana.csc305.project.repository.JobRepository;
import edu.augustana.csc305.project.repository.ShiftRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ShiftService {
    private final EmploymentRepository employmentRepository;
    private final ShiftRepository shiftRepository;
    private final JobRepository jobRepository;

    public ShiftService(EmploymentRepository employmentRepository,
                        ShiftRepository shiftRepository,
                        JobRepository jobRepository) {
        this.employmentRepository = employmentRepository;
        this.shiftRepository = shiftRepository;
        this.jobRepository = jobRepository;
    }

    public List<ShiftDto> getUpcomingShifts(ObjectId userId, Instant from, Instant to) {
        List<Employment> employments = employmentRepository.findByUserId(userId);
        List<ObjectId> employmentIds = employments.stream()
                .filter(Employment::isActive)
                .map(Employment::getId)
                .toList();

        Instant now = Instant.now();
        return shiftRepository.findByEmploymentIdsAndRange(employmentIds, from, to)
                .stream()
                .map(shift -> toDto(shift, now))
                .toList();
    }

    private ShiftDto toDto(Shift shift, Instant now) {
        String jobTitle = jobRepository.findById(shift.getJobId())
                .map(job -> job.getTitle())
                .orElseThrow(() -> new IllegalStateException(
                        "Shift refers to a job that does not exist"));

        return new ShiftDto(
                shift.getId().toHexString(),
                jobTitle,
                shift.getStartAt(),
                shift.getEndAt(),
                shift.getStatus(),
                shift.getNote(),
                shift.getStartAt().isAfter(now)
        );
    }
}
