import gym
import numpy as np
import pandas as pd
import yfinance as yf


class PortfolioEnv(gym.Env):
    def __init__(self, tickers, start_date, end_date, initial_balance=100000, transaction_cost=0.001, window_size=30,
                 normalize_features=True):
        super(PortfolioEnv, self).__init__()

        self.tickers = tickers
        self.start_date = start_date
        self.end_date = end_date
        self.initial_balance = initial_balance
        self.transaction_cost = transaction_cost
        self.window_size = window_size
        self.normalize_features = normalize_features

        # Download data from Yahoo Finance
        self.data = self.get_data()

        # Number of assets (including cash)
        self.n_assets = len(tickers) + 1  # Assets + cash

        # Action space: Portfolio weights for each asset and cash
        self.action_space = gym.spaces.Box(low=0, high=1, shape=(self.n_assets,), dtype=np.float32)

        # State space: Portfolio state (normalized price features + portfolio info)
        self.state_space = self.n_assets * (window_size + 1)

        # Initialize other environment variables
        self.reset()

    def get_data(self):
        # Download historical stock data
        data = {}
        for ticker in self.tickers:
            data[ticker] = yf.download(ticker, start=self.start_date, end=self.end_date)['Adj Close']

        # Combine all asset data into a DataFrame
        df = pd.DataFrame(data)
        df.fillna(method='ffill', inplace=True)
        return df

    def reset(self):
        # Reset environment (initial state)
        self.current_step = self.window_size
        self.balance = self.initial_balance
        self.portfolio_value = self.initial_balance
        self.shares = np.zeros(len(self.tickers))
        self.cash = self.initial_balance
        self.total_value = self.initial_balance
        self.done = False
        self.history = []

        # Return the initial state
        return self.get_state()

    def get_state(self):
        # Calculate state features: price data and portfolio information
        prices = self.data.iloc[self.current_step - self.window_size: self.current_step].values
        portfolio_state = np.concatenate([prices.flatten(), [self.balance, self.cash, self.total_value]])

        if self.normalize_features:
            portfolio_state = portfolio_state / np.max(portfolio_state)  # Normalize features

        return portfolio_state

    def step(self, action):
        # Execute portfolio action (buy/sell)
        prev_balance = self.balance
        prev_portfolio_value = self.portfolio_value

        # Normalize action to sum to 1 (portfolio weights)
        action = action / np.sum(action)

        # Calculate portfolio weights (buy/sell based on action)
        for i in range(len(self.tickers)):
            asset_weight = action[i]
            asset_value = asset_weight * self.total_value
            asset_price = self.data.iloc[self.current_step][self.tickers[i]]
            # Calculate number of shares to buy/sell
            shares_to_trade = (asset_value - (self.shares[i] * asset_price)) / asset_price
            self.shares[i] += shares_to_trade
            # Transaction cost
            self.balance -= abs(shares_to_trade) * asset_price * self.transaction_cost

        # Update portfolio value (cash + stock values)
        self.portfolio_value = sum(self.shares[i] * self.data.iloc[self.current_step][self.tickers[i]] for i in
                                   range(len(self.tickers))) + self.balance
        self.total_value = self.portfolio_value + self.cash

        # Calculate reward (portfolio value change)
        reward = np.log(self.portfolio_value / prev_portfolio_value)

        # Update state and step
        self.current_step += 1
        if self.current_step >= len(self.data):
            self.done = True

        return self.get_state(), reward, self.done, {}

    def render(self):
        print(f"Step: {self.current_step}")
        print(f"Portfolio Value: {self.portfolio_value}")
        print(f"Balance: {self.balance}")
        print(f"Cash: {self.cash}")
        print(f"Shares: {self.shares}")
