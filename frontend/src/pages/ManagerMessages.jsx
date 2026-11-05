import { useEffect, useState } from "react";
import api from "../api/backendApi";
import "./ManagerMessages.css";

function ManagerMessages() {
  const [employees, setEmployees] = useState([]);
  const [conversations, setConversations] = useState([]);
  const [selectedConversation, setSelectedConversation] = useState(null);
  const [messages, setMessages] = useState([]);
  const [employeeId, setEmployeeId] = useState("");
  const [body, setBody] = useState("");
  const [loading, setLoading] = useState(true);
  const [loadingMessages, setLoadingMessages] = useState(false);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState("");

  async function loadConversations() {
    const response = await api.get("/manager/messages/conversations");
    setConversations(response.data);
  }

  async function loadMessages(conversation) {
    setSelectedConversation(conversation);
    setLoadingMessages(true);
    setError("");

    try {
      const response = await api.get(
        `/manager/messages/conversations/${conversation.id}`
      );
      setMessages(response.data);
    } catch {
      setError("Unable to load this conversation.");
    } finally {
      setLoadingMessages(false);
    }
  }

  useEffect(() => {
    async function loadInitialData() {
      try {
        const [employeesResponse, conversationsResponse] = await Promise.all([
          api.get("/manager/employees"),
          api.get("/manager/messages/conversations"),
        ]);
        setEmployees(employeesResponse.data);
        setConversations(conversationsResponse.data);
      } catch (err) {
        const status = err.response?.status;
        setError(
          status === 403
            ? "Your account is not authorized to use manager messaging."
            : "Unable to load manager messaging."
        );
      } finally {
        setLoading(false);
      }
    }

    loadInitialData();
  }, []);

  async function handleStartConversation(event) {
    event.preventDefault();
    if (!employeeId) return;

    setError("");
    try {
      const response = await api.post("/manager/messages/conversations", {
        employeeId,
      });
      await loadConversations();
      setEmployeeId("");
      await loadMessages(response.data);
    } catch (err) {
      const status = err.response?.status;
      setError(
        status === 403
          ? "That employee is not on your active roster."
          : "Unable to start the conversation."
      );
    }
  }

  async function handleSend(event) {
    event.preventDefault();
    if (!selectedConversation || !body.trim()) return;

    setSending(true);
    setError("");
    try {
      const response = await api.post(
        `/manager/messages/conversations/${selectedConversation.id}`,
        { body }
      );
      setMessages((current) => [...current, response.data]);
      setBody("");
      await loadConversations();
    } catch {
      setError("Unable to send the message.");
    } finally {
      setSending(false);
    }
  }

  return (
    <section className="manager-messages">
      <div className="manager-messages__heading">
        <h2>Messages</h2>
        <p>Send important work information to employees on your roster.</p>
      </div>

      {error && <p className="manager-messages__error" role="alert">{error}</p>}

      <form className="manager-messages__new" onSubmit={handleStartConversation}>
        <label htmlFor="message-employee">Start a conversation</label>
        <select
          id="message-employee"
          value={employeeId}
          onChange={(event) => setEmployeeId(event.target.value)}
          disabled={loading}
        >
          <option value="">Select an employee</option>
          {employees.map((employee) => (
            <option key={employee.userId} value={employee.userId}>
              {employee.name} ({employee.email})
            </option>
          ))}
        </select>
        <button type="submit" disabled={!employeeId}>Open conversation</button>
      </form>

      <div className="manager-messages__layout">
        <aside className="manager-messages__conversations">
          <h3>Conversations</h3>
          {loading ? (
            <p>Loading...</p>
          ) : conversations.length === 0 ? (
            <p>No conversations yet.</p>
          ) : conversations.map((conversation) => (
            <button
              type="button"
              className={selectedConversation?.id === conversation.id ? "selected" : ""}
              key={conversation.id}
              onClick={() => loadMessages(conversation)}
            >
              <strong>{conversation.employeeName}</strong>
              <span>{conversation.lastMessage || "No messages yet"}</span>
            </button>
          ))}
        </aside>

        <section className="manager-messages__thread">
          {selectedConversation ? (
            <>
              <h3>{selectedConversation.employeeName}</h3>
              <div className="manager-messages__history">
                {loadingMessages ? (
                  <p>Loading messages...</p>
                ) : messages.length === 0 ? (
                  <p>No messages yet. Send the first message.</p>
                ) : messages.map((message) => (
                  <article key={message.id} className="manager-message">
                    <strong>{message.senderName}</strong>
                    <p>{message.body}</p>
                    <small>{new Date(message.sentAt).toLocaleString()}</small>
                  </article>
                ))}
              </div>
              <form className="manager-messages__composer" onSubmit={handleSend}>
                <textarea
                  value={body}
                  onChange={(event) => setBody(event.target.value)}
                  placeholder="Write a message..."
                  maxLength={2000}
                  required
                />
                <button type="submit" disabled={sending || !body.trim()}>
                  {sending ? "Sending..." : "Send"}
                </button>
              </form>
            </>
          ) : (
            <p>Select a conversation to view messages.</p>
          )}
        </section>
      </div>
    </section>
  );
}

export default ManagerMessages;
