// ==========================================
// SUPPLY CHAIN & FINANCIAL ANALYTICS CHARTS
// ==========================================

// Analytics Tab Switching
document.addEventListener('DOMContentLoaded', function() {
    const analyticsTabs = document.querySelectorAll('.analytics-tab');
    
    analyticsTabs.forEach(tab => {
        tab.addEventListener('click', function() {
            const tabName = this.getAttribute('data-tab');
            
            // Remove active class from all tabs
            analyticsTabs.forEach(t => t.classList.remove('active'));
            this.classList.add('active');
            
            // Hide all tab contents
            document.querySelectorAll('.analytics-tab-content').forEach(content => {
                content.classList.add('hidden');
            });
            
            // Show selected tab content
            const selectedTab = document.getElementById(`${tabName}-analytics-content`);
            if (selectedTab) {
                selectedTab.classList.remove('hidden');
            }
            
            // Initialize charts for the selected tab
            if (tabName === 'supply-chain' && Object.keys(supplyChainCharts).length === 0) {
                initializeSupplyChainCharts();
            } else if (tabName === 'financial' && Object.keys(financialCharts).length === 0) {
                initializeFinancialCharts();
            }
        });
    });
});

let supplyChainCharts = {};
let financialCharts = {};

// Initialize Supply Chain Charts
function initializeSupplyChainCharts() {
    // 1. Transaction Flow Chart (Area Chart)
    const txnFlowCtx = document.getElementById('transactionFlowChart');
    if (txnFlowCtx) {
        supplyChainCharts.transactionFlow = new Chart(txnFlowCtx, {
            type: 'line',
            data: {
                labels: ['Week 1', 'Week 2', 'Week 3', 'Week 4', 'Week 5', 'Week 6'],
                datasets: [{
                    label: 'Inbound Transactions',
                    data: [42, 58, 51, 67, 63, 75],
                    borderColor: '#6366f1',
                    backgroundColor: 'rgba(99, 102, 241, 0.2)',
                    fill: true,
                    tension: 0.4
                }, {
                    label: 'Outbound Transactions',
                    data: [38, 54, 48, 62, 59, 71],
                    borderColor: '#10b981',
                    backgroundColor: 'rgba(16, 185, 129, 0.2)',
                    fill: true,
                    tension: 0.4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: true,
                        position: 'top'
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: {
                            color: 'rgba(0, 0, 0, 0.05)'
                        }
                    }
                }
            }
        });
    }

    // 2. Supplier Performance Chart (Horizontal Bar)
    const supplierPerfCtx = document.getElementById('supplierPerformanceChart');
    if (supplierPerfCtx) {
        supplyChainCharts.supplierPerformance = new Chart(supplierPerfCtx, {
            type: 'bar',
            data: {
                labels: ['TechCorp Mfg', 'Global Dist', 'AsiaConnect', 'Euro Network', 'Prime Retail', 'Pacific Trading'],
                datasets: [{
                    label: 'Transaction Value ($K)',
                    data: [342, 289, 256, 223, 198, 175],
                    backgroundColor: [
                        '#6366f1',
                        '#8b5cf6',
                        '#06b6d4',
                        '#10b981',
                        '#f59e0b',
                        '#ef4444'
                    ],
                    borderRadius: 6
                }]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    x: {
                        beginAtZero: true,
                        grid: {
                            color: 'rgba(0, 0, 0, 0.05)'
                        }
                    },
                    y: {
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });
    }

    // 3. Lead Time Analysis (Grouped Bar)
    const leadTimeCtx = document.getElementById('leadTimeChart');
    if (leadTimeCtx) {
        supplyChainCharts.leadTime = new Chart(leadTimeCtx, {
            type: 'bar',
            data: {
                labels: ['Manufacturer', 'Distributor', 'Wholesaler', 'Retailer', 'Agent'],
                datasets: [{
                    label: 'Average Lead Time (days)',
                    data: [3.2, 4.8, 5.5, 6.2, 7.1],
                    backgroundColor: 'rgba(99, 102, 241, 0.7)',
                    borderRadius: 6
                }, {
                    label: 'Target Lead Time (days)',
                    data: [4.0, 5.0, 6.0, 7.0, 8.0],
                    backgroundColor: 'rgba(16, 185, 129, 0.3)',
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: true,
                        position: 'top'
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: {
                            color: 'rgba(0, 0, 0, 0.05)'
                        }
                    }
                }
            }
        });
    }

    // 4. Transaction Status Distribution (Doughnut)
    const txnStatusCtx = document.getElementById('transactionStatusChart');
    if (txnStatusCtx) {
        supplyChainCharts.transactionStatus = new Chart(txnStatusCtx, {
            type: 'doughnut',
            data: {
                labels: ['Delivered', 'In Transit', 'Confirmed', 'Pending'],
                datasets: [{
                    data: [687, 89, 52, 19],
                    backgroundColor: [
                        '#10b981',
                        '#06b6d4',
                        '#6366f1',
                        '#f59e0b'
                    ],
                    borderWidth: 2,
                    borderColor: '#ffffff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'right'
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                const percentage = ((context.parsed / total) * 100).toFixed(1);
                                return context.label + ': ' + context.parsed + ' (' + percentage + '%)';
                            }
                        }
                    }
                }
            }
        });
    }

    // 5. Network Heatmap (Matrix Chart using Bar)
    const networkHeatmapCtx = document.getElementById('networkHeatmapChart');
    if (networkHeatmapCtx) {
        const suppliers = ['TechCorp', 'Global Dist', 'Elec Wholesale', 'Prime Retail', 'AsiaConnect', 'Euro Network', 'Pacific Trade', 'FastShip'];
        const connections = [
            [0, 85, 45, 32, 0, 12, 0, 0],
            [0, 0, 92, 76, 48, 0, 23, 0],
            [0, 0, 0, 68, 34, 41, 0, 15],
            [0, 0, 0, 0, 0, 0, 0, 0],
            [125, 98, 0, 0, 0, 53, 62, 0],
            [0, 87, 45, 72, 0, 0, 38, 0],
            [0, 65, 0, 0, 108, 0, 0, 42],
            [0, 73, 48, 0, 0, 51, 0, 0]
        ];
        
        // Convert to stacked bar chart format
        const datasets = [];
        for (let i = 0; i < suppliers.length; i++) {
            datasets.push({
                label: suppliers[i],
                data: connections.map(row => row[i]),
                backgroundColor: `hsla(${(i * 360) / suppliers.length}, 70%, 60%, 0.8)`,
                borderRadius: 4
            });
        }
        
        supplyChainCharts.networkHeatmap = new Chart(networkHeatmapCtx, {
            type: 'bar',
            data: {
                labels: suppliers,
                datasets: datasets
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: true,
                        position: 'bottom'
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return context.dataset.label + ' → ' + context.label + ': $' + context.parsed.y.toLocaleString() + 'K';
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        stacked: true,
                        grid: {
                            display: false
                        }
                    },
                    y: {
                        stacked: true,
                        grid: {
                            color: 'rgba(0, 0, 0, 0.05)'
                        },
                        ticks: {
                            callback: function(value) {
                                return '$' + value + 'K';
                            }
                        }
                    }
                }
            }
        });
    }

    console.log('Supply chain charts initialized successfully');
}

// Initialize Financial Charts
function initializeFinancialCharts() {
    // 1. Cost Breakdown Chart (Pie)
    const costBreakdownCtx = document.getElementById('costBreakdownChart');
    if (costBreakdownCtx) {
        financialCharts.costBreakdown = new Chart(costBreakdownCtx, {
            type: 'pie',
            data: {
                labels: ['COGS', 'Labor', 'Overhead', 'Shipping', 'Storage', 'Other'],
                datasets: [{
                    data: [65, 15, 8, 6, 4, 2],
                    backgroundColor: [
                        '#6366f1',
                        '#8b5cf6',
                        '#06b6d4',
                        '#10b981',
                        '#f59e0b',
                        '#ef4444'
                    ],
                    borderWidth: 3,
                    borderColor: '#ffffff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'right'
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return context.label + ': ' + context.parsed + '%';
                            }
                        }
                    }
                }
            }
        });
    }

    // 2. Profit Margin Trend (Line)
    const profitMarginCtx = document.getElementById('profitMarginChart');
    if (profitMarginCtx) {
        financialCharts.profitMargin = new Chart(profitMarginCtx, {
            type: 'line',
            data: {
                labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
                datasets: [{
                    label: 'Gross Margin %',
                    data: [28.5, 29.2, 30.1, 30.8, 31.2, 31.8, 32.1, 32.5, 32.8, 33.2, 32.9, 32.5],
                    borderColor: '#6366f1',
                    backgroundColor: 'rgba(99, 102, 241, 0.1)',
                    fill: true,
                    tension: 0.4,
                    pointRadius: 5
                }, {
                    label: 'Net Margin %',
                    data: [12.3, 13.1, 14.2, 14.8, 15.2, 15.8, 16.1, 16.5, 16.8, 17.2, 16.9, 16.5],
                    borderColor: '#10b981',
                    backgroundColor: 'rgba(16, 185, 129, 0.1)',
                    fill: true,
                    tension: 0.4,
                    pointRadius: 5
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: true,
                        position: 'top'
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        max: 40,
                        grid: {
                            color: 'rgba(0, 0, 0, 0.05)'
                        },
                        ticks: {
                            callback: function(value) {
                                return value + '%';
                            }
                        }
                    }
                }
            }
        });
    }

    // 3. Revenue vs Cost (Multi-axis Bar+Line)
    const revenueCostCtx = document.getElementById('revenueCostChart');
    if (revenueCostCtx) {
        financialCharts.revenueCost = new Chart(revenueCostCtx, {
            type: 'bar',
            data: {
                labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
                datasets: [{
                    label: 'Revenue',
                    data: [285, 312, 298, 345, 368, 392, 415, 388, 428, 456, 442, 485],
                    backgroundColor: 'rgba(99, 102, 241, 0.7)',
                    borderRadius: 6,
                    yAxisID: 'y'
                }, {
                    label: 'Cost',
                    data: [192, 208, 198, 228, 242, 258, 272, 254, 280, 298, 288, 316],
                    backgroundColor: 'rgba(239, 68, 68, 0.5)',
                    borderRadius: 6,
                    yAxisID: 'y'
                }, {
                    label: 'Profit',
                    type: 'line',
                    data: [93, 104, 100, 117, 126, 134, 143, 134, 148, 158, 154, 169],
                    borderColor: '#10b981',
                    borderWidth: 3,
                    fill: false,
                    yAxisID: 'y1'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: true,
                        position: 'top'
                    }
                },
                scales: {
                    y: {
                        type: 'linear',
                        display: true,
                        position: 'left',
                        grid: {
                            color: 'rgba(0, 0, 0, 0.05)'
                        },
                        ticks: {
                            callback: function(value) {
                                return '$' + value + 'K';
                            }
                        }
                    },
                    y1: {
                        type: 'linear',
                        display: true,
                        position: 'right',
                        grid: {
                            drawOnChartArea: false
                        },
                        ticks: {
                            callback: function(value) {
                                return '$' + value + 'K';
                            }
                        }
                    }
                }
            }
        });
    }

    // 4. Payment Terms Distribution (Doughnut)
    const paymentTermsCtx = document.getElementById('paymentTermsChart');
    if (paymentTermsCtx) {
        financialCharts.paymentTerms = new Chart(paymentTermsCtx, {
            type: 'doughnut',
            data: {
                labels: ['Net 30', 'Net 60', 'Net 90', 'COD', 'Advance'],
                datasets: [{
                    data: [45, 28, 15, 8, 4],
                    backgroundColor: [
                        '#6366f1',
                        '#8b5cf6',
                        '#06b6d4',
                        '#f59e0b',
                        '#10b981'
                    ],
                    borderWidth: 2,
                    borderColor: '#ffffff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'bottom'
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return context.label + ': ' + context.parsed + '%';
                            }
                        }
                    }
                }
            }
        });
    }

    console.log('Financial charts initialized successfully');
}

// Destroy charts when switching tabs to free memory
function destroySupplyChainCharts() {
    Object.values(supplyChainCharts).forEach(chart => {
        if (chart) chart.destroy();
    });
    supplyChainCharts = {};
}

function destroyFinancialCharts() {
    Object.values(financialCharts).forEach(chart => {
        if (chart) chart.destroy();
    });
    financialCharts = {};
}