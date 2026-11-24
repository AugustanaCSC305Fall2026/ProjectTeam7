import { Client } from "@stomp/stompjs";

const websocketUrl = "ws://localhost:8080/ws";

export function connectMessagingSocket({ onMessage, onStatus }) {
  const token = localStorage.getItem("token");
  if (!token) {
    onStatus("disconnected");
    return () => {};
  }

  const client = new Client({
    brokerURL: websocketUrl,
    connectHeaders: { Authorization: `Bearer ${token}` },
    reconnectDelay: 5000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    onConnect: () => {
      onStatus("connected");
      client.subscribe("/user/queue/messages", (frame) => {
        try {
          onMessage(JSON.parse(frame.body));
        } catch {
          // Ignore malformed events; REST remains the source of truth.
        }
      });
    },
    onWebSocketClose: () => onStatus("disconnected"),
    onWebSocketError: () => onStatus("disconnected"),
    onStompError: () => onStatus("disconnected"),
  });

  onStatus("connecting");
  client.activate();
  return () => {
    onStatus("disconnected");
    void client.deactivate();
  };
}
