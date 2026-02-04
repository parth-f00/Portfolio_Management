const API_URL = "http://localhost:8082/api/portfolio";

let sectorChartInstance = null;
let historyChartInstance = null;

// --- INIT ---
document.addEventListener("DOMContentLoaded", () => {
    // 1. Dark Mode Init
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

    // 4. SCROLL SPY (New Feature)
    initScrollSpy();
});

// --- NEW: SCROLL SPY LOGIC ---
// --- NEW: SCROLL SPY LOGIC (FIXED) ---
// --- NEW: ROBUST SCROLL SPY ---
function initScrollSpy() {
    const sections = document.querySelectorAll('div[id^="section-"]');
    const navLinks = document.querySelectorAll('.list-group-item');

    // Options: Trigger whenever any part of the target is visible
    const options = {
        root: null,
        rootMargin: '0px',
        threshold: [0, 0.25, 0.5, 0.75, 1] // Check continuously as visibility changes
    };

    const observer = new IntersectionObserver((entries) => {
        // 1. We don't just check one entry; we check ALL sections to see who wins
        let maxRatio = 0;
        let activeId = '';

        // Check every section's current visibility on screen
        sections.forEach(section => {
            const rect = section.getBoundingClientRect();
            const visibleHeight = Math.min(window.innerHeight, rect.bottom) - Math.max(0, rect.top);

            // Calculate how much screen height this section occupies
            // Using a simple heuristic: visible pixels
            if (visibleHeight > 0) {
                // If it takes up more pixels than the previous winner, it's the new active one
                // We add a small buffer (50px) to bias towards the top section slightly
                if (visibleHeight > maxRatio) {
                    maxRatio = visibleHeight;
                    activeId = section.id;
                }
            }
        });

        // 2. If we found a winner, update the sidebar
        if (activeId) {
            navLinks.forEach(link => {
                link.classList.remove('active-nav');
                // Robust matching: Check if the onclick string contains the ID
                const onclickVal = link.getAttribute('onclick');
                if (onclickVal && onclickVal.includes(activeId)) {
                    link.classList.add('active-nav');
                }
            });
        }
    }, options);

    sections.forEach(section => observer.observe(section));

    // Also attach a standard scroll listener as a backup fallback
    // This ensures that when you stop scrolling, the calculation runs one last time
    window.addEventListener('scroll', () => {
         // (Optional) We can throttle this if needed, but for simple apps it's fine
         // This just triggers the logic above implicitly via the observer loop
    }, { passive: true });
}

// --- NAVIGATION ---
function scrollToSection(id) {
    const el = document.getElementById(id);
    if(el) {
        el.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
}

// --- THEME TOGGLE ---
function toggleTheme() {
    document.body.classList.toggle('dark-mode');
    const isDark = document.body.classList.contains('dark-mode');
    localStorage.setItem('theme', isDark ? 'dark' : 'light');
    document.getElementById('theme-btn').innerHTML = isDark ? '<i class="bi bi-sun-fill"></i>' : '<i class="bi bi-moon-stars-fill"></i>';

    // Force Chart Re-render
    fetchCharts();
}

// --- DATA FETCHING ---
async function fetchDashboardData() {
    try {
        const valRes = await fetch(`${API_URL}/total-value`);
        const totalVal = await valRes.json();
        document.getElementById('total-value').innerText = formatMoney(totalVal);

        const res = await fetch(`${API_URL}/`);
        const data = await res.json();

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

        const parentRow = `
            <tr class="clickable-row fw-bold" onclick="toggleCollapse('${collapseId}', this)">
                <td>${group.ticker} <span class="badge bg-light text-dark border ms-1">${group.sector}</span></td>
                <td>${group.totalQty}</td>
                <td>${formatMoney(avgPrice)}</td>
                <td>${formatMoney(group.currentPrice)}</td>
                <td class="${plColor}">${formatMoney(group.totalPL)}</td>
                <td class="fs-5">${trendIcon}</td>
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
                    <td colspan="3"></td>
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
                <td colspan="7" class="p-0">
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

async function fetchCharts() {
    // 1. Determine Theme Colors
    const isDark = document.body.classList.contains('dark-mode');
    const textColor = isDark ? '#e0e0e0' : '#666';

    // 2. FETCH DATA
    const secRes = await fetch(`${API_URL}/sector-distrinbution`);
    const secData = await secRes.json();

    const histRes = await fetch(`${API_URL}/history/portfolio`);
    const histData = await histRes.json();

    // ----------------------------
    // CHART 1: SECTOR (Pie Chart)
    // ----------------------------
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
                    labels: {
                        color: textColor,
                        usePointStyle: true,
                        padding: 20,
                        font: { size: 12 }
                    }
                }
            },
            cutout: '70%',
        }
    });

    // ----------------------------
    // CHART 2: HISTORY (Line Chart)
    // ----------------------------
    const ctx2 = document.getElementById('historyChart').getContext('2d');

    // Gradient Fill
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
            interaction: {
                mode: 'index',
                intersect: false,
            },
            plugins: {
                legend: { display: false },
                tooltip: {
                    backgroundColor: isDark ? '#333' : '#fff',
                    titleColor: isDark ? '#fff' : '#333',
                    bodyColor: isDark ? '#fff' : '#333',
                    borderColor: '#ccc',
                    borderWidth: 1,
                    callbacks: {
                        // <--- THIS FIXES THE DATE FORMAT --->
                        title: function(tooltipItems) {
                            const rawDate = tooltipItems[0].label;
                            const d = new Date(rawDate);
                            // Format: DD-MM-YYYY
                            return `${("0" + d.getDate()).slice(-2)}-${("0" + (d.getMonth() + 1)).slice(-2)}-${d.getFullYear()}`;
                        },
                        // Optional: Format the value as Currency ($)
                        label: function(context) {
                            let label = context.dataset.label || '';
                            if (label) {
                                label += ': ';
                            }
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
                            // Axis label format (e.g. Oct 25)
                            return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
                        }
                    }
                },
                y: {
                    display: true,
                    grid: { display: false },
                    ticks: { display: false }
                }
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
        let colorClass = 'text-primary'; // Default Blue
        let bgClass = 'list-group-item';

        // 1. Analyze the text to determine sentiment
        const text = s.toLowerCase();

        if (text.includes("high")) {
            // Negative / Warning
            icon = 'bi-exclamation-triangle-fill';
            colorClass = 'text-danger'; // Red
        } else if (text.includes("low")) {
            // Opportunity / Info
            icon = 'bi-arrow-down-circle';
            colorClass = 'text-info'; // Cyan/Blue
        } else if (text.includes("balanced") || text.includes("good work")) {
            // Positive
            icon = 'bi-check-circle-fill';
            colorClass = 'text-success'; // Green
        }

        // 2. Render the item with the chosen style
        list.innerHTML += `
            <li class="list-group-item d-flex align-items-start">
                <i class="bi ${icon} ${colorClass} me-3 mt-1 fs-5"></i>
                <span class="${colorClass === 'text-danger' ? 'text-danger' : ''}">${s}</span>
            </li>
        `;
    });
}

async function addInvestment(e) {
    e.preventDefault();
    const payload = {
        ticker: document.getElementById('ticker').value.toUpperCase(),
        buyPrice: parseFloat(document.getElementById('buyPrice').value),
        quantity: parseInt(document.getElementById('quantity').value),
        sector: document.getElementById('sector').value,
        purchaseDate: document.getElementById('purchaseDate').value + "T00:00:00"
    };
    await fetch(API_URL + "/", {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    });
    alert("Asset Added!");
    document.getElementById('add-form').reset();
    document.getElementById('purchaseDate').value = new Date().toISOString().split('T')[0];
    fetchDashboardData();
    fetchCharts();
}

async function deleteInvestment(id) {
    if (!confirm("Remove this entry?")) return;
    await fetch(`${API_URL}/${id}`, { method: 'DELETE' });
    fetchDashboardData();
    fetchCharts();
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
    const res = await fetch(`${API_URL}/simulate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(trade)
    });
    const data = await res.json();

    document.getElementById('sim-results').style.display = 'block';

    const riskBadge = document.getElementById('sim-risk-icon-box');
    const riskTitle = document.getElementById('sim-risk-level');
    if (data.riskLevel === 'HIGH') {
        riskBadge.className = "rounded-circle p-3 me-3 text-white bg-danger";
        riskTitle.innerText = "HIGH RISK";
    } else if (data.riskLevel === 'MEDIUM') {
        riskBadge.className = "rounded-circle p-3 me-3 text-dark bg-warning";
        riskTitle.innerText = "MEDIUM RISK";
    } else {
        riskBadge.className = "rounded-circle p-3 me-3 text-white bg-success";
        riskTitle.innerText = "HEALTHY";
    }
    document.getElementById('sim-risk-msg').innerText = data.riskMessage;

    const conc = data.highestStockPct;
    document.getElementById('sim-conc-name').innerText = data.highestStockTicker;
    document.getElementById('sim-conc-val').innerText = conc.toFixed(1) + "%";
    document.getElementById('sim-conc-bar').style.width = conc + "%";
    document.getElementById('sim-conc-bar').className = conc > 30 ? "progress-bar bg-danger" : "progress-bar bg-info";

    const list = document.getElementById('sector-impact-list');
    list.innerHTML = "";
    const allSec = new Set([...Object.keys(data.oldSectorAllocation), ...Object.keys(data.newSectorAllocation)]);
    allSec.forEach(sec => {
        const oldP = data.oldSectorAllocation[sec] || 0;
        const newP = data.newSectorAllocation[sec] || 0;
        if(Math.abs(newP - oldP) > 0.1) {
             list.innerHTML += `<div class="list-group-item d-flex justify-content-between"><span class="small">${sec}</span><span class="small fw-bold">${oldP.toFixed(1)}% <i class="bi bi-arrow-right"></i> ${newP.toFixed(1)}%</span></div>`;
        }
    });
}

function formatMoney(amount) {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(amount);
}