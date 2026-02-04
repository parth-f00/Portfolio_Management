# Import the necessary files for PPO or DQN
from ppo_agent import PPOAgent
from dqn_agent import DQNAgent
from portfolio_env import PortfolioEnv

# Function to create and return a PPO trainer
def create_ppo_trainer(tickers, start_date, end_date):
    env = PortfolioEnv(tickers=tickers, start_date=start_date, end_date=end_date)
    agent = PPOAgent(
        state_dim=env.state_dim,
        action_dim=env.action_dim,
        learning_rate=3e-4,
        gamma=0.99,
        gae_lambda=0.95,
        clip_epsilon=0.2,
        entropy_coef=0.01,
        update_epochs=10,
        batch_size=64
    )
    return Trainer(agent, env)

# Function to create and return a DQN trainer
def create_dqn_trainer(tickers, start_date, end_date):
    env = PortfolioEnv(tickers=tickers, start_date=start_date, end_date=end_date)
    agent = DQNAgent(
        state_dim=env.state_dim,
        action_dim=env.action_dim,
        learning_rate=0.001,
        gamma=0.99,
        epsilon_start=1.0,
        epsilon_end=0.01,
        epsilon_decay=0.995,
        memory_size=10000,
        batch_size=64,
        target_update=10
    )
    return Trainer(agent, env)

# Main execution to train the PPO agent (or you could switch to DQN)
if __name__ == "__main__":
    tickers = ['AAPL', 'MSFT', 'GOOGL', 'AMZN']
    start_date = '2020-01-01'
    end_date = '2023-12-31'

    # Choose the trainer to use (PPO or DQN)
    trainer = create_ppo_trainer(tickers, start_date, end_date)  # or use create_dqn_trainer

    # Train the agent
    trainer.train(n_episodes=100, save_frequency=10)

    # Evaluate the model after training
    results = trainer.evaluate(n_episodes=10)

    # Plot training history (portfolio performance over time)
    trainer.plot_training_history()
