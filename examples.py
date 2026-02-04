# Example of training a PPO agent
from train import create_ppo_trainer

tickers = ['AAPL', 'MSFT', 'GOOGL', 'AMZN']
start_date = '2020-01-01'
end_date = '2023-12-31'

trainer = create_ppo_trainer(tickers, start_date, end_date)
trainer.train(n_episodes=100, save_frequency=10)
trainer.plot_training_history()

# Example of training a DQN agent
from train import create_dqn_trainer

trainer = create_dqn_trainer(tickers, start_date, end_date)
trainer.train(n_episodes=100)
trainer.plot_training_history()
