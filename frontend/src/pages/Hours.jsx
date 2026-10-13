import React, { useEffect, useState } from "react";
import backendApi from "../api/backendApi";
import "./Hours.css";

function Hours() {
    const [entries, setEntries] = useState([]);
    const [totalMinutes, setTotalMinutes] = useState(0);
    const [selectedJobId, setSelectedJobId] = useState("");
    const [jobs, setJobs] = useState([]);
    const [error, setError] = useState("");

    const loadHours = async (jobId = "") => {
        try {
            setError("");

            const response = await backendApi.get("/hours", {
                params: jobId ? { jobId } : {}
            });

            setEntries(response.data.entries);
            setTotalMinutes(response.data.totalMinutes);

            // Build the job filter from the unfiltered response.
            if (!jobId) {
                const uniqueJobs = [];

                response.data.entries.forEach((entry) => {
                    if (!uniqueJobs.some((job) => job.id === entry.jobId)) {
                        uniqueJobs.push({
                            id: entry.jobId,
                            title: entry.jobTitle
                        });
                    }
                });

                setJobs(uniqueJobs);
            }
        } catch (err) {
            console.error(err);
            setError("Unable to load hours.");
        }
    };

    useEffect(() => {
        loadHours();
    }, []);

    const handleJobChange = (event) => {
        const jobId = event.target.value;
        setSelectedJobId(jobId);
        loadHours(jobId);
    };

    const formatHours = (minutes) => {
        const hours = Math.floor(minutes / 60);
        const remainingMinutes = minutes % 60;

        return `${hours}h ${remainingMinutes}m`;
    };

    const formatDate = (date) => {
        if (!date) {
            return "—";
        }

        return new Date(date).toLocaleString();
    };

    return (
        <div className="hours-page">
            <h1>My Hours</h1>

            <div className="hours-filter">
                <label htmlFor="job-filter">Filter by job: </label>

                <select
                    id="job-filter"
                    value={selectedJobId}
                    onChange={handleJobChange}
                >
                    <option value="">All Jobs</option>

                    {jobs.map((job) => (
                        <option key={job.id} value={job.id}>
                            {job.title}
                        </option>
                    ))}
                </select>
            </div>

            <div className="hours-total">
                <h2>Total Hours</h2>
                <p>{formatHours(totalMinutes)}</p>
            </div>

            {error && <p>{error}</p>}

            {entries.length === 0 && !error ? (
                <p>No time entries found.</p>
            ) : (
                <table className="hours-table">
                    <thead>
                        <tr>
                            <th>Job</th>
                            <th>Clock In</th>
                            <th>Clock Out</th>
                            <th>Break</th>
                            <th>Worked</th>
                        </tr>
                    </thead>

                    <tbody>
                        {entries.map((entry) => (
                            <tr key={entry.id}>
                                <td>{entry.jobTitle}</td>
                                <td>{formatDate(entry.clockInAt)}</td>
                                <td>{formatDate(entry.clockOutAt)}</td>
                                <td>{entry.breakMinutes} min</td>
                                <td>{formatHours(entry.workedMinutes)}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}
        </div>
    );
}

export default Hours;