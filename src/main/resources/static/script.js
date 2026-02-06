
const API_URL = "http://localhost:8082/api/portfolio";
const PREDICT_URL = "http://localhost:8082/api/prediction";

let sectorChartInstance = null;
let historyChartInstance = null;


document.addEventListener("DOMContentLoaded", () => {

    if(localStorage.getItem('theme') === 'dark') {
        document.body.classList.add('dark-mode');
        document.getElementById('theme-btn').innerHTML = '<i class="bi bi-sun-fill"></i>';
        Chart.defaults.color = '#e0e0e0';
        Chart.defaults.borderColor = '#444';
    } else {
        Chart.defaults.color = '#666';
        Chart.defaults.borderColor = '#ddd';
    }

    // 2. Set Default Date in Add Form
    const dateInput = document.getElementById('purchaseDate');
    if(dateInput) {
        dateInput.value = new Date().toISOString().split('T')[0];
    }

    // 3. Fetch Data
    fetchDashboardData();
    fetchCharts();
    fetchAdvisor();

    // 4. SCROLL SPY
    initScrollSpy();
});

// SCROLL SPY LOGIC
function initScrollSpy() {
    const sections = document.querySelectorAll('div[id^="section-"]');
    const navLinks = document.querySelectorAll('.list-group-item');

    const options = {
        root: null,
        rootMargin: '0px',
        threshold: [0, 0.25, 0.5, 0.75, 1]
    };

    const observer = new IntersectionObserver((entries) => {
        let maxRatio = 0;
        let activeId = '';

        sections.forEach(section => {
            const rect = section.getBoundingClientRect();
            const visibleHeight = Math.min(window.innerHeight, rect.bottom) - Math.max(0, rect.top);

            if (visibleHeight > 0) {
                if (visibleHeight > maxRatio) {
                    maxRatio = visibleHeight;
                    activeId = section.id;
                }
            }
        });

        if (activeId) {
            navLinks.forEach(link => {
                link.classList.remove('active-nav');
                const onclickVal = link.getAttribute('onclick');
                if (onclickVal && onclickVal.includes(activeId)) {
                    link.classList.add('active-nav');
                }
            });
        }
    }, options);

    sections.forEach(section => observer.observe(section));
}

//  NAVIGATION
function scrollToSection(id) {
    const el = document.getElementById(id);
    if(el) {
        el.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
}

//  THEME TOGGLE
function toggleTheme() {
    document.body.classList.toggle('dark-mode');
    const isDark = document.body.classList.contains('dark-mode');
    localStorage.setItem('theme', isDark ? 'dark' : 'light');
    document.getElementById('theme-btn').innerHTML = isDark ? '<i class="bi bi-sun-fill"></i>' : '<i class="bi bi-moon-stars-fill"></i>';
    fetchCharts();
}

//  DATA FETCHING
async function fetchDashboardData() {
    try {
        const valRes = await fetch(`${API_URL}/total-value`);
        const totalVal = await valRes.json();
        document.getElementById('total-value').innerText = formatMoney(totalVal);

        const res = await fetch(`${API_URL}/`);
        const data = await res.json();

        data.sort((a,b)=>{
         new Date(b.purchaseDate)-new Date(a.purchaseDate);
        })

        document.getElementById('total-holdings').innerText = data.length;
        renderGroupedTable(data);

        const secRes = await fetch(`${API_URL}/sector-distrinbution`);
        const secData = await secRes.json();
        let topSec = "-";
        let maxVal = 0;
        for (const [sec, val] of Object.entries(secData)) {
            if (val > maxVal) { maxVal = val; topSec = sec; }
        }
        document.getElementById('top-sector').innerText = topSec;

    } catch (e) { console.error("Error fetching dashboard:", e); }
}

function renderGroupedTable(investments) {
    const list = document.getElementById('investment-list');
    const emptyMsg = document.getElementById('empty-table-msg');
    list.innerHTML = '';

    if (!investments || investments.length === 0) {
        emptyMsg.style.display = 'block';
        return;
    }
    emptyMsg.style.display = 'none';

    const groups = {};
    investments.forEach(inv => {
        if (!groups[inv.ticker]) {
            groups[inv.ticker] = {
                ticker: inv.ticker,
                sector: inv.sector,
                currentPrice: inv.currentPrice,
                trend: inv.trend,
                totalQty: 0,
                totalInvested: 0,
                totalPL: 0,
                entries: []
            };
        }
        const g = groups[inv.ticker];
        g.totalQty += inv.quantity;
        g.totalInvested += (inv.buyPrice * inv.quantity);
        g.totalPL += inv.profitLoss;
        g.entries.push(inv);
    });

    Object.values(groups).forEach(group => {
        const avgPrice = group.totalInvested / group.totalQty;
        const plColor = group.totalPL >= 0 ? 'text-success' : 'text-danger';
        const trendIcon = group.trend === 'UP' ? '<i class="bi bi-arrow-up-circle-fill text-success"></i>' : (group.trend === 'DOWN' ? '<i class="bi bi-arrow-down-circle-fill text-danger"></i>' : '<i class="bi bi-dash-circle text-muted"></i>');
        const collapseId = `collapse-${group.ticker}`;

//         Added the Predict Button Column
        const parentRow = `
            <tr class="clickable-row fw-bold" onclick="toggleCollapse('${collapseId}', this)">
                <td>${group.ticker} <span class="badge bg-light text-dark border ms-1">${group.sector}</span></td>
                <td>${group.totalQty}</td>
                <td>${formatMoney(avgPrice)}</td>
                <td>${formatMoney(group.currentPrice)}</td>
                <td class="${plColor}">${formatMoney(group.totalPL)}</td>
                <td class="fs-5">${trendIcon}</td>

                <td>
                    <button class="btn btn-sm btn-outline-primary py-0 px-2 shadow-sm"
                            onclick="predictStock('${group.ticker}'); event.stopPropagation();"
                            title="Run AI Prediction">
                        <i class="bi bi-robot"></i> Forecast
                    </button>
                </td>

                <td><i class="bi bi-chevron-down text-muted transition-icon"></i></td>
            </tr>
        `;

        let childRows = '';
        group.entries.forEach(inv => {
            let dateStr = 'N/A';
            if (inv.purchaseDate) {
                if (Array.isArray(inv.purchaseDate)) {
                    dateStr = `${inv.purchaseDate[1]}-${inv.purchaseDate[2]}-${inv.purchaseDate[0]}`;
                } else {
                    const d = new Date(inv.purchaseDate);
                    dateStr = `${d.getMonth()+1}-${d.getDate()}-${d.getFullYear()}`;
                }
            }

             childRows += `
                <tr>
                    <td class="text-muted small ps-4"><i class="bi bi-arrow-return-right"></i> Buy: ${formatMoney(inv.buyPrice)}</td>
                    <td class="text-muted small">Qty: ${inv.quantity}</td>
                    <td class="text-muted small">Date: ${dateStr}</td>
                    <td colspan="4"></td>
                    <td class="text-end">
                        <button class="btn btn-sm btn-outline-danger border-0 p-0" onclick="deleteInvestment(${inv.id})">
                            <i class="bi bi-x-circle"></i> Remove
                        </button>
                    </td>
                </tr>
             `;
        });

        const detailContainer = `
            <tr id="${collapseId}" style="display:none;" class="bg-light">
                <td colspan="8" class="p-0">
                    <table class="table table-sm mb-0" style="background-color: transparent;">
                       ${childRows}
                    </table>
                </td>
            </tr>
        `;
        list.innerHTML += parentRow + detailContainer;
    });
}

function toggleCollapse(id, rowElement) {
    const el = document.getElementById(id);
    const icon = rowElement.querySelector('.bi-chevron-down, .bi-chevron-up');
    if (el.style.display === "none") {
        el.style.display = "table-row";
        rowElement.classList.add("expanded-row");
        if(icon) { icon.classList.remove('bi-chevron-down'); icon.classList.add('bi-chevron-up'); }
    } else {
        el.style.display = "none";
        rowElement.classList.remove("expanded-row");
        if(icon) { icon.classList.remove('bi-chevron-up'); icon.classList.add('bi-chevron-down'); }
    }
}


async function predictStock(ticker) {
    const btn = event.target.closest('button');
    const originalHTML = btn.innerHTML;

    // 1. Loading State
    btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span>';
    btn.disabled = true;

    try {
        // 2. Call API
        const response = await fetch(`${PREDICT_URL}/${ticker}`, {
            method: 'POST'
        });

        if (!response.ok) throw new Error("AI Service Unavailable");
        const data = await response.json();

        // 3. Math Logic
        const current = data.current_price;
        const predicted = data.predicted_price;
        const diff = predicted - current;
        const percent = ((diff / current) * 100).toFixed(2);
        const isBullish = diff >= 0;

        // 4. Update Modal Elements
        document.getElementById('pred-ticker').innerText = data.ticker;
        document.getElementById('pred-current').innerText = "$" + current.toFixed(2);
        document.getElementById('pred-future').innerText = "$" + predicted.toFixed(2);
        document.getElementById('pred-percent').innerText = (isBullish ? "+" : "") + percent + "%";

        // 5. Dynamic Styling (Green vs Red)
        const header = document.getElementById('pred-modal-header');
        const signalBadge = document.getElementById('pred-signal');
        const verdictCard = document.getElementById('pred-verdict-card'); // Optional if you want colored background

        if (isBullish) {
            header.className = "modal-header text-white bg-success"; // Green Header
            signalBadge.className = "badge rounded-pill mt-2 px-3 py-2 bg-success";
            signalBadge.innerText = "BUY SIGNAL 🚀";
            document.getElementById('pred-percent').className = "display-6 fw-bold mb-0 text-success";
        } else {
            header.className = "modal-header text-white bg-danger"; // Red Header
            signalBadge.className = "badge rounded-pill mt-2 px-3 py-2 bg-danger";
            signalBadge.innerText = "SELL SIGNAL 🔻";
            document.getElementById('pred-percent').className = "display-6 fw-bold mb-0 text-danger";
        }

        // 6. Show Modal
        const modal = new bootstrap.Modal(document.getElementById('predictionModal'));
        modal.show();

    } catch (error) {
        console.error(error);
        alert("Error: " + error.message);
    } finally {
        // 7. Reset Button
        btn.innerHTML = originalHTML;
        btn.disabled = false;
    }
}

async function fetchCharts() {
    const isDark = document.body.classList.contains('dark-mode');
    const textColor = isDark ? '#e0e0e0' : '#666';

    const secRes = await fetch(`${API_URL}/sector-distrinbution`);
    const secData = await secRes.json();

    const histRes = await fetch(`${API_URL}/history/portfolio`);
    const histData = await histRes.json();

    const ctx1 = document.getElementById('sectorChart').getContext('2d');
    if (sectorChartInstance) sectorChartInstance.destroy();

    sectorChartInstance = new Chart(ctx1, {
        type: 'doughnut',
        data: {
            labels: Object.keys(secData),
            datasets: [{
                data: Object.values(secData),
                backgroundColor: ['#4e73df', '#1cc88a', '#36b9cc', '#f6c23e', '#e74a3b'],
                borderColor: isDark ? '#1e1e1e' : '#ffffff',
                borderWidth: 2
            }]
        },
        options: {
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: { color: textColor, usePointStyle: true, padding: 20, font: { size: 12 } }
                }
            },
            cutout: '70%',
        }
    });

    const ctx2 = document.getElementById('historyChart').getContext('2d');
    let gradient = ctx2.createLinearGradient(0, 0, 0, 400);
    gradient.addColorStop(0, 'rgba(78, 115, 223, 0.5)');
    gradient.addColorStop(1, 'rgba(78, 115, 223, 0.0)');

    if (historyChartInstance) historyChartInstance.destroy();

    historyChartInstance = new Chart(ctx2, {
        type: 'line',
        data: {
            labels: Object.keys(histData),
            datasets: [{
                label: 'Portfolio Value',
                data: Object.values(histData),
                borderColor: '#4e73df',
                borderWidth: 2,
                backgroundColor: gradient,
                tension: 0.3,
                fill: true,
                pointRadius: 0,
                pointHoverRadius: 6,
                pointBackgroundColor: '#4e73df'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            interaction: { mode: 'index', intersect: false },
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: isDark ? '#333' : '#fff',
                    titleColor: isDark ? '#fff' : '#333',
                    bodyColor: isDark ? '#fff' : '#333',
                    borderColor: '#ccc',
                    borderWidth: 1,
                    callbacks: {
                        title: function(tooltipItems) {
                            const rawDate = tooltipItems[0].label;
                            const d = new Date(rawDate);
                            return `${("0" + d.getDate()).slice(-2)}-${("0" + (d.getMonth() + 1)).slice(-2)}-${d.getFullYear()}`;
                        },
                        label: function(context) {
                            let label = context.dataset.label || '';
                            if (label) { label += ': '; }
                            if (context.parsed.y !== null) {
                                label += new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(context.parsed.y);
                            }
                            return label;
                        }
                    }
                }
            },
            scales: {
                x: {
                    display: true,
                    grid: { display: false },
                    ticks: {
                        color: textColor,
                        maxTicksLimit: 6,
                        callback: function(val, index) {
                            const label = this.getLabelForValue(val);
                            const d = new Date(label);
                            return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
                        }
                    }
                },
                y: { display: true, grid: { display: false }, ticks: { display: true } }
            }
        }
    });
}

async function fetchAdvisor() {
    const res = await fetch(`${API_URL}/recommendations/portfolio`);
    const suggestions = await res.json();
    const list = document.getElementById('advisor-list');
    list.innerHTML = "";

    suggestions.forEach(s => {
        let icon = 'bi-info-circle';
        let colorClass = 'text-primary';
        let text = s.toLowerCase();

        if (text.includes("high")) {
            icon = 'bi-exclamation-triangle-fill';
            colorClass = 'text-danger';
        } else if (text.includes("low")) {
            icon = 'bi-arrow-down-circle';
            colorClass = 'text-info';
        } else if (text.includes("balanced") || text.includes("good work")) {
            icon = 'bi-check-circle-fill';
            colorClass = 'text-success';
        }

        list.innerHTML += `
            <li class="list-group-item d-flex align-items-start">
                <i class="bi ${icon} ${colorClass} me-3 mt-1 fs-5"></i>
                <span class="${colorClass === 'text-danger' ? 'text-danger' : ''}">${s}</span>
            </li>
        `;
    });
}

// ✅ NEW: Helper for Toast Notifications
function showToast(message) {
    const toastEl = document.getElementById('liveToast');
    const msgEl = document.getElementById('toast-msg');

    // Set message
    msgEl.innerText = message;

    // Show using Bootstrap API
    const toast = new bootstrap.Toast(toastEl);
    toast.show();
}



async function addInvestment(e) {
    e.preventDefault();
    const payload = {
        ticker: document.getElementById('ticker').value.toUpperCase(),
        buyPrice: parseFloat(document.getElementById('buyPrice').value),
        quantity: parseInt(document.getElementById('quantity').value),
        sector: document.getElementById('stockSector').value,
        purchaseDate: document.getElementById('purchaseDate').value + "T00:00:00"
    };
    await fetch(API_URL + "/", {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });
//    alert("Asset Added!");  alert accha nhi hai

    showToast("Asset added successfully")
    document.getElementById('add-form').reset();
    document.getElementById('purchaseDate').value = new Date().toISOString().split('T')[0];
    fetchDashboardData();
    fetchCharts();
    fetchAdvisor();
}

let idToDelete=null;
function deleteInvestment(id){
    idToDelete=id;
    const modal= new bootstrap.Modal(document.getElementById('deleteModal'));
    modal.show();
}

async function confirmDelete(){
    if(!idToDelete) return;
    try{
        const modalEl=document.getElementById('deleteModal');
        const modal=bootstrap.Modal.getInstance(modalEl);
        modal.hide();

        await fetch(`${API_URL}/${idToDelete}`,{method:'DELETE'})

        showToast("Asset removed successfully");
        fetchDashboardData();
        fetchCharts();
        fetchAdvisor();
    }
    catch(error){
    console.error(error);
    }finally{
    idToDelete=null;
    }
}


async function uploadCsv() {
    const fileInput = document.getElementById('csv-file-input');
    const file = fileInput.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append("file", file);
    const res = await fetch(`${API_URL}/preview-csv`, { method: 'POST', body: formData });
    const rawList = await res.json();
    const tbody = document.getElementById('csv-preview-body');
    tbody.innerHTML = "";
    rawList.forEach((inv, index) => {
        tbody.innerHTML += `
            <tr data-index="${index}">
                <td><input type="text" class="form-control form-control-sm csv-ticker" value="${inv.ticker}"></td>
                <td><input type="number" class="form-control form-control-sm csv-price" value="${inv.buyPrice}"></td>
                <td><input type="number" class="form-control form-control-sm csv-qty" value="${inv.quantity}"></td>
                <td><input type="text" class="form-control form-control-sm csv-sector" value="${inv.sector}"></td>
            </tr>`;
    });
    const modal = new bootstrap.Modal(document.getElementById('csvPreviewModal'));
    modal.show();
}

async function confirmCsvUpload() {
    const rows = document.querySelectorAll('#csv-preview-body tr');
    const finalData = Array.from(rows).map(row => {
        return {
            ticker: row.querySelector('.csv-ticker').value.toUpperCase(),
            buyPrice: parseFloat(row.querySelector('.csv-price').value),
            quantity: parseInt(row.querySelector('.csv-qty').value),
            sector: row.querySelector('.csv-sector').value
        };
    });
    await fetch(`${API_URL}/batch`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(finalData)
    });
    alert("Batch Upload Successful!");
    location.reload();
}

async function runSimulation() {
    const trade = {
        ticker: document.getElementById('sim-ticker').value.toUpperCase(),
        buyPrice: parseFloat(document.getElementById('sim-price').value),
        quantity: parseInt(document.getElementById('sim-qty').value),
        sector: document.getElementById('sim-sector').value
    };

    try {
        const res = await fetch(`${API_URL}/simulate`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(trade)
        });
        const data = await res.json();

        document.getElementById('sim-results').style.display = 'block';

        const riskBadge = document.getElementById('sim-risk-icon-box');
        const riskTitle = document.getElementById('sim-risk-level');
        const riskMsg = document.getElementById('sim-risk-msg');

        // Reset classes
        riskBadge.className = "rounded-circle p-3 me-3 text-white";

        // ✅ NEW: Handle all 5 Risk Levels from Java
        switch (data.riskLevel) {
            case 'CRITICAL':
                riskBadge.classList.add('bg-dark'); // Black/Dark Grey for Critical
                riskTitle.innerText = "CRITICAL RISK 🚨";
                break;
            case 'HIGH':
                riskBadge.classList.add('bg-danger'); // Red
                riskTitle.innerText = "HIGH RISK";
                break;
            case 'MEDIUM':
                riskBadge.classList.remove('text-white');
                riskBadge.classList.add('text-dark', 'bg-warning'); // Yellow
                riskTitle.innerText = "MEDIUM RISK";
                break;
            case 'EXCELLENT':
                riskBadge.classList.add('bg-primary'); // Blue
                riskTitle.innerText = "EXCELLENT 💎";
                break;
            default: // LOW
                riskBadge.classList.add('bg-success'); // Green
                riskTitle.innerText = "HEALTHY";
        }

        riskMsg.innerText = data.riskMessage;

        // Concentration Bar Logic
        const conc = data.highestStockPct;
        document.getElementById('sim-conc-name').innerText = data.highestStockTicker;
        document.getElementById('sim-conc-val').innerText = conc.toFixed(1) + "%";

        const bar = document.getElementById('sim-conc-bar');
        bar.style.width = conc + "%";

        // Color the bar based on severity
        bar.className = "progress-bar";
        if (conc > 50) bar.classList.add("bg-dark");
        else if (conc > 30) bar.classList.add("bg-danger");
        else bar.classList.add("bg-info");

        // Sector Impact List
        const list = document.getElementById('sector-impact-list');
        list.innerHTML = "";
        const allSec = new Set([...Object.keys(data.oldSectorAllocation), ...Object.keys(data.newSectorAllocation)]);

        allSec.forEach(sec => {
            const oldP = data.oldSectorAllocation[sec] || 0;
            const newP = data.newSectorAllocation[sec] || 0;
            // Only show if there is a noticeable change (> 0.1%)
            if(Math.abs(newP - oldP) > 0.1) {
                 list.innerHTML += `
                    <div class="list-group-item d-flex justify-content-between">
                        <span class="small">${sec}</span>
                        <span class="small fw-bold">
                            ${oldP.toFixed(1)}% <i class="bi bi-arrow-right"></i> ${newP.toFixed(1)}%
                        </span>
                    </div>`;
            }
        });

    } catch (e) {
        console.error("Simulation failed", e);
    }
}

function formatMoney(amount) {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);
}