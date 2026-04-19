package core.portfolio_tracking;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Analytics.
 */
public class AnalyticsTest {
    @Test
    void testAnalyticsInitialization() {
        Analytics analytics = new Analytics();
        assertNotNull(analytics);
    }
}

