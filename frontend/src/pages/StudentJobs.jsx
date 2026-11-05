import { useEffect, useState } from "react";
import api from "../api/backendApi";
import StudentLayout from "../components/StudentLayout";

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
      setMessage(
        error.response?.data?.message || "Could not submit your application."
      );
    }
  };

  return (
    <StudentLayout
      title="Available Jobs"
      subtitle="Browse open campus jobs and submit an application."
      showComingSoon={false}
    >
      {message && <p role="status">{message}</p>}

      {loading ? (
        <p>Loading available jobs…</p>
      ) : jobs.length === 0 ? (
        <p>No jobs are currently accepting applications.</p>
      ) : (
        <div>
          {jobs.map((job) => {
            const alreadyApplied = appliedJobIds.includes(job.id);

            return (
              <article key={job.id}>
                <h2>{job.title}</h2>
                <p>{job.department}</p>
                <p>{job.description}</p>

                <label>
                  Application statement (optional)
                  <textarea
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