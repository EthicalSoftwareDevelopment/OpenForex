package core.order_matching;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Manages order queues for different trading instruments.
 */
public class OrderQueue {
    private final Map<String, PriorityQueue<QueuedOrder>> buyBooks = new HashMap<>();
    private final Map<String, PriorityQueue<QueuedOrder>> sellBooks = new HashMap<>();

    public synchronized void enqueue(QueuedOrder order) {
        book(order.getSymbol(), order.getSide()).offer(order);
    }

    public synchronized QueuedOrder peekOpposing(String symbol, String incomingSide) {
        PriorityQueue<QueuedOrder> queue = "BUY".equalsIgnoreCase(incomingSide)
                ? sellBooks.get(symbol)
                : buyBooks.get(symbol);
        return queue == null ? null : queue.peek();
    }

    public synchronized QueuedOrder pollOpposing(String symbol, String incomingSide) {
        PriorityQueue<QueuedOrder> queue = "BUY".equalsIgnoreCase(incomingSide)
                ? sellBooks.get(symbol)
                : buyBooks.get(symbol);
        return queue == null ? null : queue.poll();
    }

    public synchronized int depth(String symbol, String side) {
        PriorityQueue<QueuedOrder> queue = "BUY".equalsIgnoreCase(side)
                ? buyBooks.get(symbol)
                : sellBooks.get(symbol);
        return queue == null ? 0 : queue.size();
    }

    public synchronized List<QueuedOrder> snapshot(String symbol, String side) {
        PriorityQueue<QueuedOrder> queue = "BUY".equalsIgnoreCase(side)
                ? buyBooks.get(symbol)
                : sellBooks.get(symbol);
        return queue == null ? List.of() : new ArrayList<>(queue);
    }

    private PriorityQueue<QueuedOrder> book(String symbol, String side) {
        Map<String, PriorityQueue<QueuedOrder>> selected = "BUY".equalsIgnoreCase(side) ? buyBooks : sellBooks;
        return selected.computeIfAbsent(symbol, key -> new PriorityQueue<>(comparatorFor(side)));
    }

    private Comparator<QueuedOrder> comparatorFor(String side) {
        Comparator<QueuedOrder> timestampComparator = Comparator.comparingLong(QueuedOrder::getCreatedAtNanos);
        if ("BUY".equalsIgnoreCase(side)) {
            return Comparator.comparingDouble(QueuedOrder::getPrice).reversed().thenComparing(timestampComparator);
        }
        return Comparator.comparingDouble(QueuedOrder::getPrice).thenComparing(timestampComparator);
    }

    public static class QueuedOrder {
        private final String orderId;
        private final String symbol;
        private final String side;
        private final String orderType;
        private final double price;
        private double remainingQuantity;
        private final long createdAtNanos;

        public QueuedOrder(String orderId, String symbol, String side, String orderType, double price,
                           double remainingQuantity, long createdAtNanos) {
            this.orderId = orderId;
            this.symbol = symbol;
            this.side = side;
            this.orderType = orderType;
            this.price = price;
            this.remainingQuantity = remainingQuantity;
            this.createdAtNanos = createdAtNanos;
        }

        public String getOrderId() {
            return orderId;
        }

        public String getSymbol() {
            return symbol;
        }

        public String getSide() {
            return side;
        }

        public String getOrderType() {
            return orderType;
        }

        public double getPrice() {
            return price;
        }

        public double getRemainingQuantity() {
            return remainingQuantity;
        }

        public void setRemainingQuantity(double remainingQuantity) {
            this.remainingQuantity = remainingQuantity;
        }

        public long getCreatedAtNanos() {
            return createdAtNanos;
        }
    }
}

