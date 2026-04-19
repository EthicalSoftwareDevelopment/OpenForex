package core.order_matching;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderQueueTest {
    @Test
    void prioritizesBestBuyPriceThenTime() {
        OrderQueue queue = new OrderQueue();
        queue.enqueue(new OrderQueue.QueuedOrder("buy-1", "EURUSD", "BUY", "LIMIT", 1.1000d, 10_000d, 1L));
        queue.enqueue(new OrderQueue.QueuedOrder("buy-2", "EURUSD", "BUY", "LIMIT", 1.1005d, 10_000d, 2L));
        queue.enqueue(new OrderQueue.QueuedOrder("sell-1", "EURUSD", "SELL", "LIMIT", 1.1010d, 10_000d, 3L));

        OrderQueue.QueuedOrder opposing = queue.peekOpposing("EURUSD", "SELL");

        assertNotNull(opposing);
        assertEquals("buy-2", opposing.getOrderId());
        assertEquals(2, queue.depth("EURUSD", "BUY"));
    }
}

