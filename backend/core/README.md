# backend/core

Purpose: Core trading mechanics (order matching, execution rules, portfolio tracking).

## Submodule Descriptions

- **Order Matching** (`order_matching`): Handles incoming orders, matches buy/sell requests, and manages order queues.
- **Execution Rules** (`execution_rules`): Applies business logic and configuration to execute matched orders.
- **Portfolio Tracking** (`portfolio_tracking`): Tracks user balances, open positions, and provides analytics and reporting.
- **Volatility Modeling** (`volatility`): Computes volatility metrics for risk management and pricing.
- **Historical Replay** (`simulation`): Simulates historical market data for backtesting strategies.
- **Real-Time Price Feeds** (`feeds`): Ingests and processes live or synthetic market data.
- **Leverage/Margin Calculations** (`calculations`): Calculates leverage and margin requirements for trades.

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


### Order Matching
- **Input:** Order object (JSON or Java class)
- **Output:** Match result (matched order details, status)

### Execution Rules
- **Input:** Order, execution config
- **Output:** Execution result (filled, rejected, partial)

### Portfolio Tracking
- **Input:** Trade events, balance updates
- **Output:** Portfolio state, analytics report

### Volatility Modeling
- **Input:** Price series, window size
- **Output:** Volatility metrics (e.g., standard deviation)

### Historical Replay
- **Input:** Historical dataset, replay speed
- **Output:** Simulated market events

### Real-Time Price Feeds
- **Input:** Feed source, frequency
- **Output:** Price tick data

### Leverage/Margin
- **Input:** Position size, account balance
- **Output:** Leverage ratio, margin requirement

## Notes

- Implement as a Maven/Gradle submodule if splitting into independent deployables.
- Use shared schemas for order, price, volatility, and replay data models (see `../../shared/schemas`).
- Ensure all modules are configurable via `config/application.properties` (e.g., volatility model, replay dataset, feed source).
- Add unit tests for all modules, especially `portfolio_tracking` (PortfolioManager, Analytics).
- Validate data models against shared schemas where applicable.
