// API Configuration
const API_BASE_URL = 'http://localhost:8080/api/v1';

// API Client
class InventoryAPI {
    constructor(baseUrl) {
        this.baseUrl = baseUrl;
    }

    async request(endpoint, options = {}) {
        const url = `${this.baseUrl}${endpoint}`;
        const config = {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers
            },
            ...options
        };

        try {
            const response = await fetch(url, config);
            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || 'API request failed');
            }

            return data;
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    }

    // Product endpoints
    async getAllProducts(page = 0, size = 20, sortBy = 'updatedAt', sortDirection = 'DESC') {
        return this.request(`/products?page=${page}&size=${size}&sortBy=${sortBy}&sortDirection=${sortDirection}`);
    }

    async getProductById(id) {
        return this.request(`/products/${id}`);
    }

    async getProductBySku(sku) {
        return this.request(`/products/sku/${sku}`);
    }

    async searchProducts(query, page = 0, size = 20) {
        return this.request(`/products/search?query=${encodeURIComponent(query)}&page=${page}&size=${size}`);
    }

    async getProductsByCategory(category, page = 0, size = 20) {
        return this.request(`/products/category/${category}?page=${page}&size=${size}`);
    }

    async getLowStockProducts(limit = 50) {
        return this.request(`/products/low-stock?limit=${limit}`);
    }

    async getProductStats() {
        return this.request('/products/stats');
    }

    async getCategories() {
        return this.request('/products/categories');
    }

    async createProduct(productData) {
        return this.request('/products', {
            method: 'POST',
            body: JSON.stringify(productData)
        });
    }

    async updateProduct(id, productData) {
        return this.request(`/products/${id}`, {
            method: 'PUT',
            body: JSON.stringify(productData)
        });
    }

    async updateStockQuantity(id, quantity) {
        return this.request(`/products/${id}/stock`, {
            method: 'PATCH',
            body: JSON.stringify({ quantity })
        });
    }

    async deleteProduct(id) {
        return this.request(`/products/${id}`, {
            method: 'DELETE'
        });
    }

    async bulkDeleteProducts(ids) {
        return this.request('/products/bulk', {
            method: 'DELETE',
            body: JSON.stringify(ids)
        });
    }

    async refreshCache() {
        return this.request('/products/cache/refresh', {
            method: 'POST'
        });
    }
}

// Initialize API client
const api = new InventoryAPI(API_BASE_URL);

// UI State Management
const state = {
    currentPage: 'dashboard',
    inventoryPage: 0,
    inventorySize: 20,
    searchTerm: '',
    selectedProducts: new Set()
};

// Toast Notification System
function showToast(message, type = 'success') {
    const container = document.getElementById('toast-container');
    
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    const iconMap = {
        success: 'fa-check-circle',
        error: 'fa-exclamation-circle',
        warning: 'fa-exclamation-triangle'
    };
    
    const colorMap = {
        success: '#10b981',
        error: '#ef4444',
        warning: '#f59e0b'
    };
    
    toast.innerHTML = `
        <i class="fas ${iconMap[type]} toast-icon" style="color: ${colorMap[type]}"></i>
        <div class="toast-content">
            <h4>${type.charAt(0).toUpperCase() + type.slice(1)}</h4>
            <p>${message}</p>
        </div>
    `;
    
    container.appendChild(toast);
    
    // Trigger animation
    setTimeout(() => toast.classList.add('show'), 10);
    
    // Remove after 5 seconds
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => container.removeChild(toast), 300);
    }, 5000);
}

// Initialize Dashboard
async function initializeDashboard() {
    try {
        // Load statistics
        await loadDashboardStats();
        
        // Load recent products
        await loadRecentProducts();
        
    } catch (error) {
        console.error('Dashboard initialization failed:', error);
        showToast('Failed to load dashboard data', 'error');
    }
}

// Load Dashboard Statistics
async function loadDashboardStats() {
    try {
        const response = await api.getProductStats();
        const stats = response.data;
        
        // Update stat cards
        document.getElementById('total-products').textContent = stats.totalProducts.toLocaleString();
        document.getElementById('low-stock-count').textContent = stats.lowStockCount.toLocaleString();
        document.getElementById('categories-count').textContent = stats.categoriesCount.toLocaleString();
        document.getElementById('monthly-revenue').textContent = 
            '$' + (stats.totalInventoryValue || 0).toLocaleString('en-US', { 
                minimumFractionDigits: 2, 
                maximumFractionDigits: 2 
            });
        
        // Update sidebar badge
        document.getElementById('inventory-count').textContent = stats.totalProducts.toLocaleString();
        
    } catch (error) {
        console.error('Failed to load stats:', error);
        throw error;
    }
}

// Load Recent Products for Dashboard
async function loadRecentProducts() {
    const tableBody = document.getElementById('table-body');
    
    try {
        const response = await api.getAllProducts(0, 5);
        const products = response.data.content;
        
        tableBody.innerHTML = '';
        
        if (products.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="7" style="text-align: center; padding: 48px;">
                        <div class="empty-state">
                            <i class="fas fa-box-open"></i>
                            <p>No products found</p>
                        </div>
                    </td>
                </tr>
            `;
            return;
        }
        
        products.forEach(product => {
            const row = createProductRow(product);
            tableBody.appendChild(row);
        });
        
    } catch (error) {
        console.error('Failed to load products:', error);
        tableBody.innerHTML = `
            <tr>
                <td colspan="7" style="text-align: center; padding: 48px;">
                    <div class="error-message">
                        <i class="fas fa-exclamation-circle"></i>
                        <span>Failed to load products. Please try again.</span>
                    </div>
                </td>
            </tr>
        `;
    }
}

// Load Full Inventory
async function loadFullInventory() {
    const tableBody = document.getElementById('full-inventory-table');
    
    try {
        const response = await api.getAllProducts(state.inventoryPage, state.inventorySize);
        const products = response.data.content;
        const totalElements = response.data.totalElements;
        
        tableBody.innerHTML = '';
        
        if (products.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="7" style="text-align: center; padding: 48px;">
                        <div class="empty-state">
                            <i class="fas fa-box-open"></i>
                            <p>No products found</p>
                        </div>
                    </td>
                </tr>
            `;
            return;
        }
        
        products.forEach(product => {
            const row = createProductRow(product);
            tableBody.appendChild(row);
        });
        
        // Update pagination
        updatePaginationInfo(response.data);
        
    } catch (error) {
        console.error('Failed to load inventory:', error);
        showToast('Failed to load inventory', 'error');
    }
}

// Create Product Table Row
function createProductRow(product) {
    const row = document.createElement('tr');
    
    const statusInfo = getStatusInfo(product.status);
    
    row.innerHTML = `
        <td><strong>${product.sku}</strong></td>
        <td>${product.name}</td>
        <td>${product.category}</td>
        <td>${product.stockQuantity}</td>
        <td>$${parseFloat(product.price).toFixed(2)}</td>
        <td><span class="stock-status ${statusInfo.class}">${statusInfo.text}</span></td>
        <td>
            <button class="action-btn edit-btn" data-id="${product.id}" title="Edit">
                <i class="fas fa-edit"></i>
            </button>
            <button class="action-btn delete-btn" data-id="${product.id}" title="Delete">
                <i class="fas fa-trash"></i>
            </button>
            <button class="action-btn view-btn" data-id="${product.id}" title="View Details">
                <i class="fas fa-eye"></i>
            </button>
        </td>
    `;
    
    // Add event listeners
    row.querySelector('.edit-btn').addEventListener('click', () => editProduct(product.id));
    row.querySelector('.delete-btn').addEventListener('click', () => deleteProduct(product.id));
    row.querySelector('.view-btn').addEventListener('click', () => viewProduct(product.id));
    
    return row;
}

// Get Status Information
function getStatusInfo(status) {
    const statusMap = {
        'IN_STOCK': { class: 'in-stock', text: 'In Stock' },
        'LOW_STOCK': { class: 'low-stock', text: 'Low Stock' },
        'OUT_OF_STOCK': { class: 'out-of-stock', text: 'Out of Stock' }
    };
    return statusMap[status] || statusMap['IN_STOCK'];
}

// Update Pagination Info
function updatePaginationInfo(pageData) {
    const start = pageData.number * pageData.size + 1;
    const end = Math.min((pageData.number + 1) * pageData.size, pageData.totalElements);
    const total = pageData.totalElements;
    
    document.getElementById('page-start').textContent = start;
    document.getElementById('page-end').textContent = end;
    document.getElementById('total-items').textContent = total;
}

// Create Product
async function createProduct(productData) {
    try {
        const response = await api.createProduct(productData);
        showToast('Product created successfully!', 'success');
        
        // Refresh data
        await loadDashboardStats();
        if (state.currentPage === 'dashboard') {
            await loadRecentProducts();
        } else if (state.currentPage === 'inventory') {
            await loadFullInventory();
        }
        
        return response.data;
    } catch (error) {
        console.error('Failed to create product:', error);
        showToast(error.message || 'Failed to create product', 'error');
        throw error;
    }
}

// Edit Product
async function editProduct(productId) {
    try {
        const response = await api.getProductById(productId);
        const product = response.data;
        
        // Populate modal with product data
        document.getElementById('product-name').value = product.name;
        document.getElementById('product-sku').value = product.sku;
        document.getElementById('product-category').value = product.category;
        document.getElementById('product-price').value = product.price;
        document.getElementById('product-stock').value = product.stockQuantity;
        document.getElementById('product-min-stock').value = product.minStockLevel;
        document.getElementById('product-description').value = product.description || '';
        
        // Change modal title
        document.querySelector('.modal-header h3').textContent = 'Edit Product';
        
        // Change save button to update
        const saveBtn = document.getElementById('save-product');
        saveBtn.innerHTML = '<i class="fas fa-save"></i> Update Product';
        saveBtn.setAttribute('data-product-id', productId);
        
        // Show modal
        document.getElementById('add-product-modal').classList.add('active');
        
    } catch (error) {
        console.error('Failed to load product:', error);
        showToast('Failed to load product details', 'error');
    }
}

// Delete Product
async function deleteProduct(productId) {
    if (!confirm('Are you sure you want to delete this product?')) {
        return;
    }
    
    try {
        await api.deleteProduct(productId);
        showToast('Product deleted successfully!', 'success');
        
        // Refresh data
        await loadDashboardStats();
        if (state.currentPage === 'dashboard') {
            await loadRecentProducts();
        } else if (state.currentPage === 'inventory') {
            await loadFullInventory();
        }
        
    } catch (error) {
        console.error('Failed to delete product:', error);
        showToast('Failed to delete product', 'error');
    }
}

// View Product Details
async function viewProduct(productId) {
    try {
        const response = await api.getProductById(productId);
        const product = response.data;
        
        // Create a detailed view (you can customize this)
        alert(`Product Details:
        
Name: ${product.name}
SKU: ${product.sku}
Category: ${product.category}
Price: $${product.price}
Stock: ${product.stockQuantity}
Status: ${product.status}
Created: ${new Date(product.createdAt).toLocaleString()}
Updated: ${new Date(product.updatedAt).toLocaleString()}`);
        
    } catch (error) {
        console.error('Failed to load product:', error);
        showToast('Failed to load product details', 'error');
    }
}

// Search Products
let searchTimeout;
async function searchProducts(query) {
    clearTimeout(searchTimeout);
    
    searchTimeout = setTimeout(async () => {
        if (!query.trim()) {
            if (state.currentPage === 'dashboard') {
                await loadRecentProducts();
            } else if (state.currentPage === 'inventory') {
                await loadFullInventory();
            }
            return;
        }
        
        try {
            const response = await api.searchProducts(query);
            const products = response.data.content;
            
            const tableBody = state.currentPage === 'dashboard' ? 
                document.getElementById('table-body') : 
                document.getElementById('full-inventory-table');
            
            tableBody.innerHTML = '';
            
            if (products.length === 0) {
                tableBody.innerHTML = `
                    <tr>
                        <td colspan="7" style="text-align: center; padding: 48px;">
                            <div class="empty-state">
                                <i class="fas fa-search"></i>
                                <p>No products found matching "${query}"</p>
                            </div>
                        </td>
                    </tr>
                `;
                return;
            }
            
            products.forEach(product => {
                const row = createProductRow(product);
                tableBody.appendChild(row);
            });
            
        } catch (error) {
            console.error('Search failed:', error);
            showToast('Search failed', 'error');
        }
    }, 300);
}

// Event Listeners
document.addEventListener('DOMContentLoaded', function() {
    // Sidebar toggle
    const sidebar = document.getElementById('sidebar');
    const toggleBtn = document.getElementById('toggle-sidebar');
    const mobileToggle = document.getElementById('mobile-toggle');
    
    toggleBtn.addEventListener('click', () => sidebar.classList.toggle('collapsed'));
    mobileToggle.addEventListener('click', () => sidebar.classList.toggle('active'));
    
    // Menu navigation
    document.querySelectorAll('.menu-item').forEach(item => {
        item.addEventListener('click', function() {
            const page = this.getAttribute('data-page');
            setActivePage(page);
            
            // Update active menu item
            document.querySelectorAll('.menu-item').forEach(i => i.classList.remove('active'));
            this.classList.add('active');
            
            // Close mobile sidebar
            if (window.innerWidth <= 992) {
                sidebar.classList.remove('active');
            }
        });
    });
    
    // Logo click
    document.getElementById('go-to-dashboard').addEventListener('click', () => {
        setActivePage('dashboard');
        document.querySelectorAll('.menu-item').forEach(i => i.classList.remove('active'));
        document.querySelector('[data-page="dashboard"]').classList.add('active');
    });
    
    // Global search
    const searchInput = document.getElementById('global-search');
    searchInput.addEventListener('input', (e) => {
        state.searchTerm = e.target.value;
        searchProducts(e.target.value);
    });
    
    // Add product buttons
    document.getElementById('add-product-btn').addEventListener('click', openAddProductModal);
    document.getElementById('add-product-inventory').addEventListener('click', openAddProductModal);
    
    // Modal controls
    document.getElementById('close-modal').addEventListener('click', closeModal);
    document.getElementById('cancel-product').addEventListener('click', closeModal);
    document.getElementById('save-product').addEventListener('click', handleSaveProduct);
    
    // Refresh dashboard
    document.getElementById('refresh-dashboard').addEventListener('click', async () => {
        showToast('Refreshing dashboard...', 'success');
        await initializeDashboard();
    });
    
    // Initialize dashboard
    initializeDashboard();
});

// Page Management
function setActivePage(page) {
    state.currentPage = page;
    
    // Hide all pages
    document.querySelectorAll('.page-content').forEach(content => {
        content.classList.add('hidden');
    });
    
    // Show selected page
    const activePage = document.getElementById(`${page}-page`);
    if (activePage) {
        activePage.classList.remove('hidden');
    }
    
    // Update page title
    updatePageTitle(page);
    
    // Load page data
    if (page === 'inventory') {
        loadFullInventory();
    } else if (page === 'dashboard') {
        initializeDashboard();
    }
}

// Update Page Title
function updatePageTitle(page) {
    const titles = {
        'dashboard': { title: 'Dashboard', subtitle: 'Real-time inventory monitoring and analytics' },
        'inventory': { title: 'Inventory Management', subtitle: 'Manage all your inventory items' },
        'analytics': { title: 'Analytics', subtitle: 'Detailed analytics and insights' },
        'suppliers': { title: 'Suppliers', subtitle: 'Manage suppliers and purchase orders' },
        'orders': { title: 'Orders', subtitle: 'View and manage customer orders' },
        'users': { title: 'Users', subtitle: 'Manage system users and permissions' },
        'settings': { title: 'Settings', subtitle: 'Configure system settings' }
    };
    
    if (titles[page]) {
        document.getElementById('page-title').textContent = titles[page].title;
        document.getElementById('page-subtitle').textContent = titles[page].subtitle;
    }
}

// Modal Management
function openAddProductModal() {
    document.getElementById('product-form').reset();
    document.querySelector('.modal-header h3').textContent = 'Add New Product';
    
    const saveBtn = document.getElementById('save-product');
    saveBtn.innerHTML = '<i class="fas fa-save"></i> Save Product';
    saveBtn.removeAttribute('data-product-id');
    
    document.getElementById('add-product-modal').classList.add('active');
}

function closeModal() {
    document.getElementById('add-product-modal').classList.remove('active');
    document.getElementById('product-form').reset();
}

async function handleSaveProduct(e) {
    e.preventDefault();
    
    const productData = {
        name: document.getElementById('product-name').value,
        sku: document.getElementById('product-sku').value,
        category: document.getElementById('product-category').value,
        price: parseFloat(document.getElementById('product-price').value),
        stockQuantity: parseInt(document.getElementById('product-stock').value),
        minStockLevel: parseInt(document.getElementById('product-min-stock').value),
        description: document.getElementById('product-description').value
    };
    
    // Validation
    if (!productData.name || !productData.sku || !productData.category || !productData.price || isNaN(productData.stockQuantity)) {
        showToast('Please fill in all required fields', 'error');
        return;
    }
    
    try {
        const productId = e.target.getAttribute('data-product-id');
        
        if (productId) {
            // Update existing product
            await api.updateProduct(productId, productData);
            showToast('Product updated successfully!', 'success');
        } else {
            // Create new product
            await createProduct(productData);
        }
        
        closeModal();
        
    } catch (error) {
        console.error('Failed to save product:', error);
        // Error already shown by createProduct or will be shown here
        if (!error.message.includes('SKU')) {
            showToast('Failed to save product', 'error');
        }
    }
}

// Close sidebar when clicking outside on mobile
document.addEventListener('click', (event) => {
    const sidebar = document.getElementById('sidebar');
    const mobileToggle = document.getElementById('mobile-toggle');
    const isMobile = window.innerWidth <= 992;
    
    if (isMobile && 
        !sidebar.contains(event.target) && 
        !mobileToggle.contains(event.target) && 
        sidebar.classList.contains('active')) {
        sidebar.classList.remove('active');
    }
});

// ==========================================
// ANALYTICS CHARTS WITH SYNTHETIC DATA
// ==========================================

let analyticsCharts = {};

// Initialize all charts when analytics page loads
function initializeAnalyticsCharts() {
    // Only initialize if charts don't exist
    if (Object.keys(analyticsCharts).length > 0) {
        return;
    }

    // Chart.js default config
    Chart.defaults.font.family = 'Inter, sans-serif';
    Chart.defaults.color = '#64748b';

    // 1. Sales Trend Chart (Line Chart)
    const salesCtx = document.getElementById('salesTrendChart');
    if (salesCtx) {
        analyticsCharts.salesTrend = new Chart(salesCtx, {
            type: 'line',
            data: {
                labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
                datasets: [{
                    label: 'Revenue ($)',
                    data: [12500, 19300, 15200, 21400, 18900, 24500, 22800],
                    borderColor: '#6366f1',
                    backgroundColor: 'rgba(99, 102, 241, 0.1)',
                    tension: 0.4,
                    fill: true,
                    pointRadius: 5,
                    pointHoverRadius: 7
                }, {
                    label: 'Orders',
                    data: [145, 232, 189, 267, 223, 298, 276],
                    borderColor: '#10b981',
                    backgroundColor: 'rgba(16, 185, 129, 0.1)',
                    tension: 0.4,
                    fill: true,
                    pointRadius: 5,
                    pointHoverRadius: 7
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: true,
                        position: 'top'
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: {
                            color: 'rgba(0, 0, 0, 0.05)'
                        }
                    },
                    x: {
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });
    }

    // 2. Category Distribution (Doughnut Chart)
    const categoryCtx = document.getElementById('categoryDistributionChart');
    if (categoryCtx) {
        analyticsCharts.categoryDistribution = new Chart(categoryCtx, {
            type: 'doughnut',
            data: {
                labels: ['Electronics', 'Furniture', 'Accessories', 'Office', 'Clothing'],
                datasets: [{
                    data: [35, 22, 18, 15, 10],
                    backgroundColor: [
                        '#6366f1',
                        '#8b5cf6',
                        '#06b6d4',
                        '#10b981',
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
                                return context.label + ': $' + (context.parsed * 1000).toLocaleString();
                            }
                        }
                    }
                }
            }
        });
    }

    // 3. Stock Level Trends (Area Chart)
    const stockCtx = document.getElementById('stockLevelChart');
    if (stockCtx) {
        analyticsCharts.stockLevel = new Chart(stockCtx, {
            type: 'line',
            data: {
                labels: ['Week 1', 'Week 2', 'Week 3', 'Week 4', 'Week 5', 'Week 6'],
                datasets: [{
                    label: 'In Stock',
                    data: [850, 920, 880, 950, 910, 980],
                    borderColor: '#10b981',
                    backgroundColor: 'rgba(16, 185, 129, 0.2)',
                    fill: true,
                    tension: 0.4
                }, {
                    label: 'Low Stock',
                    data: [42, 38, 45, 35, 40, 32],
                    borderColor: '#f59e0b',
                    backgroundColor: 'rgba(245, 158, 11, 0.2)',
                    fill: true,
                    tension: 0.4
                }, {
                    label: 'Out of Stock',
                    data: [8, 5, 7, 4, 6, 3],
                    borderColor: '#ef4444',
                    backgroundColor: 'rgba(239, 68, 68, 0.2)',
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
                        stacked: false,
                        grid: {
                            color: 'rgba(0, 0, 0, 0.05)'
                        }
                    },
                    x: {
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });
    }

    // 4. Top Products (Horizontal Bar Chart)
    const topProductsCtx = document.getElementById('topProductsChart');
    if (topProductsCtx) {
        analyticsCharts.topProducts = new Chart(topProductsCtx, {
            type: 'bar',
            data: {
                labels: ['Laptop Dell XPS', 'Office Chair Pro', 'Wireless Mouse', 'Monitor 4K', 'Keyboard Mech', 'Desk Lamp LED', 'USB-C Hub', 'Webcam HD', 'Headphones', 'Phone Stand'],
                datasets: [{
                    label: 'Revenue ($K)',
                    data: [74.5, 52.3, 48.9, 45.2, 42.8, 38.5, 35.2, 32.8, 30.5, 28.9],
                    backgroundColor: [
                        '#6366f1',
                        '#8b5cf6',
                        '#06b6d4',
                        '#10b981',
                        '#f59e0b',
                        '#6366f1',
                        '#8b5cf6',
                        '#06b6d4',
                        '#10b981',
                        '#f59e0b'
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

    // 5. Monthly Revenue (Bar + Line Chart)
    const monthlyRevenueCtx = document.getElementById('monthlyRevenueChart');
    if (monthlyRevenueCtx) {
        analyticsCharts.monthlyRevenue = new Chart(monthlyRevenueCtx, {
            type: 'bar',
            data: {
                labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'],
                datasets: [{
                    label: 'Revenue',
                    data: [65000, 72000, 68000, 78000, 82000, 88000, 92000, 85000, 95000, 102000, 98000, 110000],
                    backgroundColor: 'rgba(99, 102, 241, 0.7)',
                    borderRadius: 6,
                    order: 2
                }, {
                    label: 'Target',
                    data: [70000, 70000, 75000, 75000, 80000, 85000, 90000, 90000, 95000, 100000, 105000, 110000],
                    type: 'line',
                    borderColor: '#ef4444',
                    borderWidth: 2,
                    borderDash: [5, 5],
                    fill: false,
                    pointRadius: 0,
                    order: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: true,
                        position: 'top'
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false,
                        callbacks: {
                            label: function(context) {
                                return context.dataset.label + ': $' + context.parsed.y.toLocaleString();
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: {
                            color: 'rgba(0, 0, 0, 0.05)'
                        },
                        ticks: {
                            callback: function(value) {
                                return '$' + (value / 1000) + 'K';
                            }
                        }
                    },
                    x: {
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });
    }

    // 6. Stock Status (Pie Chart)
    const stockStatusCtx = document.getElementById('stockStatusChart');
    if (stockStatusCtx) {
        analyticsCharts.stockStatus = new Chart(stockStatusCtx, {
            type: 'pie',
            data: {
                labels: ['In Stock', 'Low Stock', 'Out of Stock'],
                datasets: [{
                    data: [980, 32, 3],
                    backgroundColor: [
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
                        position: 'bottom'
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

    // 7. Turnover Rate (Grouped Bar Chart)
    const turnoverCtx = document.getElementById('turnoverRateChart');
    if (turnoverCtx) {
        analyticsCharts.turnoverRate = new Chart(turnoverCtx, {
            type: 'bar',
            data: {
                labels: ['Electronics', 'Furniture', 'Accessories', 'Office', 'Clothing'],
                datasets: [{
                    label: 'Q1',
                    data: [4.2, 2.8, 5.1, 3.5, 6.2],
                    backgroundColor: 'rgba(99, 102, 241, 0.7)',
                    borderRadius: 6
                }, {
                    label: 'Q2',
                    data: [4.5, 3.1, 5.3, 3.8, 6.5],
                    backgroundColor: 'rgba(139, 92, 246, 0.7)',
                    borderRadius: 6
                }, {
                    label: 'Q3',
                    data: [4.8, 3.3, 5.6, 4.0, 6.8],
                    backgroundColor: 'rgba(6, 182, 212, 0.7)',
                    borderRadius: 6
                }, {
                    label: 'Q4',
                    data: [5.1, 3.5, 5.9, 4.2, 7.1],
                    backgroundColor: 'rgba(16, 185, 129, 0.7)',
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
                    },
                    tooltip: {
                        mode: 'index',
                        intersect: false,
                        callbacks: {
                            label: function(context) {
                                return context.dataset.label + ': ' + context.parsed.y.toFixed(1) + 'x';
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: {
                            color: 'rgba(0, 0, 0, 0.05)'
                        },
                        ticks: {
                            callback: function(value) {
                                return value.toFixed(1) + 'x';
                            }
                        }
                    },
                    x: {
                        grid: {
                            display: false
                        }
                    }
                }
            }
        });
    }

    console.log('Analytics charts initialized successfully');
}

// Destroy charts when leaving analytics page
function destroyAnalyticsCharts() {
    Object.values(analyticsCharts).forEach(chart => {
        if (chart) {
            chart.destroy();
        }
    });
    analyticsCharts = {};
}

// Update the setActivePage function to handle analytics charts
const originalSetActivePage = setActivePage;
setActivePage = function(page) {
    // Call original function
    originalSetActivePage(page);
    
    // Handle analytics page
    if (page === 'analytics') {
        // Small delay to ensure DOM is ready
        setTimeout(() => {
            initializeAnalyticsCharts();
        }, 100);
    } else {
        // Destroy charts when leaving analytics page to free memory
        destroyAnalyticsCharts();
    }
};