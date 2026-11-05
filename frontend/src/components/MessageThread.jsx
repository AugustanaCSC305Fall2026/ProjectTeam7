import "./MessagingUI.css";

function MessageThread({ conversation, messages, loading, body, setBody, onSend, sending }) {
  if (!conversation) {
    return <section className="messaging-ui__thread"><p>Select a conversation to view messages.</p></section>;
  }

  const title = conversation.employeeName
    || conversation.otherUserName
    || conversation.name;

  return (
    <section className="messaging-ui__thread">
      <h3>{title}</h3>
      <div className="messaging-ui__history">
        {loading ? (
          <p>Loading messages...</p>
        ) : messages.length === 0 ? (
          <p>No messages yet. Send the first message.</p>
        ) : messages.map((message) => (
          <article className="messaging-ui__message" key={message.id}>
            <strong>{message.senderName}</strong>
            <p>{message.body}</p>
            <small>{new Date(message.sentAt).toLocaleString()}</small>
          </article>
        ))}
      </div>
      <form className="messaging-ui__composer" onSubmit={onSend}>
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
    </section>
  );
}

export default MessageThread;
