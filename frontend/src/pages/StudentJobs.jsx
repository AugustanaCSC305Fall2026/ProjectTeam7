import { useEffect, useState } from "react";
import api from "../api/backendApi";
import StudentLayout from "../components/StudentLayout";
import "./StudentJobs.css";

function StudentJobs() {
  const [jobs, setJobs] = useState([]);
  const [applications, setApplications] = useState([]);
  const [statements, setStatements] = useState({});
  const [appliedJobIds, setAppliedJobIds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState("");

  useEffect(() => {
    Promise.all([api.get("/jobs/open"), api.get("/jobs/applications")])
      .then(([jobsResponse, applicationsResponse]) => {
        setJobs(jobsResponse.data);
        setApplications(applicationsResponse.data);
        setAppliedJobIds(
          applicationsResponse.data.map((application) => application.jobId)
        );
      })
      .catch((error) => {
        console.error(error);
        setMessage("Could not load your jobs and applications.");
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
      const applicationsResponse = await api.get("/jobs/applications");
      setApplications(applicationsResponse.data);
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
    >
      {message && (
        <p className="student-jobs__message" role="status">
          {message}
        </p>
      )}

      <section className="student-jobs__applications" aria-labelledby="my-applications-heading">
        <div className="student-jobs__section-heading">
          <h2 id="my-applications-heading">My Applications</h2>
          <p>Track the jobs you have applied to and their current status.</p>
        </div>

        {loading ? (
          <p className="student-jobs__state">Loading your applications…</p>
        ) : applications.length === 0 ? (
          <p className="student-jobs__state">
            You have not applied to any jobs yet.
          </p>
        ) : (
          <div className="student-jobs__application-list">
            {applications.map((application) => (
              <article
                className="student-application-card"
                key={application.applicationId}
              >
                <div>
                  <h3>{application.jobTitle}</h3>
                  <p className="student-application-card__department">
                    {application.department}
                  </p>
                </div>
                <span
                  className={`student-application-card__status student-application-card__status--${application.status.toLowerCase()}`}
                >
                  {application.status}
                </span>
                {application.applicationStatement && (
                  <p className="student-application-card__statement">
                    {application.applicationStatement}
                  </p>
                )}
              </article>
            ))}
          </div>
        )}
      </section>

      <div className="student-jobs__section-heading student-jobs__available-heading">
        <h2>Available Jobs</h2>
        <p>Browse open campus jobs and submit an application.</p>
      </div>

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
