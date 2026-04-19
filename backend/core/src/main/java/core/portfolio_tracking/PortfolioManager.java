package core.portfolio_tracking;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks portfolio balances and positions.
 */
@Path("/portfolio")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PortfolioManager {
    private final Map<String, AccountState> accounts = new ConcurrentHashMap<>();

    @GET
    public Response getPortfolio(@DefaultValue("default") @QueryParam("accountId") String accountId) {
        String normalizedAccountId = normalizeAccountId(accountId);
        return Response.ok(toSnapshot(normalizedAccountId, stateFor(normalizedAccountId))).build();
    }

    @POST
    @Path("/update")
    public Response updatePortfolio(PortfolioUpdateRequest request) {
        if (request == null) {
            return badRequest("Portfolio update request is required.");
        }

        String validationMessage = validate(request);
        if (validationMessage != null) {
            return badRequest(validationMessage);
        }

        String accountId = normalizeAccountId(request.getAccountId());
        AccountState state = stateFor(accountId);
        state.cashBalance += request.getCashAdjustment();

        if (!isBlank(request.getSymbol()) && request.getQuantity() > 0) {
            String symbol = request.getSymbol().trim().toUpperCase();
            double signedQuantity = "BUY".equals(normalize(request.getSide())) ? request.getQuantity() : -request.getQuantity();
            state.positions.merge(symbol, signedQuantity, Double::sum);
            state.lastTradePrices.put(symbol, request.getPrice());
            double cashDeltaFromTrade = request.getQuantity() * request.getPrice();
            state.cashBalance += "BUY".equals(normalize(request.getSide())) ? -cashDeltaFromTrade : cashDeltaFromTrade;
        }

        return Response.ok(toSnapshot(accountId, state)).build();
    }

    private AccountState stateFor(String accountId) {
        return accounts.computeIfAbsent(accountId, ignored -> new AccountState());
    }

    private PortfolioSnapshot toSnapshot(String accountId, AccountState state) {
        Map<String, Double> positions = new HashMap<>();
        for (Map.Entry<String, Double> entry : state.positions.entrySet()) {
            if (Math.abs(entry.getValue()) > 0.0000001d) {
                positions.put(entry.getKey(), round(entry.getValue()));
            }
        }

        double grossExposure = 0.0d;
        double netExposure = 0.0d;
        for (Map.Entry<String, Double> entry : positions.entrySet()) {
            double quantity = entry.getValue();
            double price = state.lastTradePrices.getOrDefault(entry.getKey(), 0.0d);
            grossExposure += Math.abs(quantity * price);
            netExposure += quantity * price;
        }

        return new PortfolioSnapshot(accountId, round(state.cashBalance), positions, round(grossExposure),
                round(netExposure), positions.isEmpty() ? "Portfolio is flat." : "Portfolio state retrieved.");
    }

    private Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new PortfolioSnapshot(null, 0.0d, Map.of(), 0.0d, 0.0d, message))
                .build();
    }

    private String validate(PortfolioUpdateRequest request) {
        if (isBlank(request.getAccountId())) {
            return "accountId is required.";
        }
        boolean hasTrade = !isBlank(request.getSymbol()) || request.getQuantity() > 0 || !isBlank(request.getSide()) || request.getPrice() > 0;
        if (!hasTrade) {
            if (request.getCashAdjustment() == 0.0d) {
                return "Either a trade or a cashAdjustment must be provided.";
            }
            return null;
        }
        if (isBlank(request.getSymbol())) {
            return "symbol is required when submitting a trade update.";
        }
        String side = normalize(request.getSide());
        if (!"BUY".equals(side) && !"SELL".equals(side)) {
            return "side must be BUY or SELL when submitting a trade update.";
        }
        if (request.getQuantity() <= 0) {
            return "quantity must be greater than zero when submitting a trade update.";
        }
        if (request.getPrice() <= 0) {
            return "price must be greater than zero when submitting a trade update.";
        }
        return null;
    }

    private String normalizeAccountId(String accountId) {
        return isBlank(accountId) ? "default" : accountId.trim();
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

    public static class PortfolioUpdateRequest {
        private String accountId;
        private String symbol;
        private String side;
        private double quantity;
        private double price;
        private double cashAdjustment;

        public String getAccountId() {
            return accountId;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
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

        public double getQuantity() {
            return quantity;
        }

        public void setQuantity(double quantity) {
            this.quantity = quantity;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public double getCashAdjustment() {
            return cashAdjustment;
        }

        public void setCashAdjustment(double cashAdjustment) {
            this.cashAdjustment = cashAdjustment;
        }
    }

    public static class PortfolioSnapshot {
        private String accountId;
        private double cashBalance;
        private Map<String, Double> positions;
        private double grossExposure;
        private double netExposure;
        private String message;

        public PortfolioSnapshot() {
        }

        public PortfolioSnapshot(String accountId, double cashBalance, Map<String, Double> positions, double grossExposure,
                                 double netExposure, String message) {
            this.accountId = accountId;
            this.cashBalance = cashBalance;
            this.positions = positions;
            this.grossExposure = grossExposure;
            this.netExposure = netExposure;
            this.message = message;
        }

        public String getAccountId() {
            return accountId;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }

        public double getCashBalance() {
            return cashBalance;
        }

        public void setCashBalance(double cashBalance) {
            this.cashBalance = cashBalance;
        }

        public Map<String, Double> getPositions() {
            return positions;
        }

        public void setPositions(Map<String, Double> positions) {
            this.positions = positions;
        }

        public double getGrossExposure() {
            return grossExposure;
        }

        public void setGrossExposure(double grossExposure) {
            this.grossExposure = grossExposure;
        }

        public double getNetExposure() {
            return netExposure;
        }

        public void setNetExposure(double netExposure) {
            this.netExposure = netExposure;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    private static class AccountState {
        private double cashBalance;
        private final Map<String, Double> positions = new ConcurrentHashMap<>();
        private final Map<String, Double> lastTradePrices = new ConcurrentHashMap<>();
    }
}

