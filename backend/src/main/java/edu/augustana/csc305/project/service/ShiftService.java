package edu.augustana.csc305.project.service;

import edu.augustana.csc305.project.repository.ShiftRepository;
import edu.augustana.csc305.project.repository.EmploymentRepository;
import edu.augustana.csc305.project.repository.JobRepository;

import edu.augustana.csc305.project.model.Shift;
import edu.augustana.csc305.project.model.Employment;
import edu.augustana.csc305.project.model.ShiftStatus;
import edu.augustana.csc305.project.model.ShiftOffer;
import edu.augustana.csc305.project.model.Job;
import edu.augustana.csc305.project.dto.OpenShiftDto;
import edu.augustana.csc305.project.exception.ShiftException;

import java.util.ArrayList;
import java.util.List;
import java.time.Instant;

import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ShiftService {
    private final ShiftRepository shiftRepository;
    private final EmploymentRepository employmentRepository;
    private final JobRepository jobRepository;

    public ShiftService(
            ShiftRepository shiftRepository,
            EmploymentRepository employmentRepository, JobRepository jobRepository) {
        this.shiftRepository = shiftRepository;
        this.employmentRepository = employmentRepository;
        this.jobRepository = jobRepository;
    }

    public Shift offerShift(ObjectId shiftId, ObjectId userId, String reason){
        Shift shift = shiftRepository.findById(shiftId)
        .orElseThrow(() -> new IllegalArgumentException("Shift does not exist"));

        Employment employment = employmentRepository.findByIdAndUserId(shift.getEmploymentId(), userId)
        .orElseThrow(() -> new ShiftException(HttpStatus.FORBIDDEN, "NOT_YOUR_SHIFT"));

        if(shift.hasStarted()){
            throw new ShiftException(HttpStatus.CONFLICT, "SHIFT_ALREADY_STARTED");
        }

        if (shift.getStatus() == ShiftStatus.OFFERED){
            throw new ShiftException(HttpStatus.CONFLICT, "ALREADY_OFFERED");
        }

        ShiftOffer offer = new ShiftOffer(employment.getId(), Instant.now(), reason, null, null);

        shift.setOffer(offer);
        shift.setStatus(ShiftStatus.OFFERED);

        shiftRepository.updateShift(shift);
        
        return shift;
    }

     public Shift withdrawOffer(ObjectId shiftId, ObjectId userId){
        Shift shift = shiftRepository.findById(shiftId)
        .orElseThrow(() -> new IllegalArgumentException("Shift does not exist"));

        employmentRepository.findByIdAndUserId(shift.getEmploymentId(), userId)
        .orElseThrow(() -> new ShiftException(HttpStatus.FORBIDDEN, "NOT_YOUR_SHIFT"));

         if (shift.getOffer() != null && shift.getOffer().getClaimedByEmploymentId() != null){
             throw new ShiftException(HttpStatus.CONFLICT, "OFFER_ALREADY_CLAIMED");
         }

        if (shift.getStatus() != ShiftStatus.OFFERED){
            throw new ShiftException(HttpStatus.CONFLICT, "SHIFT_NOT_OFFERED");
        }


        shift.setOffer(null);
        shift.setStatus(ShiftStatus.SCHEDULED);

        shiftRepository.updateShift(shift);

        return shift;
    }

    public List<OpenShiftDto> getOpenShifts(ObjectId userId,Instant from, Instant to) {
        List<Employment> userEmployments = employmentRepository.findByUserId(userId);
        List<OpenShiftDto> openShifts = new ArrayList<>();

        for (Shift shift : shiftRepository.findOfferedShiftsBetween(from, to)) {

            // Do not show the user's own offered shift.
            boolean ownShift = false;

            for (Employment employment : userEmployments) {
                if (shift.getOffer() != null
                        && employment.getId().equals(
                        shift.getOffer().getOfferedByEmploymentId())) {
                    ownShift = true;
                    break;
                }
            }

            if (ownShift) {
                continue;
            }

            // Find an active employment for the same job.
            Employment matchingEmployment = null;

            for (Employment employment : userEmployments) {
                if (employment.isActive()
                        && employment.getJobId().equals(shift.getJobId())) {
                    matchingEmployment = employment;
                    break;
                }
            }

            // User does not work this job, so they should not see the shift.
            if (matchingEmployment == null) {
                continue;
            }

            boolean eligible = true;

            // Check whether this employment already has an overlapping shift.
            for (Shift existingShift :
                    shiftRepository.findByEmploymentId(matchingEmployment.getId())) {

                if (existingShift.getStatus() != ShiftStatus.CANCELLED
                        && existingShift.getStartAt().isBefore(shift.getEndAt())
                        && existingShift.getEndAt().isAfter(shift.getStartAt())) {

                    eligible = false;
                    break;
                }
            }

            Job job = jobRepository.findById(shift.getJobId())
                    .orElseThrow(() ->
                            new IllegalStateException("Shift refers to a job that does not exist"));

            String reason = shift.getOffer() == null
                    ? null
                    : shift.getOffer().getReason();

            openShifts.add(new OpenShiftDto(
                    shift.getId().toHexString(),
                    shift.getJobId().toHexString(),
                    job.getTitle(),
                    shift.getStartAt(),
                    shift.getEndAt(),
                    reason,
                    eligible
            ));
        }

        return openShifts;
    }

    public Shift claimShift(
            ObjectId shiftId,
            ObjectId employmentId,
            ObjectId userId) {

        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new IllegalArgumentException("Shift does not exist"));

        if (shift.getStatus() != ShiftStatus.OFFERED
                || shift.getOffer() == null) {throw new ShiftException(HttpStatus.CONFLICT, "SHIFT_ALREADY_CLAIMED");
        }

        Employment employment = employmentRepository.findByIdAndUserId(employmentId, userId)
                .orElseThrow(() -> new ShiftException(
                        HttpStatus.FORBIDDEN, "WRONG_JOB"));

        if (!employment.isActive()
                || !employment.getJobId().equals(shift.getJobId())) {
            throw new ShiftException(HttpStatus.FORBIDDEN, "WRONG_JOB");
        }

        if (shift.getOffer().getOfferedByEmploymentId().equals(employmentId)) {
            throw new ShiftException(HttpStatus.CONFLICT, "CANNOT_CLAIM_OWN_SHIFT"
            );
        }

        for (Shift existingShift :
                shiftRepository.findByEmploymentId(employmentId)) {

            if (existingShift.getStatus() != ShiftStatus.CANCELLED
                    && existingShift.getStartAt().isBefore(shift.getEndAt())
                    && existingShift.getEndAt().isAfter(shift.getStartAt())) {

                throw new ShiftException(
                        HttpStatus.CONFLICT,
                        "OVERLAPPING_SHIFT"
                );
            }
        }

        return shiftRepository.claimOfferedShift(
                shiftId,
                employmentId,
                Instant.now()
        ).orElseThrow(() -> new ShiftException(
                HttpStatus.CONFLICT,
                "SHIFT_ALREADY_CLAIMED"
        ));
    }

    public List<Shift> getMyShifts(ObjectId userId) {
        List<Employment> userEmployments = employmentRepository.findByUserId(userId);
        List<Shift> myShifts = new ArrayList<>();

        for (Employment employment : userEmployments) {
            if (employment.isActive()) {
                myShifts.addAll(
                        shiftRepository.findByEmploymentId(employment.getId())
                );
            }
        }

        return myShifts;
    }
}