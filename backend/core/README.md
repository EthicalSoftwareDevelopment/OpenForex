# backend/core

Purpose: Core trading mechanics (order matching, execution rules, portfolio tracking).

## Directory Structure

- src/main/java/core/order_matching/OrderMatcher.java
- src/main/java/core/order_matching/OrderQueue.java
- src/main/java/core/execution_rules/OrderExecutor.java
- src/main/java/core/execution_rules/ExecutionConfig.java
- src/main/java/core/portfolio_tracking/PortfolioManager.java
- src/main/java/core/portfolio_tracking/Analytics.java
- src/main/java/core/volatility/VolatilityModel.java
- src/main/java/core/simulation/HistoricalReplay.java
- src/main/java/core/feeds/RealTimePriceFeed.java
- src/main/java/core/calculations/LeverageCalculator.java
- config/application.properties
- src/test/java/core/order_matching/OrderMatcherTest.java
- src/test/java/core/execution_rules/OrderExecutorTest.java
- src/test/java/core/volatility/VolatilityModelTest.java
- src/test/java/core/simulation/HistoricalReplayTest.java
- src/test/java/core/feeds/RealTimePriceFeedTest.java
- src/test/java/core/calculations/LeverageCalculatorTest.java

## API Contracts

- Order matching: Accepts order objects, returns match results.
- Execution rules: Executes orders based on type and config.
- Portfolio tracking: Tracks balances, positions, and analytics.
- Volatility modeling: Computes and provides volatility metrics.
- Historical replay: Simulates market data for backtesting.
- Real-time price feeds: Ingests and processes live or synthetic data.
- Leverage/margin: Calculates leverage and margin requirements.

## Notes
- Implement as a Maven/Gradle submodule if splitting into independent deployables.
- Use shared schemas for order, price, volatility, and replay data models.
