package edu.augustana.csc305.project.ClockIn;

import java.time.Instant;

public class BreakEntry {

    private Instant breakStart;
    private Instant breakEnd;


    public  BreakEntry(Instant breakStart){
        this.breakStart = breakStart;
    }
    public  Instant getBreakStart(){return breakStart;}
    public void setBreakStart(Instant breakStart){this.breakStart = breakStart;}

    public  Instant getBreakEnd(){return breakEnd;}
    public void setBreakEnd(Instant breakEnd){this.breakEnd = breakEnd;}
}
