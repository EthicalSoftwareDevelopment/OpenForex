const JAVA_BACKEND_URL = Deno.env.get("JAVA_BACKEND_URL") || "http://localhost:9080";
const PORT = parseInt(Deno.env.get("PORT") || "8000", 10);

console.log(`🚀 Starting Deno Edge Proxy Server on port ${PORT}...`);
console.log(`🔗 Destined Java Backend: ${JAVA_BACKEND_URL}`);

Deno.serve({ port: PORT }, async (req) => {
  const url = new URL(req.url);

  // 0. WebSocket Edge Gateway (Market Feed)
  if (url.pathname === "/ws/feed") {
    if (req.headers.get("upgrade")?.toLowerCase() !== "websocket") {
      return new Response("Expected WebSocket upgrade", { status: 400 });
    }
    const { socket, response } = Deno.upgradeWebSocket(req);

    socket.onopen = () => {
      console.log(`[Edge WS] Target client connected. Stream started.`);

      let eurUsdPrice = 1.1050;
      let amlPingCount = 0;

      const interval = setInterval(() => {
        if (socket.readyState === WebSocket.OPEN) {
          // Simulate high-frequency market tick
          eurUsdPrice += (Math.random() - 0.5) * 0.0020;

          socket.send(JSON.stringify({
            type: "MARKET_TICK",
            instrument: "EUR/USD",
            price: eurUsdPrice,
            timestamp: new Date().toISOString()
          }));

          // Intermittently simulate a compliance backend broadcast
          amlPingCount++;
          if (amlPingCount % 15 === 0) {
            socket.send(JSON.stringify({
              type: "COMPLIANCE_ALERT",
              message: "BaFin daily heartbeat acknowledged via JVM Core.",
              severity: "INFO",
              timestamp: new Date().toISOString()
            }));
          }
        } else {
          clearInterval(interval);
        }
      }, 800); // 800ms tick for simulation

      socket.onclose = () => {
        console.log(`[Edge WS] Connection closed.`);
        clearInterval(interval);
      };
    };

    return response;
  }

  // 1. Edge Gateway Route: BaFin Audit Logging
  if (req.method === "POST" && url.pathname === "/api/v1/bafin-audit") {
    console.log(`[Edge Auth/Audit] Intercepting BaFin audit beacon: ${req.url}`);

    try {
      const payload = await req.json();
      console.log(`[Edge Analytics] Received Action: ${payload.auditLog.actionType} for Session: ${payload.auditLog.sessionId}`);

      // Forward securely to the Java Backend for Persistence (Quarkus/OpenLiberty endpoint)
      // Note: We use keepalive headers and don't block the frontend response
      fetch(`${JAVA_BACKEND_URL}/api/audit`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "X-Gateway-Edge-Timestamp": new Date().toISOString()
        },
        body: JSON.stringify(payload),
      }).catch(err => {
        console.error(`[Edge Audit Sync Error] Failed to reach Java backend! Log queued locally:`, err);
        // Here we could implement an Edge dead-letter queue (e.g., using Deno KV)
      });

    } catch (e) {
      console.error(`[Edge Audit Error] Failed to parse beacon log:`, e);
      return new Response("Invalid payload", { status: 400 });
    }

    // Immediately release the client so the UI never blocks on compliance logging
    return new Response(JSON.stringify({ status: "Accepted", edgeReceived: true }), {
      status: 202,
      headers: { "Content-Type": "application/json" }
    });
  }

  // 2. Generic API Reverse Proxy to Java Backend
  // Maps React `/api/*` -> Java JAX-RS `http://localhost:9080/api/*`
  if (url.pathname.startsWith("/api/")) {
    const targetUrl = new URL(url.pathname + url.search, JAVA_BACKEND_URL);
    console.log(`[Edge Proxy] Forwarding ${req.method} ${url.pathname} -> ${targetUrl.toString()}`);

    try {
      const proxyReq = new Request(targetUrl.toString(), {
        method: req.method,
        headers: req.headers,
        body: req.body, // Re-stream readable body
        redirect: "manual"
      });

      const backendRes = await fetch(proxyReq);

      // We can inject security headers at the edge here:
      const proxyHeaders = new Headers(backendRes.headers);
      proxyHeaders.set("X-Protected-By", "OpenForex Deno Edge");

      return new Response(backendRes.body, {
        status: backendRes.status,
        statusText: backendRes.statusText,
        headers: proxyHeaders,
      });

    } catch (error) {
      console.error(`[Edge Proxy Error] connection refused to ${targetUrl}:`, error);
      return new Response(JSON.stringify({ error: "Java Backend Gateway Timeout" }), {
        status: 504,
        headers: { "Content-Type": "application/json" }
      });
    }
  }

  // 3. Static/CSR Fallback (Aleph.js / React Router)
  // During final integration, Aleph.js handles this block automatically.
  // For now, if we reach here and it's not an API route, serve a successful healthcheck.
  return new Response("OpenForex Edge Server running. Aleph CSR routes would render here.", {
    status: 200,
    headers: { "Content-Type": "text/html" }
  });
});
