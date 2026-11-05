import { useCallback, useState } from "react";
import api from "../api/backendApi";
import ConversationList from "../components/ConversationList";
import ContactPicker from "../components/ContactPicker";
import MessageThread from "../components/MessageThread";
import useMessagingConversation from "../components/useMessagingConversation";
import "./ManagerMessages.css";

const managerMessagesUrl = (conversationId) => `/manager/messages/conversations/${conversationId}`;

function ManagerMessages() {
  const [employees, setEmployees] = useState([]);
  const [conversations, setConversations] = useState([]);
  const [groups, setGroups] = useState([]);
  const [employeeId, setEmployeeId] = useState("");
  const [groupName, setGroupName] = useState("");
  const [groupEmployeeIds, setGroupEmployeeIds] = useState([]);
  const [showNew, setShowNew] = useState(false);
  const [creatingGroup, setCreatingGroup] = useState(false);

  const loadLists = useCallback(async () => {
    const [employeesResponse, conversationsResponse, groupsResponse] = await Promise.all([
      api.get("/manager/employees"),
      api.get("/manager/messages/conversations"),
      api.get("/manager/messages/groups"),
    ]);
    setEmployees(employeesResponse.data);
    setConversations(conversationsResponse.data);
    setGroups(groupsResponse.data);
  }, []);

  const loadErrorMessage = useCallback((requestError) => requestError.response?.status === 403
    ? "Your account is not authorized to use manager messaging."
    : "We could not load manager messages. Check your connection and try again.", []);

  const messaging = useMessagingConversation({
    loadLists,
    messagesUrl: managerMessagesUrl,
    sendUrl: managerMessagesUrl,
    loadErrorMessage,
  });

  async function handleStartConversation(event) {
    event.preventDefault();
    if (!employeeId) return;
    try {
      const response = await api.post("/manager/messages/conversations", { employeeId });
      await loadLists();
      setEmployeeId("");
      setShowNew(false);
      await messaging.openConversation(response.data);
    } catch (requestError) {
      messaging.setError(requestError.response?.status === 403
        ? "That employee is not on your active roster."
        : "We could not start that conversation. Try again.");
    }
  }

  async function handleCreateGroup(event) {
    event.preventDefault();
    if (!groupName.trim() || groupEmployeeIds.length === 0) return;
    setCreatingGroup(true);
    try {
      const response = await api.post("/manager/messages/groups", {
        name: groupName,
        employeeIds: groupEmployeeIds,
      });
      await loadLists();
      setGroupName("");
      setGroupEmployeeIds([]);
      setShowNew(false);
      await messaging.openConversation(response.data);
    } catch {
      messaging.setError("We could not create the group conversation. Try again.");
    } finally {
      setCreatingGroup(false);
    }
  }

  const newPanel = showNew && (
    <div className="messaging-ui__new-panel manager-messages__new-panel">
      <div className="messaging-ui__new-panel-heading">
        <strong>Start something new</strong>
        <button type="button" onClick={() => setShowNew(false)} aria-label="Close new conversation">×</button>
      </div>
      <form onSubmit={handleStartConversation}>
        <label htmlFor="message-employee">Direct message</label>
        <ContactPicker contacts={employees.map((employee) => ({ id: employee.id, name: employee.name, email: employee.email, relationship: "Employee" }))}
          value={employeeId} onChange={setEmployeeId} disabled={messaging.loading} id="message-employee" />
        <button className="messaging-ui__primary-action" type="submit" disabled={!employeeId || messaging.loading}>Start direct message</button>
      </form>
      <div className="messaging-ui__new-panel-or" aria-hidden="true"><span>OR</span></div>
      <form onSubmit={handleCreateGroup}>
        <div className="manager-messages__form-title">
          <strong>Group chat</strong>
          <span>Message several employees at once.</span>
        </div>
        <input id="group-name" value={groupName} onChange={(event) => setGroupName(event.target.value)} placeholder="Group name" maxLength={100} required />
        <label htmlFor="group-members">Choose members</label>
        <ContactPicker
          contacts={employees.map((employee) => ({ id: employee.id, name: employee.name, email: employee.email, relationship: "Employee" }))}
          value={groupEmployeeIds}
          onChange={setGroupEmployeeIds}
          disabled={messaging.loading}
          id="group-members"
          multiple
        />
        <small className="manager-messages__selection-hint">
          {groupEmployeeIds.length === 0 ? "Select one or more employees." : `${groupEmployeeIds.length} employee${groupEmployeeIds.length === 1 ? "" : "s"} selected.`}
        </small>
        <button className="messaging-ui__primary-action" type="submit" disabled={creatingGroup || groupEmployeeIds.length === 0}>
          {creatingGroup ? "Creating..." : "Create group chat"}
        </button>
      </form>
    </div>
  );

  return (
    <section className="manager-messages messaging-page">
      <div className="messaging-page__heading">
        <h2>Messages</h2>
        <p>Message employees directly or keep your team in sync with a group chat.</p>
      </div>
      {messaging.error && (
        <div className="messaging-page__alert" role="alert" aria-live="assertive">
          <span className="messaging-page__alert-icon" aria-hidden="true">!</span>
          <span>{messaging.error}</span>
          <button type="button" onClick={messaging.loadData}>Try again</button>
        </div>
      )}
      <div className={`messaging-ui__layout ${messaging.selectedConversation ? "has-selection" : ""}`}>
        <ConversationList conversations={conversations} groups={groups} contacts={employees.map((employee) => ({ id: employee.id, name: employee.name, relationship: "Employee" }))}
          selectedId={messaging.selectedConversation?.id} onSelect={messaging.openConversation} onNew={() => setShowNew(true)} loading={messaging.loading} newPanel={newPanel} />
        <MessageThread conversation={messaging.selectedConversation} messages={messaging.messages} loading={messaging.loadingMessages}
          body={messaging.body} setBody={messaging.setBody} onSend={messaging.handleSend} sending={messaging.sending} sendError={messaging.sendError}
          onRetry={() => messaging.sendMessage(messaging.failedBody)} onBack={() => messaging.setSelectedConversation(null)} />
      </div>
    </section>
  );
}

export default ManagerMessages;
