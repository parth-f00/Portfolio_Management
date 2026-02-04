from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import yfinance as yf
import numpy as np
import joblib
from tensorflow.keras.models import load_model
import os

app = FastAPI()

# --- LOAD BRAIN ---
try:
    model = load_model("stock_predictor_v2.h5")
    scaler = joblib.load("stock_scaler_v2.pkl")
    print("✅ AI MODEL LOADED SUCCESSFULLY")
except Exception as e:
    print(f"❌ ERROR LOADING MODEL: {e}")
    model = None
    scaler = None

class StockRequest(BaseModel):
    ticker: str

@app.post("/predict")
async def predict_stock(request: StockRequest):
    if not model:
        raise HTTPException(status_code=500, detail="Model not loaded")

    ticker = request.ticker.upper()
    try:
        # Get data
        data = yf.download(ticker, period='3mo', interval='1d', progress=False)
        if len(data) < 60:
            return {"error": "Not enough data"}

        # Prep data
        last_60 = data['Close'].values[-60:].reshape(-1, 1)
        scaled_data = scaler.transform(last_60)
        X_test = np.reshape(np.array([scaled_data]), (1, 60, 1))

        # Predict
        pred_scaled = model.predict(X_test)
        pred_price = scaler.inverse_transform(pred_scaled)

        return {
            "ticker": ticker,
            "predicted_price": round(float(pred_price[0][0]), 2),
            "current_price": round(float(last_60[-1][0]), 2)
        }
    except Exception as e:
        return {"error": str(e)}