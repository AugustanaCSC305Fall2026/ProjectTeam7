import { useCallback, useEffect, useState } from "react";
import api from "../api/backendApi";

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

  const openConversation = useCallback(async (conversation) => {
    setSelectedConversation(conversation);
    setLoadingMessages(true);
    setSendError("");
    try {
      const response = await api.get(messagesUrl(conversation.id));
      setMessages(response.data);
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
      setMessages((current) => [...current, response.data]);
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
