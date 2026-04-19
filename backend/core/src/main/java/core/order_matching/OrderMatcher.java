package core.order_matching;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the matching of buy and sell orders for the trading engine.
 */
@Path("/orders")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Order Matching")
public class OrderMatcher {
    private final OrderQueue orderQueue = new OrderQueue();
    private final Map<String, MatchResult> matchResults = new ConcurrentHashMap<>();

    @POST
    @Counted(name = "orders_submitted_total", description = "Total number of order submissions received by the matching engine.", absolute = true)
    @Timed(name = "orders_submit_time", description = "Time spent processing order submissions.", absolute = true)
    @Operation(summary = "Submit an order for matching", description = "Validates the incoming order, attempts to cross the book, and returns the resulting match state.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Order matched immediately, partially, or rejected after immediate evaluation.",
                    content = @Content(schema = @Schema(implementation = MatchResult.class))),
            @APIResponse(responseCode = "202", description = "Order accepted and placed on the order book.",
                    content = @Content(schema = @Schema(implementation = MatchResult.class))),
            @APIResponse(responseCode = "400", description = "Invalid order request.",
                    content = @Content(schema = @Schema(implementation = MatchResult.class)))
    })
    public Response submitOrder(@RequestBody(required = true, description = "Order submission payload") OrderRequest request) {
        if (request == null) {
            return badRequest("Order request is required.");
        }

        String validationMessage = validate(request);
        if (validationMessage != null) {
            return badRequest(validationMessage);
        }

        String side = normalize(request.getSide());
        String orderType = normalize(request.getOrderType());
        double workingPrice = request.getPrice();
        if ("MARKET".equals(orderType)) {
            workingPrice = "BUY".equals(side) ? Double.MAX_VALUE : 0.0d;
        }

        OrderQueue.QueuedOrder incomingOrder = new OrderQueue.QueuedOrder(
                request.getOrderId(),
                request.getSymbol().trim().toUpperCase(),
                side,
                orderType,
                workingPrice,
                request.getQuantity(),
                System.nanoTime());

        double originalQuantity = request.getQuantity();
        double matchedQuantity = 0.0d;
        double matchedNotional = 0.0d;
        List<String> counterparties = new ArrayList<>();

        while (incomingOrder.getRemainingQuantity() > 0) {
            OrderQueue.QueuedOrder bestOpposing = orderQueue.peekOpposing(incomingOrder.getSymbol(), side);
            if (bestOpposing == null || !crosses(incomingOrder, bestOpposing)) {
                break;
            }

            bestOpposing = orderQueue.pollOpposing(incomingOrder.getSymbol(), side);
            double fillQuantity = Math.min(incomingOrder.getRemainingQuantity(), bestOpposing.getRemainingQuantity());
            double executionPrice = round(bestOpposing.getPrice());

            incomingOrder.setRemainingQuantity(incomingOrder.getRemainingQuantity() - fillQuantity);
            bestOpposing.setRemainingQuantity(bestOpposing.getRemainingQuantity() - fillQuantity);

            matchedQuantity += fillQuantity;
            matchedNotional += fillQuantity * executionPrice;
            counterparties.add(bestOpposing.getOrderId());

            matchResults.put(bestOpposing.getOrderId(), new MatchResult(
                    bestOpposing.getOrderId(),
                    bestOpposing.getSymbol(),
                    bestOpposing.getRemainingQuantity() == 0 ? "MATCHED" : "PARTIAL",
                    round(fillQuantity),
                    round(bestOpposing.getRemainingQuantity()),
                    executionPrice,
                    List.of(incomingOrder.getOrderId()),
                    true,
                    bestOpposing.getRemainingQuantity() == 0
                            ? "Order fully matched."
                            : "Order partially matched and remains on the book."));

            if (bestOpposing.getRemainingQuantity() > 0) {
                orderQueue.enqueue(bestOpposing);
            }
        }

        boolean shouldRest = incomingOrder.getRemainingQuantity() > 0 && !"MARKET".equals(orderType);
        if (shouldRest) {
            orderQueue.enqueue(incomingOrder);
        }

        String status;
        if (matchedQuantity == 0.0d) {
            status = shouldRest ? "PENDING" : "REJECTED";
        } else if (incomingOrder.getRemainingQuantity() > 0) {
            status = shouldRest ? "PARTIAL" : "MATCHED";
        } else {
            status = "MATCHED";
        }

        MatchResult result = new MatchResult(
                request.getOrderId(),
                incomingOrder.getSymbol(),
                status,
                round(matchedQuantity),
                round(incomingOrder.getRemainingQuantity()),
                matchedQuantity == 0.0d ? 0.0d : round(matchedNotional / matchedQuantity),
                counterparties,
                !"REJECTED".equals(status),
                statusMessage(status));

        matchResults.put(request.getOrderId(), result);

        Response.Status httpStatus = switch (status) {
            case "PENDING" -> Response.Status.ACCEPTED;
            case "MATCHED", "PARTIAL", "REJECTED" -> Response.Status.OK;
            default -> Response.Status.OK;
        };

        return Response.status(httpStatus)
                .entity(result)
                .build();
    }

    @GET
    @Path("/{orderId}")
    @Counted(name = "order_status_requests_total", description = "Total number of order status lookups.", absolute = true)
    @Operation(summary = "Fetch the current match status for an order")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Known order status.",
                    content = @Content(schema = @Schema(implementation = MatchResult.class))),
            @APIResponse(responseCode = "404", description = "No matching order status was found.",
                    content = @Content(schema = @Schema(implementation = MatchResult.class)))
    })
    public Response getMatchStatus(@Parameter(description = "Order identifier to inspect") @PathParam("orderId") String orderId) {
        MatchResult result = matchResults.get(orderId);
        if (result == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new MatchResult(orderId, null, "UNKNOWN", 0.0d, 0.0d, 0.0d, List.of(), false,
                            "No match status was found for the supplied orderId."))
                    .build();
        }
        return Response.ok(result).build();
    }

    private boolean crosses(OrderQueue.QueuedOrder incomingOrder, OrderQueue.QueuedOrder opposingOrder) {
        String orderType = normalize(incomingOrder.getOrderType());
        if ("MARKET".equals(orderType)) {
            return true;
        }
        if ("BUY".equals(normalize(incomingOrder.getSide()))) {
            return incomingOrder.getPrice() >= opposingOrder.getPrice();
        }
        return incomingOrder.getPrice() <= opposingOrder.getPrice();
    }

    private Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new MatchResult(null, null, "REJECTED", 0.0d, 0.0d, 0.0d, List.of(), false, message))
                .build();
    }

    private String validate(OrderRequest request) {
        if (isBlank(request.getOrderId())) {
            return "orderId is required.";
        }
        if (isBlank(request.getSymbol())) {
            return "symbol is required.";
        }
        String side = normalize(request.getSide());
        if (!"BUY".equals(side) && !"SELL".equals(side)) {
            return "side must be BUY or SELL.";
        }
        String orderType = normalize(request.getOrderType());
        if (!"MARKET".equals(orderType) && !"LIMIT".equals(orderType) && !"STOP".equals(orderType)) {
            return "orderType must be MARKET, LIMIT, or STOP.";
        }
        if ("STOP".equals(orderType)) {
            return "STOP orders are not supported by the order matching engine; submit triggered orders as MARKET or LIMIT.";
        }
        if (request.getQuantity() <= 0) {
            return "quantity must be greater than zero.";
        }
        if (!"MARKET".equals(orderType) && request.getPrice() <= 0) {
            return "price must be greater than zero for limit and stop orders.";
        }
        return null;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private double round(double value) {
        return Math.round(value * 100_000.0d) / 100_000.0d;
    }

    private String statusMessage(String status) {
        return switch (status) {
            case "MATCHED" -> "Order matched successfully.";
            case "PARTIAL" -> "Order partially matched and resting on the book.";
            case "PENDING" -> "Order is resting on the book awaiting a counterparty.";
            case "REJECTED" -> "Market order could not be matched because no liquidity was available.";
            default -> "Order status is unavailable.";
        };
    }

    @Schema(name = "OrderRequest", description = "Order payload submitted to the matching engine.")
    public static class OrderRequest {
        private String orderId;
        private String symbol;
        private String side;
        private String orderType;
        private double price;
        private double quantity;

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getSide() {
            return side;
        }

        public void setSide(String side) {
            this.side = side;
        }

        public String getOrderType() {
            return orderType;
        }

        public void setOrderType(String orderType) {
            this.orderType = orderType;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public double getQuantity() {
            return quantity;
        }

        public void setQuantity(double quantity) {
            this.quantity = quantity;
        }
    }

    @Schema(name = "OrderMatchResult", description = "Current outcome of order matching for a submitted order.")
    public static class MatchResult {
        private String orderId;
        private String symbol;
        private String status;
        private double matchedQuantity;
        private double remainingQuantity;
        private double averageMatchedPrice;
        private List<String> counterpartyOrderIds;
        private boolean successful;
        private String message;

        public MatchResult() {
        }

        public MatchResult(String orderId, String symbol, String status, double matchedQuantity, double remainingQuantity,
                           double averageMatchedPrice, List<String> counterpartyOrderIds, boolean successful,
                           String message) {
            this.orderId = orderId;
            this.symbol = symbol;
            this.status = status;
            this.matchedQuantity = matchedQuantity;
            this.remainingQuantity = remainingQuantity;
            this.averageMatchedPrice = averageMatchedPrice;
            this.counterpartyOrderIds = counterpartyOrderIds;
            this.successful = successful;
            this.message = message;
        }

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public double getMatchedQuantity() {
            return matchedQuantity;
        }

        public void setMatchedQuantity(double matchedQuantity) {
            this.matchedQuantity = matchedQuantity;
        }

        public double getRemainingQuantity() {
            return remainingQuantity;
        }

        public void setRemainingQuantity(double remainingQuantity) {
            this.remainingQuantity = remainingQuantity;
        }

        public double getAverageMatchedPrice() {
            return averageMatchedPrice;
        }

        public void setAverageMatchedPrice(double averageMatchedPrice) {
            this.averageMatchedPrice = averageMatchedPrice;
        }

        public List<String> getCounterpartyOrderIds() {
            return counterpartyOrderIds;
        }

        public void setCounterpartyOrderIds(List<String> counterpartyOrderIds) {
            this.counterpartyOrderIds = counterpartyOrderIds;
        }

        public boolean isSuccessful() {
            return successful;
        }

        public void setSuccessful(boolean successful) {
            this.successful = successful;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

