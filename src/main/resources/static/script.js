//// Matches your @RequestMapping("/api/portfolio")
//const API_URL = "http://localhost:8082/api/portfolio";
//
//// ---------------------------------------------------------
//// 1. MAIN LOAD FUNCTION
//// ---------------------------------------------------------
//document.addEventListener("DOMContentLoaded", () => {
//    loadPortfolioData();      // 1. Table
//    loadTotalValue();         // 2. Total Value (Separate Endpoint)
//    loadSectorChart();        // 3. Pie Chart
//    loadHistoryChart();       // 4. Line Graph
//    loadAdvisorSuggestions(); // 5. Advisor
//});
//
//// ---------------------------------------------------------
//// 2. LOAD TABLE (Endpoint: GET /)
//// ---------------------------------------------------------
//async function loadPortfolioData() {
//    try {
//        const response = await fetch(API_URL + '/');
//
//        if (!response.ok) {
//            throw new Error(`HTTP error! status: ${response.status}`);
//        }
//
//        const stocks = await response.json();
//        const tableBody = document.getElementById('portfolio-table-body');
//        tableBody.innerHTML = '';
//
//        if (stocks.length === 0) {
//            tableBody.innerHTML = '<tr><td colspan="6" class="text-center">No stocks found. Add one!</td></tr>';
//            return;
//        }
//
//        stocks.forEach(stock => {
//            const currentValue = stock.currentPrice * stock.quantity;
//
//            // Logic for Color (Green/Red)
//            const isProfit = stock.profitLoss >= 0;
//            const colorClass = isProfit ? 'text-success' : 'text-danger';
//            const sign = isProfit ? '+' : '';
//
//            // --- THE FIX IS IN THE ROW BELOW ---
//            // We changed stock.percentChange to stock.percentageChange
//
//            let trendIcon = '➖'; // Default (Neutral)
//                let trendClass = 'text-muted'; // Gray
//
//                if (stock.trend === 'UP') {
//                    trendIcon = '📈';
//                    trendClass = 'text-success'; // Green
//                } else if (stock.trend === 'DOWN') {
//                    trendIcon = '📉';
//                    trendClass = 'text-danger';  // Red
//                }
//
//            const row = `
//                <tr>
//                    <td class="fw-bold">${stock.ticker}</td>
//                    <td>$${stock.currentPrice.toFixed(2)}</td>
//                    <td class="text-center ${trendClass}" style="font-size: 1.2em;">
//                        ${trendIcon}
//                    </td>
//                    <td>${stock.quantity}</td>
//                    <td>${formatMoney(currentValue)}</td>
//                    <td class="${colorClass}">
//                        ${sign}${stock.profitLoss.toFixed(2)}
//                        <small>(${stock.percentageChange.toFixed(2)}%)</small>
//                    </td>
//                    <td>
//                        <button class="btn btn-sm btn-outline-danger" onclick="deleteStock(${stock.id})">
//                            &times; Sell
//                        </button>
//                    </td>
//                </tr>
//            `;
//            tableBody.innerHTML += row;
//        });
//
//    } catch (error) {
//        console.error("Error loading table:", error);
//        // Show error in table so you know something went wrong
//        document.getElementById('portfolio-table-body').innerHTML =
//            `<tr><td colspan="6" class="text-danger text-center">Error loading data. Check Console.</td></tr>`;
//    }
//}
//
//// ---------------------------------------------------------
//// 3. LOAD TOTAL VALUE (Endpoint: GET /total-value)
//// ---------------------------------------------------------
//async function loadTotalValue() {
//    try {
//        // Matches @GetMapping("/total-value")
//        const response = await fetch(API_URL + '/total-value');
//        const totalValue = await response.json();
//
//        document.getElementById('total-value-display').innerText = formatMoney(totalValue);
//    } catch (error) {
//        console.error("Error loading total value:", error);
//        document.getElementById('total-value-display').innerText = "$0.00";
//    }
//}
//
//// ---------------------------------------------------------
//// 4. ADD STOCK (Endpoint: POST /)
//// ---------------------------------------------------------
//async function addStock() {
//    const ticker = document.getElementById('tickerInput').value.toUpperCase();
//    const price = document.getElementById('buyPriceInput').value;
//    const quantity = document.getElementById('quantityInput').value;
//    const sector = document.getElementById('sectorInput').value;
//
//    if (!ticker || !price || !quantity) {
//        alert("Please fill in all fields!");
//        return;
//    }
//
//    const investmentData = {
//        ticker: ticker,
//        buyPrice: parseFloat(price),
//        quantity: parseInt(quantity),
//        sector: sector
//    };
//
//    try {
//        // Matches @PostMapping("/")
//        const response = await fetch(API_URL + '/', {
//            method: 'POST',
//            headers: { 'Content-Type': 'application/json' },
//            body: JSON.stringify(investmentData)
//        });
//
//        if (response.ok) {
//            alert("Stock Added Successfully!");
//            location.reload();
//        } else {
//            alert("Failed to add stock.");
//        }
//    } catch (error) {
//        console.error("Error adding stock:", error);
//    }
//}
//
//// ---------------------------------------------------------
//// 5. DELETE STOCK (Endpoint: DELETE /{id})
//// ---------------------------------------------------------
//async function deleteStock(id) {
//    if (!confirm("Are you sure you want to sell/delete this stock?")) return;
//
//    try {
//        // Matches @DeleteMapping("/{id}")
//        const response = await fetch(`${API_URL}/${id}`, {
//            method: 'DELETE'
//        });
//
//        if (response.ok) {
//            location.reload();
//        } else {
//            alert("Failed to delete.");
//        }
//    } catch (error) {
//        console.error("Error deleting stock:", error);
//    }
//}
//
//// ---------------------------------------------------------
//// 6. SECTOR CHART (Endpoint: GET /sector-distrinbution)
//// ---------------------------------------------------------
//async function loadSectorChart() {
//    const ctx = document.getElementById('sectorChart').getContext('2d');
//
//    try {
//        // Matches @GetMapping("/sector-distrinbution") <--- Note your typo in Controller
//        const response = await fetch(API_URL + '/sector-distrinbution');
//        const data = await response.json();
//
//        new Chart(ctx, {
//            type: 'doughnut',
//            data: {
//                labels: Object.keys(data),
//                datasets: [{
//                    data: Object.values(data),
//                    backgroundColor: ['#4e73df', '#1cc88a', '#36b9cc', '#f6c23e', '#e74a3b', '#858796'],
//                    hoverOffset: 4
//                }]
//            },
//            options: {
//                maintainAspectRatio: false,
//                plugins: { legend: { position: 'right' } }
//            }
//        });
//    } catch (error) {
//        console.error("Error loading sector chart:", error);
//    }
//}
//
//// ---------------------------------------------------------
//// 7. HISTORY CHART (Endpoint: GET /history/portfolio)
//// ---------------------------------------------------------
//// ---------------------------------------------------------
//// 7. HISTORY CHART (Interactive Version)
//// ---------------------------------------------------------
//async function loadHistoryChart() {
//    const ctx = document.getElementById('historyChart').getContext('2d');
//
//    try {
//        const response = await fetch(API_URL + '/history/portfolio');
//        const data = await response.json();
//
//        new Chart(ctx, {
//            type: 'line',
//            data: {
//                labels: Object.keys(data), // Dates
//                datasets: [{
//                    label: 'Net Worth',
//                    data: Object.values(data), // Values
//                    borderColor: '#4e73df',
//                    backgroundColor: 'rgba(78, 115, 223, 0.1)', // Slight blue fill
//                    borderWidth: 2,
//                    fill: true,
//
//                    // --- INTERACTIVITY FIXES ---
//                    tension: 0.3,          // Curvy line
//                    pointRadius: 4,        // Make points VISIBLE (was 0)
//                    pointHoverRadius: 7,   // Make points BIGGER when hovered
//                    pointBackgroundColor: '#fff',
//                    pointBorderColor: '#4e73df',
//                    pointBorderWidth: 2
//                }]
//            },
//            options: {
//                maintainAspectRatio: false,
//
//                // 1. Better Interaction (Don't need to hover perfectly on the dot)
//                interaction: {
//                    mode: 'index',
//                    intersect: false,
//                },
//
//                plugins: {
//                    legend: { display: false },
//
//                    // 2. Format the Tooltip to show Currency ($)
//                    tooltip: {
//                        callbacks: {
//                            label: function(context) {
//                                let label = context.dataset.label || '';
//                                if (label) {
//                                    label += ': ';
//                                }
//                                if (context.parsed.y !== null) {
//                                    label += new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(context.parsed.y);
//                                }
//                                return label;
//                            }
//                        }
//                    }
//                },
//                scales: {
//                    x: {
//                        display: true, // Show dates on X-axis (Optional, change to false if too crowded)
//                        grid: { display: false },
//                        ticks: { maxTicksLimit: 7 } // Don't crowd the axis with 100 dates
//                    },
//                    y: {
//                        grid: { borderDash: [2] },
//                        ticks: {
//                            // Format Y-axis as Dollars
//                            callback: function(value) {
//                                return '$' + value;
//                            }
//                        }
//                    }
//                },
//
//                // 3. (Optional) CLICK EVENT: If you specifically want to click to see a value
//                onClick: (e, activeElements) => {
//                    if (activeElements.length > 0) {
//                        const index = activeElements[0].index;
//                        const date = Object.keys(data)[index];
//                        const value = Object.values(data)[index];
//                        alert(`On ${date}, your portfolio was worth $${value}`);
//                    }
//                }
//            }
//        });
//    } catch (error) {
//        console.error("Error loading history:", error);
//    }
//}
//
//// ---------------------------------------------------------
//// 8. ADVISOR (Endpoint: GET /recommendations/portfolio)
//// ---------------------------------------------------------
//async function loadAdvisorSuggestions() {
//    const list = document.getElementById('advisor-list');
//
//    try {
//        // Matches @GetMapping("/recommendations/portfolio")
//        const response = await fetch(API_URL + '/recommendations/portfolio');
//        const suggestions = await response.json();
//
//        list.innerHTML = '';
//
//        suggestions.forEach(msg => {
//            let colorClass = 'text-dark';
//
//            if (msg.includes("⚠️") || msg.includes("Risk")) {
//                colorClass = 'text-danger fw-bold';
//            } else if (msg.includes("✅") || msg.includes("Great")) {
//                colorClass = 'text-success fw-bold';
//            } else if (msg.includes("🚀")) {
//                colorClass = 'text-primary fw-bold';
//            }
//
//            const li = `<li class="list-group-item ${colorClass}">${msg}</li>`;
//            list.innerHTML += li;
//        });
//    } catch (error) {
//        console.error("Error loading advisor:", error);
//        list.innerHTML = '<li class="list-group-item text-danger">Advisor unavailable</li>';
//    }
//}
//
//// Helper: Format number as USD Currency
//function formatMoney(amount) {
//    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);
//}
//
//// Global variable to hold temporary data
//let previewData = [];
//
//// 1. UPLOAD & PREVIEW
//async function uploadCSV() {
//    const fileInput = document.getElementById('csvInput');
//    const file = fileInput.files[0];
//    if (!file) return;
//
//    const formData = new FormData();
//    formData.append('file', file);
//
//    try {
//        // Call the PREVIEW endpoint
//        const response = await fetch(API_URL + '/preview-csv', {
//            method: 'POST',
//            body: formData
//        });
//
//        if (!response.ok) throw new Error("Failed to parse CSV");
//
//        // Get the parsed list from Backend
//        previewData = await response.json();
//
//        // Show the Modal
//        renderReviewTable();
//        new bootstrap.Modal(document.getElementById('reviewModal')).show();
//
//    } catch (error) {
//        alert("Error: " + error.message);
//    }
//    fileInput.value = ''; // Reset input
//}
//
//// 2. RENDER THE EDITABLE TABLE
//function renderReviewTable() {
//    const tbody = document.getElementById('review-table-body');
//    tbody.innerHTML = '';
//
//    previewData.forEach((item, index) => {
//        const row = `
//            <tr id="row-${index}">
//                <td><input type="text" class="form-control form-control-sm" value="${item.ticker}" id="ticker-${index}"></td>
//                <td><input type="number" class="form-control form-control-sm" value="${item.buyPrice}" id="price-${index}"></td>
//                <td><input type="number" class="form-control form-control-sm" value="${item.quantity}" id="qty-${index}"></td>
//                <td>
//                    <select class="form-select form-select-sm" id="sector-${index}">
//                        <option value="Technology" ${item.sector === 'Technology' ? 'selected' : ''}>Technology</option>
//                        <option value="Finance" ${item.sector === 'Finance' ? 'selected' : ''}>Finance</option>
//                        <option value="Automotive" ${item.sector === 'Automotive' ? 'selected' : ''}>Automotive</option>
//                        <option value="Healthcare" ${item.sector === 'Healthcare' ? 'selected' : ''}>Healthcare</option>
//                        <option value="Energy" ${item.sector === 'Energy' ? 'selected' : ''}>Energy</option>
//                        <option value="Other" ${item.sector === 'Other' || !item.sector ? 'selected' : ''}>Other</option>
//                    </select>
//                </td>
//                <td>
//                    <button class="btn btn-sm btn-danger" onclick="removePreviewRow(${index})">×</button>
//                </td>
//            </tr>
//        `;
//        tbody.innerHTML += row;
//    });
//}
//
//// 3. REMOVE A ROW FROM PREVIEW (If it's junk data)
//function removePreviewRow(index) {
//    document.getElementById(`row-${index}`).remove();
//    // Mark as null so we skip it later
//    previewData[index] = null;
//}
//
//// 4. SAVE CONFIRMED DATA
//async function confirmBatchUpload() {
//    const finalData = [];
//
//    // Loop through original size to capture edits
//    for (let i = 0; i < previewData.length; i++) {
//        if (previewData[i] === null) continue; // Skip deleted rows
//
//        // Grab values from the INPUT fields
//        const ticker = document.getElementById(`ticker-${i}`).value;
//        const price = document.getElementById(`price-${i}`).value;
//        const qty = document.getElementById(`qty-${i}`).value;
//        const sector = document.getElementById(`sector-${i}`).value;
//
//        if(ticker && price && qty) {
//            finalData.push({
//                ticker: ticker.toUpperCase(),
//                buyPrice: parseFloat(price),
//                quantity: parseInt(qty),
//                sector: sector
//            });
//        }
//    }
//
//    // Send to Batch Save Endpoint
//    try {
//        const response = await fetch(API_URL + '/batch', {
//            method: 'POST',
//            headers: { 'Content-Type': 'application/json' },
//            body: JSON.stringify(finalData)
//        });
//
//        if (response.ok) {
//            alert("Success! Imported " + finalData.length + " stocks.");
//            location.reload();
//        } else {
//            alert("Failed to save data.");
//        }
//    } catch (error) {
//        console.error("Batch save error:", error);
//    }
//}





//Old version but better
// Base URL (No trailing slash here, we add it in the calls)
//const API_URL = "http://localhost:8082/api/portfolio";
//
//// Temporary storage for CSV Review
//let previewData = [];
//
//document.addEventListener("DOMContentLoaded", () => {
//    loadPortfolioData();
//    loadSectorChart();
//    loadPerformanceChart();
//    loadAdvisorSuggestions();
//});
//
//// --- 1. LOAD PORTFOLIO TABLE & STATS ---
//async function loadPortfolioData() {
//    try {
//        console.log("Fetching portfolio...");
//        // NOTE: Added '/' because your controller has @GetMapping("/")
//        const response = await fetch(API_URL + '/');
//
//        if (!response.ok) throw new Error(`HTTP error! Status: ${response.status}`);
//
//        const data = await response.json();
//        console.log("Portfolio Data:", data); // Check Console to see if data arrives
//
//        const tableBody = document.getElementById('portfolio-table-body');
//        if (!tableBody) return; // Safety check
//
//        tableBody.innerHTML = '';
//        let totalValue = 0;
//        let totalPL = 0;
//
//        if (!data || data.length === 0) {
//            tableBody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">No stocks found. Add one to start!</td></tr>';
//            // Update zero stats
//            updateStats(0, 0);
//            return;
//        }
//
//        data.forEach(stock => {
//            // Safety: Default to 0 if null
//            const price = stock.currentPrice || 0;
//            const qty = stock.quantity || 0;
//            const pnl = stock.profitLoss || 0;
//            const change = stock.percentageChange || 0;
//            const ticker = stock.ticker || "UNKNOWN";
//            const id = stock.id;
//
//            // Calculate row value
//            const currentValue = price * qty;
//            totalValue += currentValue;
//            totalPL += pnl;
//
//            // Trend Icon Logic
//            let trendIcon = '➖';
//            let trendClass = 'text-muted';
//            if (stock.trend === 'UP') {
//                trendIcon = '📈';
//                trendClass = 'text-success';
//            } else if (stock.trend === 'DOWN') {
//                trendIcon = '📉';
//                trendClass = 'text-danger';
//            }
//
//            // Color Logic
//            const colorClass = pnl >= 0 ? 'text-success' : 'text-danger';
//            const sign = pnl >= 0 ? '+' : '';
//
//            const row = `
//                <tr>
//                    <td class="fw-bold">${ticker}</td>
//                    <td>$${price.toFixed(2)}</td>
//                    <td class="text-center ${trendClass}" style="font-size: 1.2rem;">${trendIcon}</td>
//                    <td>${qty}</td>
//                    <td>${formatMoney(currentValue)}</td>
//                    <td class="${colorClass}">
//                        ${sign}${pnl.toFixed(2)}
//                        <small>(${change.toFixed(2)}%)</small>
//                    </td>
//                    <td>
//                        <button class="btn btn-sm btn-outline-danger" onclick="deleteStock(${id})">
//                            &times; Sell
//                        </button>
//                    </td>
//                </tr>
//            `;
//            tableBody.innerHTML += row;
//        });
//
//        // Update Top Cards
//        updateStats(totalValue, totalPL);
//
//    } catch (error) {
//        console.error("Error loading portfolio:", error);
//        document.getElementById('portfolio-table-body').innerHTML =
//            `<tr><td colspan="7" class="text-danger text-center">Error: ${error.message}</td></tr>`;
//    }
//}
//
//// Helper to update Top Cards
//function updateStats(value, pl) {
//    const valEl = document.getElementById('total-value');
//    if (valEl) valEl.innerText = formatMoney(value);
//
//    const plEl = document.getElementById('total-pl');
//    if (plEl) {
//        plEl.innerText = (pl >= 0 ? "+" : "") + formatMoney(pl);
//        plEl.className = "fw-bold mb-0 " + (pl >= 0 ? "text-success" : "text-danger");
//    }
//}
//
//// --- 2. ADD STOCK (MANUAL) ---
//const addStockForm = document.getElementById('addStockForm');
//if (addStockForm) {
//    addStockForm.addEventListener('submit', async (e) => {
//        e.preventDefault();
//
//        const stock = {
//            ticker: document.getElementById('ticker').value.toUpperCase(),
//            buyPrice: parseFloat(document.getElementById('buyPrice').value),
//            quantity: parseInt(document.getElementById('quantity').value),
//            sector: document.getElementById('sector').value
//        };
//
//        try {
//            // Added '/' to match @PostMapping("/")
//            const response = await fetch(API_URL + '/', {
//                method: 'POST',
//                headers: { 'Content-Type': 'application/json' },
//                body: JSON.stringify(stock)
//            });
//
//            if (response.ok) {
//                alert("Stock Added Successfully!");
//                location.reload();
//            } else {
//                alert("Error adding stock");
//            }
//        } catch (error) {
//            console.error("Error:", error);
//        }
//    });
//}
//
//// --- 3. DELETE STOCK ---
//async function deleteStock(id) {
//    if (!confirm("Are you sure you want to sell this stock?")) return;
//
//    try {
//        await fetch(`${API_URL}/${id}`, { method: 'DELETE' });
//        loadPortfolioData();
//        loadSectorChart();
//        location.reload(); // Refresh to update totals
//    } catch (error) {
//        console.error("Error deleting stock:", error);
//    }
//}
//
//// --- 4. CSV UPLOAD (PREVIEW PHASE) ---
//async function uploadCSV() {
//    const fileInput = document.getElementById('csvInput');
//    const file = fileInput.files[0];
//    if (!file) return;
//
//    const formData = new FormData();
//    formData.append('file', file);
//
//    try {
//        const response = await fetch(API_URL + '/preview-csv', {
//            method: 'POST',
//            body: formData
//        });
//
//        if (!response.ok) throw new Error("Failed to parse CSV");
//
//        previewData = await response.json();
//        renderReviewTable();
//        new bootstrap.Modal(document.getElementById('reviewModal')).show();
//
//    } catch (error) {
//        alert("Error: " + error.message);
//    }
//    fileInput.value = '';
//}
//
//// --- 5. RENDER REVIEW TABLE ---
//function renderReviewTable() {
//    const tbody = document.getElementById('review-table-body');
//    if (!tbody) return;
//    tbody.innerHTML = '';
//
//    previewData.forEach((item, index) => {
//        if(item === null) return;
//
//        const row = `
//            <tr id="row-${index}">
//                <td><input type="text" class="form-control form-control-sm" value="${item.ticker}" id="ticker-${index}"></td>
//                <td><input type="number" class="form-control form-control-sm" value="${item.buyPrice}" id="price-${index}"></td>
//                <td><input type="number" class="form-control form-control-sm" value="${item.quantity}" id="qty-${index}"></td>
//                <td>
//                    <select class="form-select form-select-sm" id="sector-${index}">
//                        <option value="Technology" ${item.sector === 'Technology' ? 'selected' : ''}>Technology</option>
//                        <option value="Finance" ${item.sector === 'Finance' ? 'selected' : ''}>Finance</option>
//                        <option value="Automotive" ${item.sector === 'Automotive' ? 'selected' : ''}>Automotive</option>
//                        <option value="Healthcare" ${item.sector === 'Healthcare' ? 'selected' : ''}>Healthcare</option>
//                        <option value="Energy" ${item.sector === 'Energy' ? 'selected' : ''}>Energy</option>
//                        <option value="Consumer" ${item.sector === 'Consumer' ? 'selected' : ''}>Consumer</option>
//                        <option value="Other" ${item.sector === 'Other' || !item.sector ? 'selected' : ''}>Other</option>
//                    </select>
//                </td>
//                <td>
//                    <button class="btn btn-sm btn-danger" onclick="removePreviewRow(${index})">×</button>
//                </td>
//            </tr>
//        `;
//        tbody.innerHTML += row;
//    });
//}
//
//function removePreviewRow(index) {
//    document.getElementById(`row-${index}`).remove();
//    previewData[index] = null;
//}
//
//// --- 6. CONFIRM BATCH UPLOAD ---
//async function confirmBatchUpload() {
//    const finalData = [];
//
//    for (let i = 0; i < previewData.length; i++) {
//        if (previewData[i] === null) continue;
//
//        const ticker = document.getElementById(`ticker-${i}`).value;
//        const price = document.getElementById(`price-${i}`).value;
//        const qty = document.getElementById(`qty-${i}`).value;
//        const sector = document.getElementById(`sector-${i}`).value;
//
//        if(ticker && price && qty) {
//            finalData.push({
//                ticker: ticker.toUpperCase(),
//                buyPrice: parseFloat(price),
//                quantity: parseInt(qty),
//                sector: sector
//            });
//        }
//    }
//
//    try {
//        const response = await fetch(API_URL + '/batch', {
//            method: 'POST',
//            headers: { 'Content-Type': 'application/json' },
//            body: JSON.stringify(finalData)
//        });
//
//        if (response.ok) {
//            alert("Success! Imported " + finalData.length + " stocks.");
//            location.reload();
//        } else {
//            alert("Failed to save data.");
//        }
//    } catch (error) {
//        console.error("Batch save error:", error);
//    }
//}
//
//// --- 7. LOAD CHARTS (Corrected URL) ---
//async function loadSectorChart() {
//    try {
//        // NOTE: Matched typo in your Controller: "sector-distrinbution"
//        const response = await fetch(API_URL + '/sector-distrinbution');
//
//        if (!response.ok) throw new Error("Chart data failed");
//
//        const data = await response.json();
//
//        const canvas = document.getElementById('sectorChart');
//        if (!canvas) return;
//
//        const ctx = canvas.getContext('2d');
//
//        // Destroy old chart if exists to prevent overlapping
//        if (window.mySectorChart) window.mySectorChart.destroy();
//
//        window.mySectorChart = new Chart(ctx, {
//            type: 'doughnut',
//            data: {
//                labels: Object.keys(data),
//                datasets: [{
//                    data: Object.values(data),
//                    backgroundColor: ['#4e73df', '#1cc88a', '#36b9cc', '#f6c23e', '#e74a3b', '#858796'],
//                    borderWidth: 1
//                }]
//            },
//            options: {
//                responsive: true,
//                maintainAspectRatio: false,
//                plugins: {
//                    legend: { position: 'bottom' }
//                }
//            }
//        });
//    } catch (error) {
//        console.error("Error loading chart:", error);
//    }
//}
//
//// --- 8. LOAD ADVISOR (Corrected URL) ---
//async function loadAdvisorSuggestions() {
//    try {
//        // NOTE: Updated URL to match your Controller: "/recommendations/portfolio"
//        const response = await fetch(API_URL + '/recommendations/portfolio');
//
//        if (!response.ok) throw new Error("Advisor failed");
//
//        const suggestions = await response.json();
//
//        const list = document.getElementById('advisor-list');
//        if (!list) return;
//
//        list.innerHTML = '';
//
//        if (suggestions.length === 0) {
//            list.innerHTML = '<li>No suggestions yet.</li>';
//            return;
//        }
//
//        suggestions.forEach(suggestion => {
//            const li = document.createElement('li');
//            li.textContent = "• " + suggestion;
//            li.style.marginBottom = "8px";
//            list.appendChild(li);
//        });
//    } catch (error) {
//        console.error("Error loading advisor:", error);
//        const list = document.getElementById('advisor-list');
//        if (list) list.innerHTML = '<li class="text-white-50">Advisor unavailable</li>';
//    }
//}
//
//// --- 9. LOAD PERFORMANCE CHART (Line Chart) ---
//async function loadPerformanceChart() {
//    try {
//        // Fetch from your endpoint: /history/portfolio
//        const response = await fetch(API_URL + '/history/portfolio');
//
//        if (!response.ok) throw new Error("History data failed");
//
//        const data = await response.json();
//
//        // Convert Map { "2023-01-01": 150.00 } to Arrays
//        const labels = Object.keys(data); // Dates
//        const values = Object.values(data); // Prices
//
//        const ctx = document.getElementById('performanceChart').getContext('2d');
//
//        // Create a Gradient for the line (Green fade)
//        const gradient = ctx.createLinearGradient(0, 0, 0, 400);
//        gradient.addColorStop(0, 'rgba(28, 200, 138, 0.5)'); // Top (Green)
//        gradient.addColorStop(1, 'rgba(28, 200, 138, 0.0)'); // Bottom (Transparent)
//
//        new Chart(ctx, {
//            type: 'line',
//            data: {
//                labels: labels,
//                datasets: [{
//                    label: 'Total Portfolio Value ($)',
//                    data: values,
//                    borderColor: '#1cc88a', // Green Line
//                    backgroundColor: gradient, // Gradient Fill
//                    borderWidth: 2,
//                    pointRadius: 3,
//                    pointHoverRadius: 5,
//                    fill: true, // Fills area under line
//                    tension: 0.3 // Makes line smooth (curved)
//                }]
//            },
//            options: {
//                responsive: true,
//                maintainAspectRatio: false,
//                plugins: {
//                    legend: { display: false }, // Hide legend title
//                    tooltip: {
//                        callbacks: {
//                            label: function(context) {
//                                return '$' + context.parsed.y.toFixed(2);
//                            }
//                        }
//                    }
//                },
//                scales: {
//                    x: {
//                        grid: { display: false },
//                        ticks: { maxTicksLimit: 7 } // Show fewer dates
//                    },
//                    y: {
//                        ticks: {
//                            callback: function(value) { return '$' + value; }
//                        }
//                    }
//                }
//            }
//        });
//
//    } catch (error) {
//        console.error("Error loading performance chart:", error);
//    }
//}
//// Helper: Format Money
//function formatMoney(amount) {
//    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);
//}



// --- CONFIGURATION ---
//const API_URL = "http://localhost:8080/api/investments";
//let sectorChartInstance = null;
//let assetChartInstance = null;
//
//// --- INITIALIZATION ---
//document.addEventListener("DOMContentLoaded", () => {
//    fetchPortfolio(); // Load data on start
//});
//
//// --- NAVIGATION LOGIC ---
//function showSection(sectionId, linkElement) {
//    // 1. Hide all sections
//    document.querySelectorAll('.view-section').forEach(el => el.style.display = 'none');
//
//    // 2. Show selected section
//    document.getElementById(sectionId).style.display = 'block';
//
//    // 3. Update Sidebar Active State
//    document.querySelectorAll('.list-group-item').forEach(el => el.classList.remove('active-nav'));
//    linkElement.classList.add('active-nav');
//
//    // 4. Update Header
//    document.getElementById('page-title').innerText = linkElement.innerText.toUpperCase().trim() + " OVERVIEW";
//}
//
//// --- CORE: FETCH DATA ---
//async function fetchPortfolio() {
//    try {
//        const response = await fetch(API_URL);
//        const data = await response.json();
//
//        renderTable(data);
//        updateCharts(data);
//    } catch (error) {
//        console.error("Error fetching data:", error);
//    }
//}
//
//// --- VIEW 1: CHARTS ---
//function updateCharts(investments) {
//    if (!investments || investments.length === 0) return;
//
//    // 1. Prepare Data for Sector Chart
//    const sectorMap = {};
//    investments.forEach(inv => {
//        const val = inv.buyPrice * inv.quantity;
//        sectorMap[inv.sector] = (sectorMap[inv.sector] || 0) + val;
//    });
//
//    const ctx1 = document.getElementById('sectorChart').getContext('2d');
//    if (sectorChartInstance) sectorChartInstance.destroy(); // Prevent overlap
//
//    sectorChartInstance = new Chart(ctx1, {
//        type: 'doughnut',
//        data: {
//            labels: Object.keys(sectorMap),
//            datasets: [{
//                data: Object.values(sectorMap),
//                backgroundColor: ['#4e73df', '#1cc88a', '#36b9cc', '#f6c23e', '#e74a3b', '#858796']
//            }]
//        }
//    });
//
//    // 2. Prepare Data for Asset Chart (Top 5 Assets)
//    const sortedAssets = [...investments].sort((a,b) => (b.buyPrice*b.quantity) - (a.buyPrice*a.quantity)).slice(0,5);
//    const ctx2 = document.getElementById('assetChart').getContext('2d');
//    if (assetChartInstance) assetChartInstance.destroy();
//
//    assetChartInstance = new Chart(ctx2, {
//        type: 'bar',
//        data: {
//            labels: sortedAssets.map(i => i.ticker),
//            datasets: [{
//                label: 'Asset Value ($)',
//                data: sortedAssets.map(i => i.buyPrice * i.quantity),
//                backgroundColor: '#4e73df'
//            }]
//        }
//    });
//}
//
//// --- VIEW 2: TABLE & CRUD ---
//function renderTable(investments) {
//    const list = document.getElementById('investment-list');
//    const emptyMsg = document.getElementById('empty-table-msg');
//    list.innerHTML = '';
//
//    if (investments.length === 0) {
//        emptyMsg.style.display = 'block';
//        return;
//    }
//    emptyMsg.style.display = 'none';
//
//    investments.forEach(inv => {
//        const total = (inv.buyPrice * inv.quantity).toFixed(2);
//        const row = `
//            <tr>
//                <td class="fw-bold">${inv.ticker}</td>
//                <td><span class="badge bg-light text-dark border">${inv.sector}</span></td>
//                <td>$${inv.buyPrice}</td>
//                <td>${inv.quantity}</td>
//                <td class="text-success fw-bold">$${total}</td>
//                <td class="text-end">
//                    <button class="btn btn-sm btn-outline-danger" onclick="deleteInvestment(${inv.id})">
//                        <i class="bi bi-trash"></i>
//                    </button>
//                </td>
//            </tr>
//        `;
//        list.innerHTML += row;
//    });
//}
//
//async function addInvestment(event) {
//    event.preventDefault();
//    const investment = {
//        ticker: document.getElementById('ticker').value.toUpperCase(),
//        buyPrice: parseFloat(document.getElementById('buyPrice').value),
//        quantity: parseInt(document.getElementById('quantity').value),
//        sector: document.getElementById('sector').value
//    };
//
//    await fetch(API_URL, {
//        method: 'POST',
//        headers: { 'Content-Type': 'application/json' },
//        body: JSON.stringify(investment)
//    });
//
//    document.getElementById('add-form').reset();
//    fetchPortfolio(); // Refresh UI
//    alert("Investment Added!");
//}
//
//async function deleteInvestment(id) {
//    if(!confirm("Are you sure?")) return;
//
//    await fetch(`${API_URL}/${id}`, { method: 'DELETE' });
//    fetchPortfolio();
//}
//
//// --- VIEW 3: SIMULATOR (ENDPOINT: /api/investments/simulate) ---
//async function runSimulation() {
//    const trade = {
//        ticker: document.getElementById('sim-ticker').value.toUpperCase(),
//        buyPrice: parseFloat(document.getElementById('sim-price').value),
//        quantity: parseInt(document.getElementById('sim-qty').value),
//        sector: document.getElementById('sim-sector').value
//    };
//
//    if (!trade.ticker || !trade.buyPrice) {
//        alert("Please enter valid details"); return;
//    }
//
//    try {
//        const response = await fetch(`${API_URL}/simulate`, {
//            method: 'POST',
//            headers: { 'Content-Type': 'application/json' },
//            body: JSON.stringify(trade)
//        });
//
//        const data = await response.json();
//
//        // Update UI
//        document.getElementById('sim-empty-state').style.display = 'none';
//        document.getElementById('sim-results').style.display = 'block';
//
//        document.getElementById('sim-cur-val').innerText = formatMoney(data.currentTotalValue);
//        document.getElementById('sim-new-val').innerText = formatMoney(data.simulatedTotalValue);
//
//        const diff = data.valueDifference;
//        const diffEl = document.getElementById('sim-diff');
//        diffEl.innerText = (diff >= 0 ? "+" : "") + formatMoney(diff);
//        diffEl.className = diff >= 0 ? "fs-5 fw-bold text-success" : "fs-5 fw-bold text-danger";
//
//        // Risk Badge
//        const riskBadge = document.getElementById('sim-risk-alert');
//        const riskIcon = document.getElementById('sim-risk-icon');
//        const riskTxt = document.getElementById('sim-risk-level');
//
//        if(data.riskLevel === 'HIGH') {
//            riskBadge.className = "alert alert-danger d-flex align-items-center mb-4";
//            riskIcon.className = "bi bi-exclamation-triangle-fill fs-3 me-3";
//            riskTxt.innerText = "RISK: HIGH";
//        } else if (data.riskLevel === 'MEDIUM') {
//            riskBadge.className = "alert alert-warning d-flex align-items-center mb-4";
//            riskIcon.className = "bi bi-exclamation-circle-fill fs-3 me-3";
//            riskTxt.innerText = "RISK: MEDIUM";
//        } else {
//            riskBadge.className = "alert alert-success d-flex align-items-center mb-4";
//            riskIcon.className = "bi bi-shield-check fs-3 me-3";
//            riskTxt.innerText = "RISK: LOW";
//        }
//        document.getElementById('sim-risk-msg').innerText = data.riskMessage;
//
//        // Concentration Bar
//        const conc = data.highestStockPct || 0;
//        document.getElementById('sim-conc-val').innerText = conc.toFixed(1) + "%";
//        const bar = document.getElementById('sim-conc-bar');
//        bar.style.width = conc + "%";
//        bar.className = conc > 25 ? "progress-bar bg-danger" : "progress-bar bg-info";
//
//    } catch (e) {
//        console.error(e);
//        alert("Simulation failed.");
//    }
//}
//
//function formatMoney(num) {
//    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(num);
//}

// --- CONFIGURATION ---
// 1. Pointing to your specific controller RequestMapping
// --- CONFIGURATION ---
// CHECK YOUR PORT: Ensure your Spring Boot runs on 8082. If it's 8080, change this.
// --- CONFIGURATION ---
const BASE_URL = "http://localhost:8082/api/portfolio";

// Global Chart Instances to prevent "ReferenceError" and "Canvas already in use" errors
let sectorChartInstance = null;
let historyChartInstance = null;

// --- INITIAL LOAD ---
document.addEventListener("DOMContentLoaded", () => {
    console.log("App Started. Testing connection to:", BASE_URL);
    fetchPortfolio();
    // Load advisor suggestions on start too
    loadAdvisorSuggestions();
});

// --- NAVIGATION ---
function showSection(sectionId, linkElement) {
    document.querySelectorAll('.view-section').forEach(el => el.style.display = 'none');
    document.getElementById(sectionId).style.display = 'block';
    document.querySelectorAll('.list-group-item').forEach(el => el.classList.remove('active-nav'));
    linkElement.classList.add('active-nav');
    document.getElementById('page-title').innerText = linkElement.innerText.toUpperCase().trim();
}

// --- DATA FETCHING ---
async function fetchPortfolio() {
    // 1. Fetch Main Table Data
    try {
        const res = await fetch(`${BASE_URL}/`);
        if (!res.ok) throw new Error("Portfolio endpoint returned " + res.status);
        const data = await res.json();
        renderTable(data);
        updateSectorChart(data);
    } catch (err) {
        console.error("CRITICAL: Failed to load portfolio table:", err);
    }

    // 2. Fetch Total Value
    try {
        const res = await fetch(`${BASE_URL}/total-value`);
        const total = await res.json();
        const totalEl = document.getElementById('total-portfolio-value');
        if (totalEl) totalEl.innerText = formatMoney(total);
    } catch (err) {
        console.warn("Minor: Failed to load total value.");
    }

    // 3. Fetch History
    try {
        const res = await fetch(`${BASE_URL}/history/portfolio`);
        const historyData = await res.json();
        updateHistoryChart(historyData);
    } catch (err) {
        console.warn("Minor: Failed to load history chart.");
    }
}

// --- UI RENDERING ---
function renderTable(investments) {
    const list = document.getElementById('investment-list');
    if (!list) return;
    list.innerHTML = '';

    investments.forEach(inv => {
        const currentPrice = inv.currentPrice || inv.buyPrice || 0;
        const pl = inv.profitLoss || 0;
        const pct = inv.percentageChange || 0;
        const trendIcon = inv.trend === "UP" ? "bi-caret-up-fill text-success" : "bi-caret-down-fill text-danger";

        list.innerHTML += `
            <tr>
                <td class="fw-bold">${inv.ticker} <i class="bi ${trendIcon}"></i></td>
                <td><span class="badge bg-light text-dark border">${inv.sector}</span></td>
                <td>
                    <div class="small text-muted">Buy: $${inv.buyPrice.toFixed(2)}</div>
                    <div class="fw-bold">Live: $${currentPrice.toFixed(2)}</div>
                </td>
                <td>${inv.quantity}</td>
                <td>
                    <div class="${pl >= 0 ? 'text-success' : 'text-danger'} fw-bold">${formatMoney(pl)}</div>
                    <small class="${pl >= 0 ? 'text-success' : 'text-danger'}">${pct.toFixed(2)}%</small>
                </td>
                <td class="text-end">
                    <button class="btn btn-sm btn-outline-danger" onclick="deleteInvestment(${inv.id})">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            </tr>`;
    });
}

// --- CHARTING ---
function updateSectorChart(investments) {
    const sectorMap = {};
    investments.forEach(inv => {
        const val = (inv.currentPrice || inv.buyPrice) * inv.quantity;
        sectorMap[inv.sector] = (sectorMap[inv.sector] || 0) + val;
    });

    const canvas = document.getElementById('sectorChart');
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (sectorChartInstance) sectorChartInstance.destroy();

    sectorChartInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: Object.keys(sectorMap),
            datasets: [{
                data: Object.values(sectorMap),
                backgroundColor: ['#4e73df', '#1cc88a', '#36b9cc', '#f6c23e', '#e74a3b']
            }]
        },
        options: {
            maintainAspectRatio: false,
            cutout: '70%'
        }
    });
}

function updateHistoryChart(historyData) {
    const labels = Object.keys(historyData).map(d => d.split('T')[0]);
    const values = Object.values(historyData);

    const canvas = document.getElementById('historyChart');
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (historyChartInstance) historyChartInstance.destroy();

    historyChartInstance = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Portfolio Value',
                data: values,
                borderColor: '#4e73df',
                fill: true,
                backgroundColor: 'rgba(78, 115, 223, 0.1)',
                tension: 0.3
            }]
        },
        options: {
            maintainAspectRatio: false
        }
    });
}

// --- ADVISOR ---
async function loadAdvisorSuggestions() {
    try {
        const response = await fetch(`${BASE_URL}/recommendations/portfolio`);
        const suggestions = await response.json();
        const list = document.getElementById('advisor-list');
        if (!list) return;

        list.innerHTML = '';
        suggestions.forEach(msg => {
            const li = document.createElement('li');
            li.className = "list-group-item small";
            li.textContent = msg;
            list.appendChild(li);
        });
    } catch (error) {
        console.error("Advisor Error:", error);
    }
}

// --- CRUD OPERATIONS ---
async function addInvestment(e) {
    e.preventDefault();
    const inv = {
        ticker: document.getElementById('ticker').value.toUpperCase(),
        buyPrice: parseFloat(document.getElementById('buyPrice').value),
        quantity: parseInt(document.getElementById('quantity').value),
        sector: document.getElementById('sector').value
    };

    try {
        const res = await fetch(`${BASE_URL}/`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(inv)
        });
        if (res.ok) {
            document.getElementById('add-form').reset();
            fetchPortfolio();
        }
    } catch (err) {
        console.error("Add failed:", err);
    }
}

async function deleteInvestment(id) {
    if (confirm("Delete this asset?")) {
        try {
            await fetch(`${BASE_URL}/${id}`, { method: 'DELETE' });
            fetchPortfolio();
        } catch (err) {
            console.error("Delete failed:", err);
        }
    }
}

// --- SIMULATOR ---
async function runSimulation() {
    const trade = {
        ticker: document.getElementById('sim-ticker').value.toUpperCase(),
        buyPrice: parseFloat(document.getElementById('sim-price').value),
        quantity: parseInt(document.getElementById('sim-qty').value),
        sector: document.getElementById('sim-sector').value
    };

    try {
        const response = await fetch(`${BASE_URL}/simulate`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(trade)
        });
        const data = await response.json();

        // Update UI logic for Risk Level
        document.getElementById('sim-empty-state').style.display = 'none';
        document.getElementById('sim-results').style.display = 'block';

        const riskLevelEl = document.getElementById('sim-risk-level');
        riskLevelEl.innerText = data.riskLevel;
        document.getElementById('sim-risk-msg').innerText = data.riskMessage;

        // Visual bar for concentration
        const bar = document.getElementById('sim-conc-bar');
        bar.style.width = data.highestStockPct + "%";
        document.getElementById('sim-conc-val').innerText = data.highestStockPct.toFixed(1) + "%";
        document.getElementById('sim-conc-name').innerText = data.highestStockTicker;

    } catch (err) {
        console.error("Simulation failed:", err);
    }
}

// --- UTILITIES ---
function formatMoney(num) {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(num);
}