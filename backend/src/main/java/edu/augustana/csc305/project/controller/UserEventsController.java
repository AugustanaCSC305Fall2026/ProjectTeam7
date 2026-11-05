
package edu.augustana.csc305.project.controller;

import edu.augustana.csc305.project.model.UserEvents;
import edu.augustana.csc305.project.repository.UserEventsRepository;
import edu.augustana.csc305.project.security.AuthenticatedUser;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/user-events")
public class UserEventsController {

    private final UserEventsRepository repository;

    public UserEventsController(UserEventsRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<UserEvents> getEvents(
            @AuthenticationPrincipal AuthenticatedUser user) {

        return repository.findByUserId(
                new ObjectId(user.id()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserEvents createEvent(
            @RequestBody EventRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {

        validate(request);

        UserEvents event = new UserEvents();
        event.setId(new ObjectId());
        event.setUserId(new ObjectId(user.id()));
        event.setTitle(request.title().trim());
        event.setDescription(request.description());
        event.setStartTime(request.startTime());
        event.setEndTime(request.endTime());

        repository.save(event);

        return event;
    }

    @PutMapping("/{id}")
    public UserEvents updateEvent(
            @PathVariable String id,
            @RequestBody EventRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {

        validate(request);

        ObjectId eventId = parseId(id);
        ObjectId userId = new ObjectId(user.id());

        UserEvents event = repository.findByIdAndUserId(eventId, userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        event.setTitle(request.title().trim());
        event.setDescription(request.description());
        event.setStartTime(request.startTime());
        event.setEndTime(request.endTime());

        repository.update(event);

        return event;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEvent(
            @PathVariable String id,
            @AuthenticationPrincipal AuthenticatedUser user) {

        boolean deleted = repository.delete(
                parseId(id),
                new ObjectId(user.id()));

        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    private void validate(EventRequest request) {
        if (request == null || request.title() == null || request.title().isBlank() || request.startTime() == null || request.endTime() == null || !request.endTime().isAfter(request.startTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid event information");
        }
    }

    private ObjectId parseId(String id) {
        if (!ObjectId.isValid(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid event ID");
        }

        return new ObjectId(id);
    }

    public record EventRequest(String title, String description, Instant startTime, Instant endTime) {}
}
