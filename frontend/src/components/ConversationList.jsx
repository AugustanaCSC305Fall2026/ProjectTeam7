import { useMemo, useState } from "react";
import "./MessagingUI.css";

function ConversationList({
  conversations,
  groups = [],
  contacts = [],
  selectedId,
  onSelect,
  onNew,
  newPanel,
  loading = false,
}) {
  const [query, setQuery] = useState("");
  const contactById = new Map(contacts.map((contact) => [contact.id, contact]));
  const allConversations = useMemo(() => [
    ...conversations.map((conversation) => ({
      ...conversation,
      kind: conversation.type === "GROUP" ? "group" : "direct",
    })),
    ...groups.map((conversation) => ({ ...conversation, kind: "group" })),
  ], [conversations, groups]);
  const filteredConversations = allConversations.filter((conversation) => {
    const name = conversation.employeeName || conversation.otherUserName || conversation.name || "";
    return name.toLowerCase().includes(query.trim().toLowerCase());
  });

  const getName = (conversation) => conversation.employeeName
    || conversation.otherUserName || conversation.name || "Conversation";
  const getRole = (conversation) => conversation.kind === "group"
    ? `${conversation.memberCount || 0} members`
    : contactById.get(conversation.employeeId || conversation.otherUserId)?.relationship || "Contact";
  const getTime = (conversation) => conversation.updatedAt
    ? new Date(conversation.updatedAt).toLocaleTimeString([], { hour: "numeric", minute: "2-digit" })
    : "";

  return (
    <aside className="messaging-ui__conversations" aria-label="Conversations">
      <div className="messaging-ui__conversations-heading">
        <div>
          <p className="messaging-ui__eyebrow">INBOX</p>
          <h3>Conversations</h3>
        </div>
        <button className="messaging-ui__new-button" type="button" onClick={onNew}>
          <span aria-hidden="true">+</span> New
        </button>
      </div>

      {newPanel}

      <label className="messaging-ui__search">
        <span className="messaging-ui__search-icon" aria-hidden="true">⌕</span>
        <span className="sr-only">Search conversations</span>
        <input
          type="search"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder="Search conversations..."
        />
      </label>

      <div className="messaging-ui__conversation-meta">
        <span>All conversations</span>
        <span>{allConversations.length}</span>
      </div>

      <div className="messaging-ui__conversation-list">
        {loading ? (
          [1, 2, 3].map((item) => (
            <div className="messaging-ui__skeleton-row" key={item}>
              <span className="messaging-ui__skeleton messaging-ui__skeleton-avatar" />
              <span className="messaging-ui__skeleton-copy">
                <span className="messaging-ui__skeleton messaging-ui__skeleton-name" />
                <span className="messaging-ui__skeleton messaging-ui__skeleton-preview" />
              </span>
            </div>
          ))
        ) : filteredConversations.length === 0 ? (
          <div className="messaging-ui__empty-list">
            <span className="messaging-ui__empty-icon" aria-hidden="true">✦</span>
            <strong>{query ? "No matches found" : "No conversations yet"}</strong>
            <p>{query ? "Try a different search." : "Start a new conversation to get started."}</p>
            {!query && <button type="button" onClick={onNew}>Start a conversation</button>}
          </div>
        ) : filteredConversations.map((conversation) => {
          const name = getName(conversation);
          const unreadCount = conversation.unreadCount || 0;
          return (
            <button
              type="button"
              className={`messaging-ui__conversation ${selectedId === conversation.id ? "selected" : ""}`}
              key={conversation.id}
              onClick={() => onSelect(conversation)}
              aria-current={selectedId === conversation.id ? "true" : undefined}
            >
              <span className={`messaging-ui__avatar ${conversation.kind === "group" ? "group" : ""}`} aria-hidden="true">
                {conversation.kind === "group" ? "#" : name.charAt(0).toUpperCase()}
              </span>
              <span className="messaging-ui__conversation-copy">
                <span className="messaging-ui__conversation-topline">
                  <strong>{name}</strong>
                  <time>{getTime(conversation)}</time>
                </span>
                <span className="messaging-ui__conversation-bottomline">
                  <span>{getRole(conversation)}</span>
                  <span className="messaging-ui__conversation-preview">{conversation.lastMessage || "No messages yet"}</span>
                  {unreadCount > 0 && <b className="messaging-ui__unread">{unreadCount}</b>}
                </span>
              </span>
            </button>
          );
        })}
      </div>
    </aside>
  );
}

export default ConversationList;
