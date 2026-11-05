package edu.augustana.csc305.project.service;

import edu.augustana.csc305.project.dto.HoursEntryDto;
import edu.augustana.csc305.project.dto.HoursSummaryDto;
import edu.augustana.csc305.project.model.Employment;
import edu.augustana.csc305.project.model.Job;
import edu.augustana.csc305.project.model.TimeEntry;
import edu.augustana.csc305.project.repository.EmploymentRepository;
import edu.augustana.csc305.project.repository.JobRepository;
import edu.augustana.csc305.project.repository.TimeEntryRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HoursService {

    private final TimeEntryRepository timeEntryRepository;
    private final EmploymentRepository employmentRepository;
    private final JobRepository jobRepository;

    public HoursService(
            TimeEntryRepository timeEntryRepository,
            EmploymentRepository employmentRepository,
            JobRepository jobRepository) {

        this.timeEntryRepository = timeEntryRepository;
        this.employmentRepository = employmentRepository;
        this.jobRepository = jobRepository;
    }

    public HoursSummaryDto getHours(ObjectId userId, ObjectId jobId) {

        List<TimeEntry> timeEntries = timeEntryRepository.findByUserId(userId);
        List<HoursEntryDto> entries = new ArrayList<>();

        for (TimeEntry timeEntry : timeEntries) {

            Employment employment = employmentRepository
                    .findById(timeEntry.getEmploymentId())
                    .orElse(null);

            if (employment == null) {
                continue;
            }

            // Story 11: filter by job when a jobId is supplied
            if (jobId != null && !jobId.equals(employment.getJobId())) {
                continue;
            }

            Job job = jobRepository
                    .findById(employment.getJobId())
                    .orElse(null);

            if (job == null) {
                continue;
            }

            entries.add(new HoursEntryDto(
                    timeEntry.getId().toHexString(),
                    timeEntry.getEmploymentId().toHexString(),
                    employment.getJobId().toHexString(),
                    job.getTitle(),
                    timeEntry.getClockInAt(),
                    timeEntry.getClockOutAt(),
                    timeEntry.getBreakMinutes(),
                    timeEntry.isOnBreak(),
                    timeEntry.getWorkedMinutes()
            ));
        }

        int totalMinutes = entries.stream()
            .mapToInt(HoursEntryDto::workedMinutes)
            .sum();

    long totalGrossPayCents = 0;

    for (TimeEntry timeEntry : timeEntries) {
        Employment employment = employmentRepository
                .findById(timeEntry.getEmploymentId())
                .orElse(null);

        if (employment == null) {
            continue;
        }

        if (jobId != null && !jobId.equals(employment.getJobId())) {
            continue;
        }

        if (jobRepository.findById(employment.getJobId()).isEmpty()) {
            continue;
        }

        if (timeEntry.getClockOutAt() == null) {
            continue;
        }

        long workedMinutes = timeEntry.getWorkedMinutes();
        long payRateCents = employment.getPayRateCents();

        totalGrossPayCents += Math.round(
                workedMinutes * payRateCents / 60.0
        );
    }

    return new HoursSummaryDto(entries, totalMinutes, totalGrossPayCents);
    }
}