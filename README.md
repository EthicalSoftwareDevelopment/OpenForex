# OpenForex
Enterprise‑grade simulator with a Java backend and React frontend, replicating live forex trading. Enables strategy validation, market insight, and risk‑free practice.


## Windows Startup
From `C:\Dev\OpenForex\installation`, PowerShell does **not** run scripts from the current directory unless you prefix them with `./` or `\.\`.

Use one of these:

```powershell
Set-Location C:\Dev\OpenForex\installation
.\start-all.ps1
```

Or use the Windows wrapper:

```powershell
Set-Location C:\Dev\OpenForex\installation
cmd /c start-all.cmd
```

For backend-only startup:

```powershell
Set-Location C:\Dev\OpenForex\installation
.\install-and-run-backend.ps1
```

If you want to run `start-all.ps1` without `\.\`, add `C:\Dev\OpenForex\installation` to your `PATH` or define a PowerShell alias/function in your profile.


## Forex Trading Simulator

## Overview
A platform to simulate foreign exchange trading with realistic market data, risk management tools, and educational features.
Written in Java.

## Objectives
- Provide a safe environment to practice forex trading
- Teach risk management and strategy development
- Support both beginners and advanced users
- Enable AI-driven scenario generation

## Core Modules
### Market Simulation
- [ ] Real-time price feeds (AI: expand into data sources and frequency)
- [ ] Historical replay mode (AI: flesh out timeframes and datasets)
- [ ] Volatility modeling (AI: describe algorithms and parameters)

### Trading Mechanics
- [ ] Order types: market, limit, stop (AI: expand into execution rules)
- [ ] Leverage and margin (AI: flesh out calculation formulas)
- [ ] Portfolio tracking (AI: describe reporting and analytics)

### Risk Management
- [ ] Stop-loss and take-profit (AI: expand into configuration options)
- [ ] Position sizing (AI: detail algorithms and user controls)
- [ ] Risk/reward analysis (AI: describe visualization tools)

### Educational Features
- [ ] Tutorials and guided scenarios (AI: flesh out curriculum design)

## Roadmap
- Phase 1: Core trading mechanics and market simulation
- Phase 2: Risk management and educational modules
- Phase 3: AI-driven scenarios and advanced analytics

## Open Questions
- Should the simulator use real-time live feeds or synthetic data?
- How to balance realism with accessibility for beginners?
- Should gamification elements (leaderboards, badges) be included?
