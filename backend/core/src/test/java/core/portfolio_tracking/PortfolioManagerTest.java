package core.portfolio_tracking;

import org.junit.jupiter.api.Test;

import jakarta.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PortfolioManager.
 */
public class PortfolioManagerTest {
    @Test
    void updatesPortfolioAndReturnsSnapshot() {
        PortfolioManager manager = new PortfolioManager();
        PortfolioManager.PortfolioUpdateRequest request = new PortfolioManager.PortfolioUpdateRequest();
        request.setAccountId("acct-1");
        request.setCashAdjustment(5_000d);
        request.setSymbol("EURUSD");
        request.setSide("BUY");
        request.setQuantity(10_000d);
        request.setPrice(1.1d);

        Response updateResponse = manager.updatePortfolio(request);
        PortfolioManager.PortfolioSnapshot snapshot = (PortfolioManager.PortfolioSnapshot) updateResponse.getEntity();

        assertEquals(200, updateResponse.getStatus());
        assertEquals("acct-1", snapshot.getAccountId());
        assertEquals(10_000d, snapshot.getPositions().get("EURUSD"));
        assertEquals(-6_000d, snapshot.getCashBalance());

        Response getResponse = manager.getPortfolio("acct-1");
        PortfolioManager.PortfolioSnapshot loaded = (PortfolioManager.PortfolioSnapshot) getResponse.getEntity();
        assertEquals(200, getResponse.getStatus());
        assertEquals(11_000d, loaded.getGrossExposure());
    }

    @Test
    void rejectsEmptyPortfolioUpdate() {
        PortfolioManager manager = new PortfolioManager();
        PortfolioManager.PortfolioUpdateRequest request = new PortfolioManager.PortfolioUpdateRequest();
        request.setAccountId("acct-2");

        Response response = manager.updatePortfolio(request);
        PortfolioManager.PortfolioSnapshot snapshot = (PortfolioManager.PortfolioSnapshot) response.getEntity();

        assertEquals(400, response.getStatus());
        assertEquals("Either a trade or a cashAdjustment must be provided.", snapshot.getMessage());
    }
}

