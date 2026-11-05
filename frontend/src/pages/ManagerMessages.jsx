import { useEffect, useState } from "react";
import api from "../api/backendApi";
import ConversationList from "../components/ConversationList";
import MessageThread from "../components/MessageThread";
import "./ManagerMessages.css";

function ManagerMessages() {
  const [employees, setEmployees] = useState([]);
  const [conversations, setConversations] = useState([]);
  const [groups, setGroups] = useState([]);
  const [selectedConversation, setSelectedConversation] = useState(null);
  const [messages, setMessages] = useState([]);
  const [employeeId, setEmployeeId] = useState("");
  const [groupName, setGroupName] = useState("");
  const [groupEmployeeIds, setGroupEmployeeIds] = useState([]);
  const [creatingGroup, setCreatingGroup] = useState(false);
  const [body, setBody] = useState("");
  const [loading, setLoading] = useState(true);
  const [loadingMessages, setLoadingMessages] = useState(false);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState("");

  async function loadConversations() {
    const [conversationResponse, groupResponse] = await Promise.all([
      api.get("/manager/messages/conversations"),
      api.get("/manager/messages/groups"),
    ]);
    setConversations(conversationResponse.data);
    setGroups(groupResponse.data);
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
        const [employeesResponse, conversationsResponse, groupsResponse] = await Promise.all([
          api.get("/manager/employees"),
          api.get("/manager/messages/conversations"),
          api.get("/manager/messages/groups"),
        ]);
        setEmployees(employeesResponse.data);
        setConversations(conversationsResponse.data);
        setGroups(groupsResponse.data);
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

  async function handleCreateGroup(event) {
    event.preventDefault();
    if (!groupName.trim() || groupEmployeeIds.length === 0) return;
    setCreatingGroup(true);
    setError("");
    try {
      const response = await api.post("/manager/messages/groups", {
        name: groupName,
        employeeIds: groupEmployeeIds,
      });
      await loadConversations();
      setGroupName("");
      setGroupEmployeeIds([]);
      await loadMessages(response.data);
    } catch {
      setError("Unable to create the group conversation.");
    } finally {
      setCreatingGroup(false);
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

    <form className="manager-messages__new" onSubmit={handleCreateGroup}>
      <label htmlFor="group-name">Create a group chat</label>
      <input id="group-name" value={groupName}
        onChange={(event) => setGroupName(event.target.value)}
        placeholder="Group name" maxLength={100} required />
      <select multiple value={groupEmployeeIds}
        onChange={(event) => setGroupEmployeeIds(
          Array.from(event.target.selectedOptions, (option) => option.value)
        )} required>
        {employees.map((employee) => (
          <option key={employee.userId} value={employee.userId}>{employee.name}</option>
        ))}
      </select>
      <button type="submit" disabled={creatingGroup || groupEmployeeIds.length === 0}>
        {creatingGroup ? "Creating..." : "Create group"}
      </button>
    </form>

      <div className="messaging-ui__layout">
        <ConversationList conversations={conversations} groups={groups}
          selectedId={selectedConversation?.id} onSelect={loadMessages} />
        <MessageThread conversation={selectedConversation} messages={messages}
          loading={loadingMessages} body={body} setBody={setBody}
          onSend={handleSend} sending={sending} />
      </div>
    </section>
  );
}

export default ManagerMessages;
