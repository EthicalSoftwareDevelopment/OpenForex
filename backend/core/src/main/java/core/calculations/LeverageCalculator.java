package core.calculations;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Performs leverage and margin calculations for trading mechanics.
 */
@Path("/leverage")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LeverageCalculator {
    private static final double DEFAULT_ENTRY_PRICE = 1.0d;
    private static final double DEFAULT_MAX_ALLOWED_LEVERAGE = 10.0d;

    @POST
    public Response calculateLeverage(LeverageRequest request) {
        if (request == null) {
            return badRequest("Leverage request is required.");
        }

        String validationMessage = validate(request);
        if (validationMessage != null) {
            return badRequest(validationMessage);
        }

        double entryPrice = request.getEntryPrice() > 0 ? request.getEntryPrice() : DEFAULT_ENTRY_PRICE;
        double notionalValue = request.getPositionSize() * entryPrice;
        double leverageRatio = round(notionalValue / request.getAccountBalance());
        double maxAllowedLeverage = request.getMaxAllowedLeverage() > 0
                ? request.getMaxAllowedLeverage()
                : DEFAULT_MAX_ALLOWED_LEVERAGE;
        double marginRequirement = round(notionalValue / maxAllowedLeverage);
        double freeMargin = round(request.getAccountBalance() - marginRequirement);
        double marginLevel = marginRequirement == 0.0d
                ? 0.0d
                : round((request.getAccountBalance() / marginRequirement) * 100.0d);

        return Response.ok(new LeverageResult(
                round(notionalValue),
                leverageRatio,
                marginRequirement,
                freeMargin,
                marginLevel,
                leverageRatio <= maxAllowedLeverage,
                leverageRatio <= maxAllowedLeverage
                        ? "Leverage is within the configured risk threshold."
                        : "Leverage exceeds the configured risk threshold."))
                .build();
    }

    private Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new LeverageResult(0.0d, 0.0d, 0.0d, 0.0d, 0.0d, false, message))
                .build();
    }

    private String validate(LeverageRequest request) {
        if (request.getPositionSize() <= 0) {
            return "positionSize must be greater than zero.";
        }
        if (request.getAccountBalance() <= 0) {
            return "accountBalance must be greater than zero.";
        }
        if (request.getEntryPrice() < 0) {
            return "entryPrice cannot be negative.";
        }
        if (request.getMaxAllowedLeverage() < 0) {
            return "maxAllowedLeverage cannot be negative.";
        }
        return null;
    }

    private double round(double value) {
        return Math.round(value * 100_000.0d) / 100_000.0d;
    }

    public static class LeverageRequest {
        private double positionSize;
        private double accountBalance;
        private double entryPrice;
        private double maxAllowedLeverage;

        public double getPositionSize() {
            return positionSize;
        }

        public void setPositionSize(double positionSize) {
            this.positionSize = positionSize;
        }

        public double getAccountBalance() {
            return accountBalance;
        }

        public void setAccountBalance(double accountBalance) {
            this.accountBalance = accountBalance;
        }

        public double getEntryPrice() {
            return entryPrice;
        }

        public void setEntryPrice(double entryPrice) {
            this.entryPrice = entryPrice;
        }

        public double getMaxAllowedLeverage() {
            return maxAllowedLeverage;
        }

        public void setMaxAllowedLeverage(double maxAllowedLeverage) {
            this.maxAllowedLeverage = maxAllowedLeverage;
        }
    }

    public static class LeverageResult {
        private double notionalValue;
        private double leverageRatio;
        private double marginRequirement;
        private double freeMargin;
        private double marginLevel;
        private boolean withinRiskLimit;
        private String message;

        public LeverageResult() {
        }

        public LeverageResult(double notionalValue, double leverageRatio, double marginRequirement, double freeMargin,
                              double marginLevel, boolean withinRiskLimit, String message) {
            this.notionalValue = notionalValue;
            this.leverageRatio = leverageRatio;
            this.marginRequirement = marginRequirement;
            this.freeMargin = freeMargin;
            this.marginLevel = marginLevel;
            this.withinRiskLimit = withinRiskLimit;
            this.message = message;
        }

        public double getNotionalValue() {
            return notionalValue;
        }

        public void setNotionalValue(double notionalValue) {
            this.notionalValue = notionalValue;
        }

        public double getLeverageRatio() {
            return leverageRatio;
        }

        public void setLeverageRatio(double leverageRatio) {
            this.leverageRatio = leverageRatio;
        }

        public double getMarginRequirement() {
            return marginRequirement;
        }

        public void setMarginRequirement(double marginRequirement) {
            this.marginRequirement = marginRequirement;
        }

        public double getFreeMargin() {
            return freeMargin;
        }

        public void setFreeMargin(double freeMargin) {
            this.freeMargin = freeMargin;
        }

        public double getMarginLevel() {
            return marginLevel;
        }

        public void setMarginLevel(double marginLevel) {
            this.marginLevel = marginLevel;
        }

        public boolean isWithinRiskLimit() {
            return withinRiskLimit;
        }

        public void setWithinRiskLimit(boolean withinRiskLimit) {
            this.withinRiskLimit = withinRiskLimit;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

