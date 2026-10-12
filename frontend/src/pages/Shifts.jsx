import React, { useEffect, useState } from "react";
import api from "../api/backendApi.js";
import "./Shifts.css";

function Shifts() {
  const [myShifts, setMyShifts] = useState([]);
  const [openShifts, setOpenShifts] = useState([]);
  const [employments, setEmployments] = useState([]);
  const [employmentId, setEmploymentId] = useState("");
  const [message, setMessage] = useState("");

  const loadMyShifts = () => {
    api.get("/shifts/me")
      .then((response) => {
        setMyShifts(response.data);
      })
      .catch((error) => {
        console.error(error);
        setMessage("Could not load your shifts.");
      });
  };

  const loadOpenShifts = () => {
    const from = new Date();

    const to = new Date();
    to.setDate(to.getDate() + 30);

    api.get("/shifts/open", {
        params: {
        from: from.toISOString(),
        to: to.toISOString()
        }
    })
        .then((response) => {
        setOpenShifts(response.data);
        })
        .catch((error) => {
        console.error(error);
        setMessage("Could not load open shifts.");
        });
    };

  const loadEmployments = () => {
    api.get("/employments/me")
      .then((response) => {
        setEmployments(response.data);

        if (response.data.length > 0) {
          setEmploymentId(response.data[0].employmentId);
        }
      })
      .catch((error) => {
        console.error(error);
        setMessage("Could not load your employments.");
      });
  };

  useEffect(() => {
    loadMyShifts();
    loadOpenShifts();
    loadEmployments();
  }, []);

  const offerShift = (shiftId) => {
    const reason = window.prompt(
      "Why are you offering this shift? (Optional)"
    );

    if (reason === null) {
      return;
    }

    api.post(`/shifts/${shiftId}/offer`, {
      reason: reason
    })
      .then(() => {
        setMessage("Shift offered successfully.");
        loadMyShifts();
        loadOpenShifts();
      })
      .catch((error) => {
        console.error(error);
        setMessage("Could not offer shift.");
      });
  };

  const withdrawOffer = (shiftId) => {
    api.delete(`/shifts/${shiftId}/offer`)
      .then(() => {
        setMessage("Shift offer withdrawn.");
        loadMyShifts();
        loadOpenShifts();
      })
      .catch((error) => {
        console.error(error);
        setMessage("Could not withdraw shift offer.");
      });
  };

  const claimShift = (shiftId) => {
    if (!employmentId) {
      setMessage("You do not have an employment available for this shift.");
      return;
    }

    api.post(`/shifts/${shiftId}/claim`, {
      employmentId: employmentId
    })
      .then(() => {
        setMessage("Shift claimed successfully.");
        loadMyShifts();
        loadOpenShifts();
      })
      .catch((error) => {
        console.error(error);
        setMessage("Could not claim shift.");
      });
  };

  return (
    <div className="shifts-page">
      <h1>My Shifts</h1>

      {message && <p>{message}</p>}

      {myShifts.length === 0 ? (
        <p>You do not currently have any shifts.</p>
      ) : (
        myShifts.map((shift) => (
          <div className="shift-card" key={shift.id}>
            <p>
              <strong>Start:</strong>{" "}
              {new Date(shift.startAt).toLocaleString()}
            </p>

            <p>
              <strong>End:</strong>{" "}
              {new Date(shift.endAt).toLocaleString()}
            </p>

            <p>
              <strong>Status:</strong> {shift.status}
            </p>

            {shift.offer?.reason && (
              <p>
                <strong>Reason:</strong> {shift.offer.reason}
              </p>
            )}

            {shift.status === "SCHEDULED" && (
              <button onClick={() => offerShift(shift.id)}>
                Offer Shift
              </button>
            )}

            {shift.status === "OFFERED" && (
              <button onClick={() => withdrawOffer(shift.id)}>
                Withdraw Offer
              </button>
            )}
          </div>
        ))
      )}

      <hr />

      <h1>Open Shifts</h1>

      {employments.length > 1 && (
        <div className="employment-selector">
          <label htmlFor="employment">Job: </label>

          <select
            id="employment"
            value={employmentId}
            onChange={(event) => setEmploymentId(event.target.value)}
          >
            {employments.map((employment) => (
              <option
                key={employment.employmentId}
                value={employment.employmentId}
              >
                {employment.jobTitle}
              </option>
            ))}
          </select>
        </div>
      )}

      {openShifts.length === 0 ? (
        <p>No open shifts are currently available.</p>
      ) : (
        openShifts.map((shift) => (
        <div className="shift-card" key={shift.id}>
            <p>
            <strong>Job:</strong> {shift.jobTitle}
            </p>

            <p>
            <strong>Start:</strong>{" "}
            {new Date(shift.startAt).toLocaleString()}
            </p>

            <p>
            <strong>End:</strong>{" "}
            {new Date(shift.endAt).toLocaleString()}
            </p>

            {shift.reason && (
            <p>
                <strong>Reason:</strong> {shift.reason}
            </p>
            )}

            <p>
            <strong>Eligible:</strong> {shift.eligible ? "Yes" : "No"}
            </p>

            <button
            onClick={() => claimShift(shift.id)}
            disabled={!shift.eligible}
            >
            {shift.eligible ? "Pick Up Shift" : "Not Eligible"}
            </button>
        </div>
        ))
      )}
    </div>
  );
}

export default Shifts;