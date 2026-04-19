package core.order_matching;

import org.junit.jupiter.api.Test;

import jakarta.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.*;

class OrderMatcherTest {
    @Test
    void matchesCompatibleOrdersAndPersistsStatus() {
        OrderMatcher matcher = new OrderMatcher();

        OrderMatcher.OrderRequest sellOrder = new OrderMatcher.OrderRequest();
        sellOrder.setOrderId("sell-1");
        sellOrder.setSymbol("EURUSD");
        sellOrder.setSide("SELL");
        sellOrder.setOrderType("LIMIT");
        sellOrder.setPrice(1.10000d);
        sellOrder.setQuantity(100_000d);

        Response firstResponse = matcher.submitOrder(sellOrder);
        OrderMatcher.MatchResult pending = (OrderMatcher.MatchResult) firstResponse.getEntity();
        assertEquals(202, firstResponse.getStatus());
        assertEquals("PENDING", pending.getStatus());

        OrderMatcher.OrderRequest buyOrder = new OrderMatcher.OrderRequest();
        buyOrder.setOrderId("buy-1");
        buyOrder.setSymbol("EURUSD");
        buyOrder.setSide("BUY");
        buyOrder.setOrderType("LIMIT");
        buyOrder.setPrice(1.10020d);
        buyOrder.setQuantity(100_000d);

        Response secondResponse = matcher.submitOrder(buyOrder);
        OrderMatcher.MatchResult matched = (OrderMatcher.MatchResult) secondResponse.getEntity();

        assertEquals(200, secondResponse.getStatus());
        assertEquals("MATCHED", matched.getStatus());
        assertEquals(100_000d, matched.getMatchedQuantity());
        assertEquals(1, matched.getCounterpartyOrderIds().size());

        Response statusResponse = matcher.getMatchStatus("sell-1");
        OrderMatcher.MatchResult sellStatus = (OrderMatcher.MatchResult) statusResponse.getEntity();
        assertEquals(200, statusResponse.getStatus());
        assertEquals("MATCHED", sellStatus.getStatus());
    }

    @Test
    void returnsNotFoundForUnknownOrderStatus() {
        OrderMatcher matcher = new OrderMatcher();

        Response response = matcher.getMatchStatus("missing-order");
        OrderMatcher.MatchResult result = (OrderMatcher.MatchResult) response.getEntity();

        assertEquals(404, response.getStatus());
        assertEquals("UNKNOWN", result.getStatus());
    }
}

