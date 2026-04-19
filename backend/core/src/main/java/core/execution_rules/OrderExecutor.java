package core.execution_rules;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Handles execution logic for various order types (market, limit, stop).
 */
@Path("/execute")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OrderExecutor {
    @POST
    public Response executeOrder(ExecutionRequest request) {
        if (request == null) {
            return badRequest("Execution request is required.");
        }

        String validationMessage = validate(request);
        if (validationMessage != null) {
            return badRequest(validationMessage);
        }

        ExecutionConfig config = request.getExecutionConfig() == null
                ? ExecutionConfig.defaultConfig()
                : request.getExecutionConfig();

        if (!config.validate().isEmpty()) {
            return badRequest(String.join(" ", config.validate()));
        }

        String side = normalize(request.getSide());
        String orderType = normalize(request.getOrderType());
        double marketPrice = request.getMarketPrice();
        double requestedPrice = request.getRequestedPrice() > 0 ? request.getRequestedPrice() : marketPrice;
        double availableLiquidity = request.getAvailableLiquidity() > 0 ? request.getAvailableLiquidity() : request.getQuantity();

        if (config.isRequirePriceImprovement() && marketPrice >= requestedPrice && "BUY".equals(side)) {
            return rejectedResult(request, "Configured price improvement requirement was not satisfied.");
        }
        if (config.isRequirePriceImprovement() && marketPrice <= requestedPrice && "SELL".equals(side)) {
            return rejectedResult(request, "Configured price improvement requirement was not satisfied.");
        }

        if (!isTriggered(orderType, side, requestedPrice, marketPrice)) {
            return Response.ok(new ExecutionResult(
                    request.getOrderId(),
                    "REJECTED",
                    0.0d,
                    0.0d,
                    "Order conditions were not met at the current market price.",
                    false)).build();
        }

        double slippage = Math.abs(marketPrice - requestedPrice);
        if (!"MARKET".equals(orderType) && slippage > config.getMaxSlippagePips()) {
            return rejectedResult(request, "Slippage exceeds configured tolerance.");
        }

        if (availableLiquidity <= 0) {
            return rejectedResult(request, "No liquidity is currently available.");
        }

        if (availableLiquidity < request.getQuantity() && !config.isAllowPartialFill()) {
            return rejectedResult(request, "Partial fills are disabled for this execution policy.");
        }

        double filledQuantity = Math.min(request.getQuantity(), availableLiquidity);
        String status = filledQuantity < request.getQuantity() ? "PARTIAL" : "FILLED";

        return Response.ok(new ExecutionResult(
                request.getOrderId(),
                status,
                round(marketPrice),
                round(filledQuantity),
                status.equals("FILLED")
                        ? "Order executed successfully."
                        : "Order partially executed due to limited liquidity.",
                true)).build();
    }

    private Response rejectedResult(ExecutionRequest request, String reason) {
        return Response.ok(new ExecutionResult(
                request.getOrderId(),
                "REJECTED",
                0.0d,
                0.0d,
                reason,
                false)).build();
    }

    private Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ExecutionResult(null, "REJECTED", 0.0d, 0.0d, message, false))
                .build();
    }

    private String validate(ExecutionRequest request) {
        if (isBlank(request.getOrderId())) {
            return "orderId is required.";
        }
        if (isBlank(request.getSymbol())) {
            return "symbol is required.";
        }
        if (!"BUY".equals(normalize(request.getSide())) && !"SELL".equals(normalize(request.getSide()))) {
            return "side must be BUY or SELL.";
        }
        String orderType = normalize(request.getOrderType());
        if (!"MARKET".equals(orderType) && !"LIMIT".equals(orderType) && !"STOP".equals(orderType)) {
            return "orderType must be MARKET, LIMIT, or STOP.";
        }
        if (request.getQuantity() <= 0) {
            return "quantity must be greater than zero.";
        }
        if (request.getMarketPrice() <= 0) {
            return "marketPrice must be greater than zero.";
        }
        if (("LIMIT".equals(orderType) || "STOP".equals(orderType)) && request.getRequestedPrice() <= 0) {
            return "requestedPrice must be greater than zero for limit and stop orders.";
        }
        return null;
    }

    private boolean isTriggered(String orderType, String side, double requestedPrice, double marketPrice) {
        return switch (orderType) {
            case "MARKET" -> true;
            case "LIMIT" -> "BUY".equals(side) ? marketPrice <= requestedPrice : marketPrice >= requestedPrice;
            case "STOP" -> "BUY".equals(side) ? marketPrice >= requestedPrice : marketPrice <= requestedPrice;
            default -> false;
        };
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

    public static class ExecutionRequest {
        private String orderId;
        private String symbol;
        private String side;
        private String orderType;
        private double requestedPrice;
        private double marketPrice;
        private double quantity;
        private double availableLiquidity;
        private ExecutionConfig executionConfig;

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

        public double getRequestedPrice() {
            return requestedPrice;
        }

        public void setRequestedPrice(double requestedPrice) {
            this.requestedPrice = requestedPrice;
        }

        public double getMarketPrice() {
            return marketPrice;
        }

        public void setMarketPrice(double marketPrice) {
            this.marketPrice = marketPrice;
        }

        public double getQuantity() {
            return quantity;
        }

        public void setQuantity(double quantity) {
            this.quantity = quantity;
        }

        public double getAvailableLiquidity() {
            return availableLiquidity;
        }

        public void setAvailableLiquidity(double availableLiquidity) {
            this.availableLiquidity = availableLiquidity;
        }

        public ExecutionConfig getExecutionConfig() {
            return executionConfig;
        }

        public void setExecutionConfig(ExecutionConfig executionConfig) {
            this.executionConfig = executionConfig;
        }
    }

    public static class ExecutionResult {
        private String orderId;
        private String status;
        private double executedPrice;
        private double filledQuantity;
        private String message;
        private boolean successful;

        public ExecutionResult() {
        }

        public ExecutionResult(String orderId, String status, double executedPrice, double filledQuantity, String message,
                               boolean successful) {
            this.orderId = orderId;
            this.status = status;
            this.executedPrice = executedPrice;
            this.filledQuantity = filledQuantity;
            this.message = message;
            this.successful = successful;
        }

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public double getExecutedPrice() {
            return executedPrice;
        }

        public void setExecutedPrice(double executedPrice) {
            this.executedPrice = executedPrice;
        }

        public double getFilledQuantity() {
            return filledQuantity;
        }

        public void setFilledQuantity(double filledQuantity) {
            this.filledQuantity = filledQuantity;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public boolean isSuccessful() {
            return successful;
        }

        public void setSuccessful(boolean successful) {
            this.successful = successful;
        }
    }
}

