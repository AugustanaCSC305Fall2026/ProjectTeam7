package edu.augustana.csc305.project.service;

import edu.augustana.csc305.project.dto.AvailabilityRequest;
import edu.augustana.csc305.project.dto.AvailabilityResponse;
import edu.augustana.csc305.project.dto.AvailabilityWindowDto;
import edu.augustana.csc305.project.model.Availability;
import edu.augustana.csc305.project.repository.AvailabilityRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Service
public class AvailabilityService {
    private final AvailabilityRepository repository;

    public AvailabilityService(AvailabilityRepository repository) {
        this.repository = repository;
    }

    public AvailabilityResponse getForUser(ObjectId userId) {
        List<AvailabilityWindowDto> windows = repository.findByUserId(userId).stream()
                .sorted(Comparator.comparingInt(window -> DayOfWeek.valueOf(window.getDayOfWeek()).getValue()))
                .map(window -> new AvailabilityWindowDto(
                        window.getDayOfWeek(), window.getStartTime(), window.getEndTime()))
                .toList();
        return new AvailabilityResponse(userId.toHexString(), windows);
    }

    public AvailabilityResponse replaceForUser(ObjectId userId, AvailabilityRequest request) {
        if (request == null || request.windows() == null) {
            throw new IllegalArgumentException("Availability windows are required");
        }

        List<Availability> windows = request.windows().stream().map(window -> {
            if (window == null || window.dayOfWeek() == null
                    || window.startTime() == null || window.endTime() == null) {
                throw new IllegalArgumentException("Each availability window needs a day and start/end time");
            }

            DayOfWeek day = parseDay(window.dayOfWeek());
            LocalTime start = parseTime(window.startTime());
            LocalTime end = parseTime(window.endTime());
            if (!end.isAfter(start)) {
                throw new IllegalArgumentException("Availability end time must be after start time");
            }

            return new Availability(new ObjectId(), userId, day.name(),
                    start.toString(), end.toString());
        }).toList();

        repository.replaceForUser(userId, windows);
        return getForUser(userId);
    }

    private DayOfWeek parseDay(String value) {
        try {
            return DayOfWeek.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid day of week: " + value);
        }
    }

    private LocalTime parseTime(String value) {
        try {
            return LocalTime.parse(value);
        } catch (Exception exception) {
            throw new IllegalArgumentException("Times must use HH:mm format");
        }
    }
}
