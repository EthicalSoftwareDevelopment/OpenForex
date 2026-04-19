package core.feeds;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles real-time price feed data ingestion and processing.
 */
@Path("/price-feed")
@Produces(MediaType.APPLICATION_JSON)
public class RealTimePriceFeed {
    private final Map<String, PriceTick> latestTicks = new ConcurrentHashMap<>();

    @GET
    public Response getPriceTick(@QueryParam("symbol") String symbol,
                                 @DefaultValue("synthetic") @QueryParam("source") String source,
                                 @DefaultValue("1000") @QueryParam("frequencyMs") long frequencyMs) {
        if (isBlank(symbol)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new PriceTick(null, source, frequencyMs, 0.0d, 0.0d, 0.0d, null,
                            "symbol is required."))
                    .build();
        }
        if (frequencyMs <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new PriceTick(symbol.trim().toUpperCase(), source, frequencyMs, 0.0d, 0.0d, 0.0d, null,
                            "frequencyMs must be greater than zero."))
                    .build();
        }

        String normalizedSymbol = symbol.trim().toUpperCase();
        PriceTick tick = latestTicks.computeIfAbsent(normalizedSymbol,
                key -> createSyntheticTick(normalizedSymbol, source, frequencyMs));
        return Response.ok(tick).build();
    }

    public PriceTick publishSyntheticTick(String symbol, String source, long frequencyMs) {
        PriceTick tick = createSyntheticTick(symbol.trim().toUpperCase(), source, frequencyMs);
        latestTicks.put(tick.getSymbol(), tick);
        return tick;
    }

    private PriceTick createSyntheticTick(String symbol, String source, long frequencyMs) {
        double basePrice = 1.00000d + (Math.abs(symbol.hashCode()) % 5000) / 10_000.0d;
        double drift = (frequencyMs % 997) / 100_000.0d;
        double bid = round(basePrice + drift);
        double ask = round(bid + 0.00020d);
        double last = round((bid + ask) / 2.0d);
        return new PriceTick(symbol, source, frequencyMs, bid, ask, last, Instant.now().toString(),
                "Synthetic tick generated.");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private double round(double value) {
        return Math.round(value * 100_000.0d) / 100_000.0d;
    }

    public static class PriceTick {
        private String symbol;
        private String source;
        private long frequencyMs;
        private double bid;
        private double ask;
        private double last;
        private String timestamp;
        private String message;

        public PriceTick() {
        }

        public PriceTick(String symbol, String source, long frequencyMs, double bid, double ask, double last,
                         String timestamp, String message) {
            this.symbol = symbol;
            this.source = source;
            this.frequencyMs = frequencyMs;
            this.bid = bid;
            this.ask = ask;
            this.last = last;
            this.timestamp = timestamp;
            this.message = message;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public long getFrequencyMs() {
            return frequencyMs;
        }

        public void setFrequencyMs(long frequencyMs) {
            this.frequencyMs = frequencyMs;
        }

        public double getBid() {
            return bid;
        }

        public void setBid(double bid) {
            this.bid = bid;
        }

        public double getAsk() {
            return ask;
        }

        public void setAsk(double ask) {
            this.ask = ask;
        }

        public double getLast() {
            return last;
        }

        public void setLast(double last) {
            this.last = last;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

