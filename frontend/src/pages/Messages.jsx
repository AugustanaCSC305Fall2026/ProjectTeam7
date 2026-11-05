import { useCallback, useState } from "react";
import api from "../api/backendApi";
import ConversationList from "../components/ConversationList";
import ContactPicker from "../components/ContactPicker";
import MessageThread from "../components/MessageThread";
import StudentLayout from "../components/StudentLayout";
import SupervisorLayout from "../components/SupervisorLayout";
import useMessagingConversation from "../components/useMessagingConversation";
import "./Messages.css";

function Messages({ supervisorMode = false }) {
  const [contacts, setContacts] = useState([]);
  const [conversations, setConversations] = useState([]);
  const [recipientId, setRecipientId] = useState("");
  const [showNew, setShowNew] = useState(false);

  const loadLists = useCallback(async () => {
    const [contactsResponse, conversationsResponse] = await Promise.all([
      api.get("/messages/contacts"),
      api.get("/messages/conversations"),
    ]);
    setContacts(contactsResponse.data);
    setConversations(conversationsResponse.data);
  }, []);

  const messaging = useMessagingConversation({
    loadLists,
    messagesUrl: (conversationId) => `/messages/conversations/${conversationId}`,
    sendUrl: (conversationId) => `/messages/conversations/${conversationId}`,
    loadErrorMessage: "We could not load your messages. Check your connection and try again.",
  });

  async function handleStart(event) {
    event.preventDefault();
    if (!recipientId) return;
    try {
      const response = await api.post("/messages/conversations", { recipientId });
      await loadLists();
      setRecipientId("");
      setShowNew(false);
      await messaging.openConversation(response.data);
    } catch (requestError) {
      messaging.setError(requestError.response?.status === 403
        ? "You cannot message that person with your current work relationship."
        : "We could not start that conversation. Try again.");
    }
  }

  const Layout = supervisorMode ? SupervisorLayout : StudentLayout;
  const layoutProps = supervisorMode
    ? {}
    : { title: "Student", subtitle: "Stay connected with your campus team." };

  return (
    <Layout {...layoutProps}>
      <section className="messages-page messaging-page">
      <div className="messaging-page__heading">
        <div>
          <h2>Messages</h2>
          <p>{supervisorMode
            ? "Message managers, employees, and campus staff."
            : "Contact your coworkers, managers, and supervisors."}</p>
        </div>
      </div>

      {messaging.error && (
        <div className="messaging-page__alert" role="alert" aria-live="assertive">
          <span className="messaging-page__alert-icon" aria-hidden="true">!</span>
          <span>{messaging.error}</span>
          <button type="button" onClick={messaging.loadData}>Try again</button>
        </div>
      )}

      <div className={`messaging-ui__layout ${messaging.selectedConversation ? "has-selection" : ""}`}>
        <ConversationList
          conversations={conversations}
          contacts={contacts}
          selectedId={messaging.selectedConversation?.id}
          onSelect={messaging.openConversation}
          onNew={() => setShowNew(true)}
          loading={messaging.loading}
          newPanel={showNew && (
            <form className="messaging-ui__new-panel" onSubmit={handleStart}>
              <div className="messaging-ui__new-panel-heading">
                <strong>New conversation</strong>
                <button type="button" onClick={() => setShowNew(false)} aria-label="Close new conversation">×</button>
              </div>
              <label htmlFor="message-recipient">Choose a person</label>
              <ContactPicker contacts={contacts} value={recipientId} onChange={setRecipientId}
                disabled={messaging.loading || Boolean(messaging.error)} id="message-recipient" />
              <button className="messaging-ui__primary-action" type="submit" disabled={!recipientId || messaging.loading || Boolean(messaging.error)}>
                Start conversation
              </button>
            </form>
          )}
        />
        <MessageThread
          conversation={messaging.selectedConversation}
          messages={messaging.messages}
          loading={messaging.loadingMessages}
          body={messaging.body}
          setBody={messaging.setBody}
          onSend={messaging.handleSend}
          sending={messaging.sending}
          sendError={messaging.sendError}
          onRetry={() => messaging.sendMessage(messaging.failedBody)}
          onBack={() => messaging.setSelectedConversation(null)}
        />
      </div>
      </section>
    </Layout>
  );
}

export default Messages;
