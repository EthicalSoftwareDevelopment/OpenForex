package core.calculations;

import org.junit.jupiter.api.Test;

import jakarta.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.*;

class LeverageCalculatorTest {
    @Test
    void calculatesLeverageAndMarginMetrics() {
        LeverageCalculator calculator = new LeverageCalculator();
        LeverageCalculator.LeverageRequest request = new LeverageCalculator.LeverageRequest();
        request.setPositionSize(100_000d);
        request.setAccountBalance(10_000d);
        request.setEntryPrice(1.2d);
        request.setMaxAllowedLeverage(20d);

        Response response = calculator.calculateLeverage(request);
        LeverageCalculator.LeverageResult result = (LeverageCalculator.LeverageResult) response.getEntity();

        assertEquals(200, response.getStatus());
        assertEquals(120_000d, result.getNotionalValue());
        assertEquals(12d, result.getLeverageRatio());
        assertEquals(6_000d, result.getMarginRequirement());
        assertTrue(result.isWithinRiskLimit());
    }

    @Test
    void rejectsInvalidLeverageRequest() {
        LeverageCalculator calculator = new LeverageCalculator();
        LeverageCalculator.LeverageRequest request = new LeverageCalculator.LeverageRequest();
        request.setPositionSize(0d);
        request.setAccountBalance(10_000d);

        Response response = calculator.calculateLeverage(request);
        LeverageCalculator.LeverageResult result = (LeverageCalculator.LeverageResult) response.getEntity();

        assertEquals(400, response.getStatus());
        assertFalse(result.isWithinRiskLimit());
        assertEquals("positionSize must be greater than zero.", result.getMessage());
    }
}

