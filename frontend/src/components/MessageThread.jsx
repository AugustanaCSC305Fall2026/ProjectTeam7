import "./MessagingUI.css";

function MessageThread({
  conversation,
  messages,
  loading,
  body,
  setBody,
  onSend,
  sending,
  sendError,
  onRetry,
  onBack,
}) {
  let currentUserId = "";
  try {
    currentUserId = JSON.parse(localStorage.getItem("user") || "null")?.id || "";
  } catch {
    currentUserId = "";
  }

  if (!conversation) {
    return (
      <section className="messaging-ui__thread messaging-ui__thread--empty" aria-label="Message thread">
        <div className="messaging-ui__welcome-icon" aria-hidden="true">✦</div>
        <h3>Your messages</h3>
        <p>Select a conversation to read messages and reply.</p>
      </section>
    );
  }

  const title = conversation.employeeName || conversation.otherUserName || conversation.name || "Conversation";
  const subtitle = conversation.employeeEmail || conversation.otherUserEmail
    || (conversation.memberCount ? `${conversation.memberCount} members` : "Direct conversation");
  const groupedMessages = messages.reduce((groups, message) => {
    const day = new Date(message.sentAt).toLocaleDateString([], {
      weekday: "long", month: "short", day: "numeric",
    });
    if (!groups[day]) groups[day] = [];
    groups[day].push(message);
    return groups;
  }, {});

  function handleKeyDown(event) {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault();
      event.currentTarget.form.requestSubmit();
    }
  }

  return (
    <section className="messaging-ui__thread" aria-label={`Conversation with ${title}`}>
      <header className="messaging-ui__thread-heading">
        {onBack && <button className="messaging-ui__back-button" type="button" onClick={onBack}>← <span>Back</span></button>}
        <span className={`messaging-ui__avatar messaging-ui__avatar--large ${conversation.memberCount ? "group" : ""}`} aria-hidden="true">
          {conversation.memberCount ? "#" : title.charAt(0).toUpperCase()}
        </span>
        <div className="messaging-ui__thread-title">
          <h3>{title}</h3>
          <p>{subtitle}</p>
        </div>
        <button className="messaging-ui__more-button" type="button" aria-label="Conversation options">•••</button>
      </header>

      <div className="messaging-ui__history" aria-live="polite">
        {loading ? (
          <div className="messaging-ui__message-skeletons" aria-label="Loading messages">
            <span /><span /><span />
          </div>
        ) : messages.length === 0 ? (
          <div className="messaging-ui__thread-empty">
            <span className="messaging-ui__empty-icon" aria-hidden="true">✦</span>
            <strong>No messages yet</strong>
            <p>Send the first message to start the conversation.</p>
          </div>
        ) : Object.entries(groupedMessages).map(([day, dayMessages]) => (
          <div className="messaging-ui__day" key={day}>
            <div className="messaging-ui__day-label"><span>{day}</span></div>
            {dayMessages.map((message) => {
              const own = message.senderId === currentUserId;
              return (
                <article className={`messaging-ui__message ${own ? "own" : ""}`} key={message.id}>
                  <div className="messaging-ui__message-meta">
                    <strong>{own ? "You" : message.senderName}</strong>
                    <time>{new Date(message.sentAt).toLocaleTimeString([], { hour: "numeric", minute: "2-digit" })}</time>
                  </div>
                  <p>{message.body}</p>
                </article>
              );
            })}
          </div>
        ))}
      </div>

      {sendError && (
        <div className="messaging-ui__send-error" role="alert" aria-live="assertive">
          <span aria-hidden="true">!</span>
          <span>{sendError}</span>
          <button type="button" onClick={onRetry}>Try again</button>
        </div>
      )}
      <form className="messaging-ui__composer" onSubmit={onSend}>
        <label className="sr-only" htmlFor="message-composer">Type a message</label>
        <textarea
          id="message-composer"
          value={body}
          onChange={(event) => setBody(event.target.value)}
          onKeyDown={handleKeyDown}
          placeholder="Type a message..."
          maxLength={2000}
          rows={1}
          disabled={sending}
        />
        <div className="messaging-ui__composer-footer">
          <span>Enter to send · Shift + Enter for a new line</span>
          <button type="submit" disabled={sending || !body.trim()}>
            {sending ? "Sending..." : "Send"}
          </button>
        </div>
      </form>
    </section>
  );
}

export default MessageThread;
