import "./MessagingUI.css";

function ConversationList({ conversations, groups = [], selectedId, onSelect }) {
  return (
    <aside className="messaging-ui__conversations">
      <h3>Conversations</h3>
      {conversations.length === 0 && groups.length === 0 ? (
        <p>No conversations yet.</p>
      ) : conversations.map((conversation) => (
        <button
          type="button"
          className={selectedId === conversation.id ? "selected" : ""}
          key={conversation.id}
          onClick={() => onSelect(conversation)}
        >
          <strong>{conversation.employeeName || conversation.otherUserName}</strong>
          <span>{conversation.lastMessage || "No messages yet"}</span>
        </button>
      ))}
      {groups.length > 0 && <h3>Group chats</h3>}
      {groups.map((group) => (
        <button
          type="button"
          className={selectedId === group.id ? "selected" : ""}
          key={group.id}
          onClick={() => onSelect(group)}
        >
          <strong>{group.name}</strong>
          <span>{group.lastMessage || `${group.memberCount} members`}</span>
        </button>
      ))}
    </aside>
  );
}

export default ConversationList;
