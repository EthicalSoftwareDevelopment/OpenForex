package core.execution_rules;

import org.junit.jupiter.api.Test;

import jakarta.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.*;

class OrderExecutorTest {
    @Test
    void executesMarketOrderWithPartialFillWhenPolicyAllowsIt() {
        OrderExecutor executor = new OrderExecutor();
        OrderExecutor.ExecutionRequest request = new OrderExecutor.ExecutionRequest();
        request.setOrderId("exec-1");
        request.setSymbol("EURUSD");
        request.setSide("BUY");
        request.setOrderType("MARKET");
        request.setMarketPrice(1.10234d);
        request.setQuantity(100_000d);
        request.setAvailableLiquidity(40_000d);

        Response response = executor.executeOrder(request);
        OrderExecutor.ExecutionResult result = (OrderExecutor.ExecutionResult) response.getEntity();

        assertEquals(200, response.getStatus());
        assertEquals("PARTIAL", result.getStatus());
        assertEquals(40_000d, result.getFilledQuantity());
        assertTrue(result.isSuccessful());
    }

    @Test
    void rejectsLimitOrderWhenTriggerPriceIsNotReached() {
        OrderExecutor executor = new OrderExecutor();
        OrderExecutor.ExecutionRequest request = new OrderExecutor.ExecutionRequest();
        request.setOrderId("exec-2");
        request.setSymbol("EURUSD");
        request.setSide("BUY");
        request.setOrderType("LIMIT");
        request.setRequestedPrice(1.10000d);
        request.setMarketPrice(1.10100d);
        request.setQuantity(10_000d);
        request.setAvailableLiquidity(10_000d);

        Response response = executor.executeOrder(request);
        OrderExecutor.ExecutionResult result = (OrderExecutor.ExecutionResult) response.getEntity();

        assertEquals(200, response.getStatus());
        assertEquals("REJECTED", result.getStatus());
        assertFalse(result.isSuccessful());
    }
}

