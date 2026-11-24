import { useEffect, useState } from "react";
import api from "../api/backendApi";
import StudentLayout from "../components/StudentLayout";
import "./StudentJobs.css";

function StudentJobs() {
  const [jobs, setJobs] = useState([]);
  const [statements, setStatements] = useState({});
  const [appliedJobIds, setAppliedJobIds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState("");

  useEffect(() => {
    api.get("/jobs/open")
      .then((response) => setJobs(response.data))
      .catch((error) => {
        console.error(error);
        setMessage("Could not load available jobs.");
      })
      .finally(() => setLoading(false));
  }, []);

  const applyToJob = async (jobId) => {
    setMessage("");

    try {
      await api.post(`/jobs/${jobId}/applications`, {
        applicationStatement: statements[jobId] || "",
      });

      setAppliedJobIds((currentIds) => [...currentIds, jobId]);
      setMessage("Your application was submitted.");
    } catch (error) {
      console.error(error);
      if (error.response?.status === 409) {
        setAppliedJobIds((currentIds) =>
          currentIds.includes(jobId) ? currentIds : [...currentIds, jobId]
        );
        setMessage("You have already applied to this job.");
      } else {
        setMessage("Could not submit your application.");
      }
    }
  };

  return (
    <StudentLayout
      title="Available Jobs"
      subtitle="Browse open campus jobs and submit an application."
    >
      {message && (
        <p className="student-jobs__message" role="status">
          {message}
        </p>
      )}

      {loading ? (
        <p className="student-jobs__state">Loading available jobs…</p>
      ) : jobs.length === 0 ? (
        <p className="student-jobs__state">
          No jobs are currently accepting applications.
        </p>
      ) : (
        <div className="student-jobs__list">
          {jobs.map((job) => {
            const alreadyApplied = appliedJobIds.includes(job.id);

            return (
              <article className="student-job-card" key={job.id}>
                <h2 className="student-job-card__title">{job.title}</h2>
                <p className="student-job-card__department">
                  {job.department}
                </p>
                <p className="student-job-card__description">
                  {job.description}
                </p>

                <label className="student-job-card__statement">
                  Application statement (optional)
                  <textarea
                    className="student-job-card__textarea"
                    rows={4}
                    value={statements[job.id] || ""}
                    onChange={(event) =>
                      setStatements((current) => ({
                        ...current,
                        [job.id]: event.target.value,
                      }))
                    }
                    disabled={alreadyApplied}
                  />
                </label>

                <button
                  className="student-job-card__apply"
                  type="button"
                  onClick={() => applyToJob(job.id)}
                  disabled={alreadyApplied}
                >
                  {alreadyApplied ? "Applied" : "Apply"}
                </button>
              </article>
            );
          })}
        </div>
      )}
    </StudentLayout>
  );
}

export default StudentJobs;
