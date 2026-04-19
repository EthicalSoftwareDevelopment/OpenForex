package core.execution_rules;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for execution rules.
 */
public class ExecutionConfig {
    private double maxSlippagePips = 0.0015d;
    private long maxExecutionLatencyMs = 250L;
    private boolean allowPartialFill = true;
    private boolean allowAfterHours = false;
    private boolean requirePriceImprovement = false;

    public ExecutionConfig() {
    }

    public static ExecutionConfig defaultConfig() {
        return new ExecutionConfig();
    }

    public List<String> validate() {
        List<String> validationErrors = new ArrayList<>();
        if (maxSlippagePips < 0) {
            validationErrors.add("maxSlippagePips must be greater than or equal to zero.");
        }
        if (maxExecutionLatencyMs < 0) {
            validationErrors.add("maxExecutionLatencyMs must be greater than or equal to zero.");
        }
        return validationErrors;
    }

    public double getMaxSlippagePips() {
        return maxSlippagePips;
    }

    public void setMaxSlippagePips(double maxSlippagePips) {
        this.maxSlippagePips = maxSlippagePips;
    }

    public long getMaxExecutionLatencyMs() {
        return maxExecutionLatencyMs;
    }

    public void setMaxExecutionLatencyMs(long maxExecutionLatencyMs) {
        this.maxExecutionLatencyMs = maxExecutionLatencyMs;
    }

    public boolean isAllowPartialFill() {
        return allowPartialFill;
    }

    public void setAllowPartialFill(boolean allowPartialFill) {
        this.allowPartialFill = allowPartialFill;
    }

    public boolean isAllowAfterHours() {
        return allowAfterHours;
    }

    public void setAllowAfterHours(boolean allowAfterHours) {
        this.allowAfterHours = allowAfterHours;
    }

    public boolean isRequirePriceImprovement() {
        return requirePriceImprovement;
    }

    public void setRequirePriceImprovement(boolean requirePriceImprovement) {
        this.requirePriceImprovement = requirePriceImprovement;
    }
}

