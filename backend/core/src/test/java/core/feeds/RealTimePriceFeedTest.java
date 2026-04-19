package core.feeds;

import org.junit.jupiter.api.Test;

import jakarta.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.*;

class RealTimePriceFeedTest {
    @Test
    void returnsSyntheticTickForRequestedSymbol() {
        RealTimePriceFeed feed = new RealTimePriceFeed();

        Response response = feed.getPriceTick("eurusd", "synthetic", 500L);
        RealTimePriceFeed.PriceTick tick = (RealTimePriceFeed.PriceTick) response.getEntity();

        assertEquals(200, response.getStatus());
        assertEquals("EURUSD", tick.getSymbol());
        assertEquals("synthetic", tick.getSource());
        assertTrue(tick.getAsk() > tick.getBid());
        assertNotNull(tick.getTimestamp());
    }

    @Test
    void rejectsMissingSymbol() {
        RealTimePriceFeed feed = new RealTimePriceFeed();

        Response response = feed.getPriceTick("   ", "synthetic", 500L);
        RealTimePriceFeed.PriceTick tick = (RealTimePriceFeed.PriceTick) response.getEntity();

        assertEquals(400, response.getStatus());
        assertEquals("symbol is required.", tick.getMessage());
    }
}

