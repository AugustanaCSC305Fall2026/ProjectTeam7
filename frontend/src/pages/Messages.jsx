import { useEffect, useState } from "react";
import api from "../api/backendApi";
import "./Messages.css";

function Messages() {
  const [contacts, setContacts] = useState([]);
  const [conversations, setConversations] = useState([]);
  const [selectedConversation, setSelectedConversation] = useState(null);
  const [messages, setMessages] = useState([]);
  const [recipientId, setRecipientId] = useState("");
  const [body, setBody] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  async function refreshConversations() {
    const response = await api.get("/messages/conversations");
    setConversations(response.data);
  }

  async function openConversation(conversation) {
    setSelectedConversation(conversation);
    setError("");
    try {
      const response = await api.get(`/messages/conversations/${conversation.id}`);
      setMessages(response.data);
    } catch {
      setError("Unable to load this conversation.");
    }
  }

  useEffect(() => {
    async function loadData() {
      try {
        const [contactsResponse, conversationsResponse] = await Promise.all([
          api.get("/messages/contacts"),
          api.get("/messages/conversations"),
        ]);
        setContacts(contactsResponse.data);
        setConversations(conversationsResponse.data);
      } catch {
        setError("Unable to load messaging.");
      } finally {
        setLoading(false);
      }
    }
    loadData();
  }, []);

  async function handleStart(event) {
    event.preventDefault();
    if (!recipientId) return;
    try {
      const response = await api.post("/messages/conversations", { recipientId });
      await refreshConversations();
      setRecipientId("");
      await openConversation(response.data);
    } catch (err) {
      setError(err.response?.status === 403
        ? "You cannot message that user."
        : "Unable to start the conversation.");
    }
  }

  async function handleSend(event) {
    event.preventDefault();
    if (!selectedConversation || !body.trim()) return;
    try {
      const response = await api.post(
        `/messages/conversations/${selectedConversation.id}`,
        { body }
      );
      setMessages((current) => [...current, response.data]);
      setBody("");
      await refreshConversations();
    } catch {
      setError("Unable to send the message.");
    }
  }

  return (
    <section className="messages-page">
      <div className="messages-page__heading">
        <h2>Messages</h2>
        <p>Communicate with your coworkers, managers, and supervisors.</p>
      </div>
      {error && <p className="messages-page__error" role="alert">{error}</p>}

      <form className="messages-page__new" onSubmit={handleStart}>
        <label htmlFor="message-recipient">New conversation</label>
        <select id="message-recipient" value={recipientId}
          onChange={(event) => setRecipientId(event.target.value)} disabled={loading}>
          <option value="">Select a person</option>
          {contacts.map((contact) => (
            <option key={contact.id} value={contact.id}>
              {contact.name} — {contact.relationship}
            </option>
          ))}
        </select>
        <button type="submit" disabled={!recipientId}>Open conversation</button>
      </form>

      <div className="messages-page__layout">
        <aside className="messages-page__conversations">
          <h3>Conversations</h3>
          {conversations.length === 0 ? <p>No conversations yet.</p> : conversations.map((conversation) => (
            <button type="button" key={conversation.id}
              className={selectedConversation?.id === conversation.id ? "selected" : ""}
              onClick={() => openConversation(conversation)}>
              <strong>{conversation.otherUserName}</strong>
              <span>{conversation.lastMessage || "No messages yet"}</span>
            </button>
          ))}
        </aside>

        <section className="messages-page__thread">
          {selectedConversation ? (
            <>
              <h3>{selectedConversation.otherUserName}</h3>
              <div className="messages-page__history">
                {messages.length === 0 ? <p>No messages yet. Send the first message.</p> : messages.map((message) => (
                  <article className="messages-page__message" key={message.id}>
                    <strong>{message.senderName}</strong>
                    <p>{message.body}</p>
                    <small>{new Date(message.sentAt).toLocaleString()}</small>
                  </article>
                ))}
              </div>
              <form className="messages-page__composer" onSubmit={handleSend}>
                <textarea value={body} onChange={(event) => setBody(event.target.value)}
                  placeholder="Write a message..." maxLength={2000} required />
                <button type="submit" disabled={!body.trim()}>Send</button>
              </form>
            </>
          ) : <p>Select a conversation to view messages.</p>}
        </section>
      </div>
    </section>
  );
}

export default Messages;
