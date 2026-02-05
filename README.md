# Portfolio Management

## Overview

The Portfolio Management application allows users to manage their financial portfolios by tracking investments in stocks, bonds, mutual funds, and other assets. The system helps users make informed decisions by providing key financial data, performance tracking, and the ability to diversify their investments for optimal risk management.

## Features

- **Track Investments**: Add and manage various asset classes (stocks, bonds, mutual funds, etc.).
- **Portfolio Performance**: View historical performance of assets and overall portfolio performance.
- **Real-time Market Data**: Integrate with live financial data sources to fetch up-to-date market information.
- **Risk Analysis**: Get insights into portfolio risk based on diversification, correlation of assets, and other metrics.
- **LSTM Model**: LSTM model for accurate stock price prediction.
- **AI-Bot**: Bot was made to give a customized experience for the user. 

## Technologies Used

### Frontend:
- **HTML, CSS**: For basic structure and styling.
- **Vanilla.js**: For visualizing portfolio data with interactive charts.
- **BootStrap**: For ready made templates for additional styling and enhancements. 

### Backend:
- **SpringBoot**: For the backend.
- **MySQL**: For database management.
- **RESTful API**: For communication between frontend and backend.
- **Python**: For Connecting to a Flask server.

### External APIs:
- **FinHub** or **Yahoo Finance API**: For retrieving real-time stock data.

### Installation 
1. Clone the repository: https://github.com/parth-f00/Portfolio_Management
2. Navigate to the project directory:
   cd portfolio-management
3. Install the backend dependencies: npm install
4. Set up environement variables:
   create a .env file in the root directory and add the following variables:
   DB_URI=your-mongodb-uri
  JWT_SECRET=your-jwt-secret
  ALPHA_VANTAGE_API_KEY=your-alpha-vantage-api-key
5. Start the backend server:
   npm start
6. For the frontend:
7. Navigate to the client directory:
   cd client 

   

