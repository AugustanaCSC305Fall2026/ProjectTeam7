import { useCallback, useEffect, useRef, useState } from "react";
import api from "../api/backendApi";
import { connectMessagingSocket } from "../api/messagingSocket";

function appendUniqueMessage(current, incoming) {
  if (!incoming?.id || current.some((message) => message.id === incoming.id)) {
    return current;
  }
  return [...current, incoming].sort(
    (first, second) => new Date(first.sentAt) - new Date(second.sentAt),
  );
}

function useMessagingConversation({
  loadLists,
  messagesUrl,
  sendUrl,
  loadErrorMessage = "We could not load messages. Try again.",
  openErrorMessage = "We could not open this conversation. Try again.",
}) {
  const [selectedConversation, setSelectedConversation] = useState(null);
  const [messages, setMessages] = useState([]);
  const [body, setBody] = useState("");
  const [failedBody, setFailedBody] = useState("");
  const [loading, setLoading] = useState(true);
  const [loadingMessages, setLoadingMessages] = useState(false);
  const [sending, setSending] = useState(false);
  const [error, setError] = useState("");
  const [sendError, setSendError] = useState("");
  const [connectionStatus, setConnectionStatus] = useState("connecting");
  const selectedConversationRef = useRef(selectedConversation);
  const loadListsRef = useRef(loadLists);

  useEffect(() => {
    selectedConversationRef.current = selectedConversation;
  }, [selectedConversation]);

  useEffect(() => {
    loadListsRef.current = loadLists;
  }, [loadLists]);

  const loadData = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      await loadLists();
    } catch (requestError) {
      setError(typeof loadErrorMessage === "function"
        ? loadErrorMessage(requestError)
        : loadErrorMessage);
    } finally {
      setLoading(false);
    }
  }, [loadErrorMessage, loadLists]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  useEffect(() => connectMessagingSocket({
    onStatus: setConnectionStatus,
    onMessage: (incoming) => {
      if (selectedConversationRef.current?.id === incoming.conversationId) {
        setMessages((current) => appendUniqueMessage(current, incoming));
      }
      loadListsRef.current().catch(() => {
        // A failed preview refresh must not interrupt the live thread or REST UI.
      });
    },
  }), []);

  const openConversation = useCallback(async (conversation) => {
    selectedConversationRef.current = conversation;
    setSelectedConversation(conversation);
    setMessages([]);
    setLoadingMessages(true);
    setSendError("");
    try {
      const response = await api.get(messagesUrl(conversation.id));
      setMessages((current) => response.data.reduce(
        (messagesWithEvents, message) => appendUniqueMessage(messagesWithEvents, message),
        current,
      ));
    } catch {
      setError(openErrorMessage);
    } finally {
      setLoadingMessages(false);
    }
  }, [messagesUrl, openErrorMessage]);

  const sendMessage = useCallback(async (messageBody) => {
    if (!selectedConversation || !messageBody.trim()) return;
    setSending(true);
    setSendError("");
    try {
      const response = await api.post(sendUrl(selectedConversation.id), { body: messageBody });
      setMessages((current) => appendUniqueMessage(current, response.data));
      setBody("");
      setFailedBody("");
      await loadLists();
    } catch {
      setFailedBody(messageBody);
      setSendError("Your message was not sent.");
    } finally {
      setSending(false);
    }
  }, [loadLists, selectedConversation, sendUrl]);

  const handleSend = useCallback((event) => {
    event.preventDefault();
    sendMessage(body);
  }, [body, sendMessage]);

  return {
    body,
    connectionStatus,
    error,
    failedBody,
    handleSend,
    loadData,
    loading,
    loadingMessages,
    messages,
    openConversation,
    selectedConversation,
    sendError,
    sendMessage,
    sending,
    setBody,
    setError,
    setSelectedConversation,
  };
}

export default useMessagingConversation;
