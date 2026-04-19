package core.execution_rules;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExecutionConfigTest {
    @Test
    void providesSaneDefaults() {
        ExecutionConfig config = ExecutionConfig.defaultConfig();

        assertTrue(config.isAllowPartialFill());
        assertFalse(config.isRequirePriceImprovement());
        assertTrue(config.validate().isEmpty());
    }

    @Test
    void reportsInvalidNumericSettings() {
        ExecutionConfig config = new ExecutionConfig();
        config.setMaxSlippagePips(-1d);
        config.setMaxExecutionLatencyMs(-5L);

        assertEquals(2, config.validate().size());
    }
}

