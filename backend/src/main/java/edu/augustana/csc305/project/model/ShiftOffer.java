package edu.augustana.csc305.project.model;

import org.bson.types.ObjectId;

import java.time.Instant;

public class ShiftOffer{
    private ObjectId offeredByEmploymentId;
    private Instant offeredAt;
    private String reason;
    private ObjectId claimedByEmploymentId;
    private Instant claimedAt;

    public ShiftOffer(){

    }
    
    public ShiftOffer(ObjectId offeredByEmploymentId,
     Instant offeredAt,
     String reason,
     ObjectId claimedByEmploymentId,
     Instant claimedAt){
    this.offeredByEmploymentId = offeredByEmploymentId;
    this.offeredAt = offeredAt;
    this.reason = reason;
    this.claimedByEmploymentId = claimedByEmploymentId;
    this.claimedAt = claimedAt;
    }
    public ObjectId getOfferedByEmploymentId(){
        return offeredByEmploymentId;
    }
    public void setOfferedByEmploymentId(ObjectId offeredByEmploymentId){
        this.offeredByEmploymentId = offeredByEmploymentId;
    }
    public Instant getOfferedAt(){
        return offeredAt;
    }
    public void setOfferedAt(Instant offeredAt){
        this.offeredAt = offeredAt;
    }
    public String getReason(){
        return reason;
    }
    public void setReason(String reason){
        this.reason = reason;
    }
    public ObjectId getClaimedByEmploymentId(){
        return claimedByEmploymentId;
    }
    public void setClaimedByEmploymentId(ObjectId claimedByEmploymentId){
        this.claimedByEmploymentId = claimedByEmploymentId;
    }
    public Instant getClaimedAt(){
        return claimedAt;
    }
    public void setClaimedAt(Instant claimedAt){
        this.claimedAt = claimedAt;
    }

}

