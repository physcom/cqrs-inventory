// ==========================================
// SUPPLY CHAIN MANAGEMENT - SYNTHETIC DATA & FUNCTIONS
// ==========================================

// Synthetic Suppliers Data
const syntheticSuppliers = [
    {id: 1, supplierCode: 'SUP-001', name: 'TechCorp Manufacturing', type: 'MANUFACTURER', email: 'contact@techcorp.com', phone: '+1-555-0101', rating: 4.8, status: 'ACTIVE', city: 'San Francisco', country: 'USA'},
    {id: 2, supplierCode: 'SUP-002', name: 'Global Distributors Inc', type: 'DISTRIBUTOR', email: 'sales@globaldist.com', phone: '+1-555-0102', rating: 4.5, status: 'ACTIVE', city: 'New York', country: 'USA'},
    {id: 3, supplierCode: 'SUP-003', name: 'Electronics Wholesale Ltd', type: 'WHOLESALER', email: 'info@elecwholesale.com', phone: '+1-555-0103', rating: 4.3, status: 'ACTIVE', city: 'Chicago', country: 'USA'},
    {id: 4, supplierCode: 'SUP-004', name: 'Prime Retail Chain', type: 'RETAILER', email: 'orders@primeretail.com', phone: '+1-555-0104', rating: 4.6, status: 'ACTIVE', city: 'Los Angeles', country: 'USA'},
    {id: 5, supplierCode: 'SUP-005', name: 'FastShip Dropshippers', type: 'DROPSHIPPER', email: 'support@fastship.com', phone: '+1-555-0105', rating: 4.2, status: 'ACTIVE', city: 'Seattle', country: 'USA'},
    {id: 6, supplierCode: 'SUP-006', name: 'AsiaConnect Manufacturers', type: 'MANUFACTURER', email: 'export@asiaconnect.com', phone: '+86-555-0106', rating: 4.7, status: 'ACTIVE', city: 'Shanghai', country: 'China'},
    {id: 7, supplierCode: 'SUP-007', name: 'Euro Distribution Network', type: 'DISTRIBUTOR', email: 'contact@eurodist.com', phone: '+49-555-0107', rating: 4.4, status: 'ACTIVE', city: 'Berlin', country: 'Germany'},
    {id: 8, supplierCode: 'SUP-008', name: 'Pacific Trading Co', type: 'AGENT', email: 'agent@pacifictrade.com', phone: '+81-555-0108', rating: 4.1, status: 'ACTIVE', city: 'Tokyo', country: 'Japan'}
];

// Synthetic Supply Chain Transactions
const syntheticTransactions = [
    {id: 1, transactionNumber: 'TXN-2024-001', fromSupplierId: 1, toSupplierId: 2, productId: 1, quantity: 500, unitPrice: 85.00, totalAmount: 42500.00, type: 'SALE', status: 'DELIVERED', date: '2024-01-15'},
    {id: 2, transactionNumber: 'TXN-2024-002', fromSupplierId: 2, toSupplierId: 3, productId: 1, quantity: 300, unitPrice: 89.00, totalAmount: 26700.00, type: 'SALE', status: 'DELIVERED', date: '2024-01-18'},
    {id: 3, transactionNumber: 'TXN-2024-003', fromSupplierId: 3, toSupplierId: 4, productId: 1, quantity: 200, unitPrice: 92.00, totalAmount: 18400.00, type: 'SALE', status: 'IN_TRANSIT', date: '2024-01-20'},
    {id: 4, transactionNumber: 'TXN-2024-004', fromSupplierId: 6, toSupplierId: 2, productId: 2, quantity: 1000, unitPrice: 230.00, totalAmount: 230000.00, type: 'SALE', status: 'DELIVERED', date: '2024-01-10'},
    {id: 5, transactionNumber: 'TXN-2024-005', fromSupplierId: 2, toSupplierId: 7, productId: 2, quantity: 400, unitPrice: 245.00, totalAmount: 98000.00, type: 'SALE', status: 'CONFIRMED', date: '2024-01-22'},
    {id: 6, transactionNumber: 'TXN-2024-006', fromSupplierId: 1, toSupplierId: 5, productId: 3, quantity: 800, unitPrice: 95.00, totalAmount: 76000.00, type: 'SALE', status: 'DELIVERED', date: '2024-01-12'},
    {id: 7, transactionNumber: 'TXN-2024-007', fromSupplierId: 6, toSupplierId: 1, productId: 4, quantity: 2000, unitPrice: 40.00, totalAmount: 80000.00, type: 'SALE', status: 'DELIVERED', date: '2024-01-08'},
    {id: 8, transactionNumber: 'TXN-2024-008', fromSupplierId: 7, toSupplierId: 4, productId: 5, quantity: 600, unitPrice: 48.00, totalAmount: 28800.00, type: 'SALE', status: 'PENDING', date: '2024-01-25'},
    {id: 9, transactionNumber: 'TXN-2024-009', fromSupplierId: 1, toSupplierId: 3, productId: 6, quantity: 350, unitPrice: 58.00, totalAmount: 20300.00, type: 'SALE', status: 'DELIVERED', date: '2024-01-14'},
    {id: 10, transactionNumber: 'TXN-2024-010', fromSupplierId: 8, toSupplierId: 2, productId: 7, quantity: 450, unitPrice: 380.00, totalAmount: 171000.00, type: 'SALE', status: 'IN_TRANSIT', date: '2024-01-23'}
];

// Load Suppliers Page
function loadSuppliersPage() {
    // Update stats
    document.getElementById('total-suppliers').textContent = syntheticSuppliers.length;
    document.getElementById('active-suppliers').textContent = syntheticSuppliers.filter(s => s.status === 'ACTIVE').length;
    
    const avgRating = (syntheticSuppliers.reduce((sum, s) => sum + s.rating, 0) / syntheticSuppliers.length).toFixed(1);
    document.getElementById('avg-supplier-rating').textContent = avgRating;
    
    // Count unique partnerships (connections)
    const partnerships = new Set();
    syntheticTransactions.forEach(t => {
        partnerships.add(`${t.fromSupplierId}-${t.toSupplierId}`);
    });
    document.getElementById('supplier-partnerships').textContent = partnerships.size;
    
    // Populate suppliers table
    const tableBody = document.getElementById('suppliers-table-body');
    tableBody.innerHTML = '';
    
    syntheticSuppliers.forEach(supplier => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td><strong>${supplier.supplierCode}</strong></td>
            <td>${supplier.name}</td>
            <td><span style="padding: 4px 8px; background: var(--gray-100); border-radius: 4px; font-size: 0.85rem;">${supplier.type}</span></td>
            <td>
                <div style="font-size: 0.85rem;">
                    <div>${supplier.email}</div>
                    <div style="color: var(--gray-500);">${supplier.phone}</div>
                </div>
            </td>
            <td>
                <div style="display: flex; align-items: center; gap: 4px;">
                    <span style="color: var(--warning-color);"><i class="fas fa-star"></i></span>
                    <span style="font-weight: 600;">${supplier.rating}</span>
                </div>
            </td>
            <td><span class="stock-status in-stock">${supplier.status}</span></td>
            <td>
                <button class="action-btn" title="View"><i class="fas fa-eye"></i></button>
                <button class="action-btn" title="Edit"><i class="fas fa-edit"></i></button>
                <button class="action-btn" title="Transactions"><i class="fas fa-exchange-alt"></i></button>
            </td>
        `;
        tableBody.appendChild(row);
    });
}

// Load Transactions Page
function loadTransactionsPage() {
    // Update stats
    document.getElementById('total-transactions').textContent = syntheticTransactions.length;
    
    const totalValue = syntheticTransactions.reduce((sum, t) => sum + t.totalAmount, 0);
    document.getElementById('total-transaction-value').textContent = '$' + totalValue.toLocaleString();
    
    document.getElementById('pending-transactions').textContent = syntheticTransactions.filter(t => t.status === 'PENDING').length;
    document.getElementById('in-transit-transactions').textContent = syntheticTransactions.filter(t => t.status === 'IN_TRANSIT').length;
    
    // Populate transactions table
    const tableBody = document.getElementById('transactions-table-body');
    tableBody.innerHTML = '';
    
    syntheticTransactions.forEach(txn => {
        const fromSupplier = syntheticSuppliers.find(s => s.id === txn.fromSupplierId);
        const toSupplier = syntheticSuppliers.find(s => s.id === txn.toSupplierId);
        
        const statusClass = {
            'DELIVERED': 'in-stock',
            'IN_TRANSIT': 'low-stock',
            'PENDING': 'out-of-stock',
            'CONFIRMED': 'in-stock'
        }[txn.status] || 'out-of-stock';
        
        const row = document.createElement('tr');
        row.innerHTML = `
            <td><strong>${txn.transactionNumber}</strong></td>
            <td style="font-size: 0.85rem;">${fromSupplier?.name || 'Unknown'}</td>
            <td style="font-size: 0.85rem;">${toSupplier?.name || 'Unknown'}</td>
            <td style="font-size: 0.85rem;">Product #${txn.productId}</td>
            <td>${txn.quantity}</td>
            <td><strong>$${txn.totalAmount.toLocaleString()}</strong></td>
            <td><span class="stock-status ${statusClass}">${txn.status.replace('_', ' ')}</span></td>
            <td style="font-size: 0.85rem;">${txn.date}</td>
            <td>
                <button class="action-btn" title="View"><i class="fas fa-eye"></i></button>
                <button class="action-btn" title="Track"><i class="fas fa-map-marker-alt"></i></button>
            </td>
        `;
        tableBody.appendChild(row);
    });
}

// Supply Chain Network Visualization
function initializeSupplyChainNetwork() {
    const canvas = document.getElementById('supplyChainNetworkCanvas');
    if (!canvas) return;
    
    const ctx = canvas.getContext('2d');
    canvas.width = canvas.parentElement.clientWidth;
    canvas.height = 600;
    
    // Prepare nodes and edges
    const nodes = syntheticSuppliers.map((supplier, index) => ({
        id: supplier.id,
        name: supplier.name,
        type: supplier.type,
        x: Math.cos((index / syntheticSuppliers.length) * 2 * Math.PI) * 200 + canvas.width / 2,
        y: Math.sin((index / syntheticSuppliers.length) * 2 * Math.PI) * 200 + 300,
        radius: 20,
        color: getSupplierColor(supplier.type)
    }));
    
    // Calculate edges from transactions
    const edgeMap = new Map();
    syntheticTransactions.forEach(txn => {
        const key = `${txn.fromSupplierId}-${txn.toSupplierId}`;
        if (!edgeMap.has(key)) {
            edgeMap.set(key, { from: txn.fromSupplierId, to: txn.toSupplierId, count: 0, value: 0 });
        }
        const edge = edgeMap.get(key);
        edge.count++;
        edge.value += txn.totalAmount;
    });
    
    const edges = Array.from(edgeMap.values());
    
    // Draw edges
    edges.forEach(edge => {
        const fromNode = nodes.find(n => n.id === edge.from);
        const toNode = nodes.find(n => n.id === edge.to);
        
        if (fromNode && toNode) {
            ctx.beginPath();
            ctx.moveTo(fromNode.x, fromNode.y);
            ctx.lineTo(toNode.x, toNode.y);
            ctx.strokeStyle = 'rgba(99, 102, 241, 0.3)';
            ctx.lineWidth = Math.min(edge.count, 10);
            ctx.stroke();
            
            // Draw arrow
            const angle = Math.atan2(toNode.y - fromNode.y, toNode.x - fromNode.x);
            const arrowSize = 10;
            ctx.save();
            ctx.translate(toNode.x - Math.cos(angle) * toNode.radius, 
                         toNode.y - Math.sin(angle) * toNode.radius);
            ctx.rotate(angle);
            ctx.beginPath();
            ctx.moveTo(0, 0);
            ctx.lineTo(-arrowSize, -arrowSize / 2);
            ctx.lineTo(-arrowSize, arrowSize / 2);
            ctx.closePath();
            ctx.fillStyle = 'rgba(99, 102, 241, 0.6)';
            ctx.fill();
            ctx.restore();
        }
    });
    
    // Draw nodes
    nodes.forEach(node => {
        ctx.beginPath();
        ctx.arc(node.x, node.y, node.radius, 0, 2 * Math.PI);
        ctx.fillStyle = node.color;
        ctx.fill();
        ctx.strokeStyle = 'white';
        ctx.lineWidth = 3;
        ctx.stroke();
        
        // Draw label
        ctx.fillStyle = '#1e293b';
        ctx.font = '11px Inter, sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText(node.name.substring(0, 20), node.x, node.y + node.radius + 15);
    });
    
    // Update network metrics
    const totalConnections = edges.length;
    const totalSuppliers = nodes.length;
    const maxConnections = (totalSuppliers * (totalSuppliers - 1)) / 2;
    const density = ((totalConnections / maxConnections) * 100).toFixed(1);
    const avgConnections = ((totalConnections * 2) / totalSuppliers).toFixed(1);
    
    document.getElementById('network-density').textContent = density + '%';
    document.getElementById('network-density-bar').style.width = density + '%';
    
    document.getElementById('network-connectivity').textContent = '85%'; // Synthetic
    document.getElementById('network-connectivity-bar').style.width = '85%';
    
    document.getElementById('avg-connections').textContent = avgConnections;
    document.getElementById('avg-connections-bar').style.width = Math.min((avgConnections / 10) * 100, 100) + '%';
    
    // Populate top connections
    const topConnections = edges.sort((a, b) => b.value - a.value).slice(0, 5);
    const topConnectionsList = document.getElementById('top-connections-list');
    topConnectionsList.innerHTML = topConnections.map((edge, index) => {
        const fromSupplier = syntheticSuppliers.find(s => s.id === edge.from);
        const toSupplier = syntheticSuppliers.find(s => s.id === edge.to);
        return `
            <div style="padding: 12px; background: var(--gray-50); border-radius: 8px; margin-bottom: 8px;">
                <div style="display: flex; justify-content: space-between; margin-bottom: 4px;">
                    <span style="font-size: 0.85rem; font-weight: 600;">#${index + 1}</span>
                    <span style="font-size: 0.85rem; font-weight: 700; color: var(--success-color);">$${edge.value.toLocaleString()}</span>
                </div>
                <div style="font-size: 0.8rem; color: var(--gray-600);">
                    ${fromSupplier?.name} → ${toSupplier?.name}
                </div>
                <div style="font-size: 0.75rem; color: var(--gray-500); margin-top: 4px;">
                    ${edge.count} transactions
                </div>
            </div>
        `;
    }).join('');
    
    // Populate network table
    populateNetworkTable(edges);
}

function getSupplierColor(type) {
    const colors = {
        'MANUFACTURER': '#6366f1',
        'DISTRIBUTOR': '#10b981',
        'WHOLESALER': '#8b5cf6',
        'RETAILER': '#06b6d4',
        'DROPSHIPPER': '#f59e0b',
        'AGENT': '#ef4444'
    };
    return colors[type] || '#6b7280';
}

function populateNetworkTable(edges) {
    const tableBody = document.getElementById('network-connections-table');
    tableBody.innerHTML = '';
    
    edges.slice(0, 20).forEach(edge => {
        const fromSupplier = syntheticSuppliers.find(s => s.id === edge.from);
        const toSupplier = syntheticSuppliers.find(s => s.id === edge.to);
        const avgValue = edge.value / edge.count;
        
        const row = document.createElement('tr');
        row.innerHTML = `
            <td style="font-size: 0.9rem;">${fromSupplier?.name || 'Unknown'}</td>
            <td style="font-size: 0.9rem;">${toSupplier?.name || 'Unknown'}</td>
            <td><strong>${edge.count}</strong></td>
            <td><strong style="color: var(--success-color);">$${edge.value.toLocaleString()}</strong></td>
            <td>$${avgValue.toLocaleString()}</td>
        `;
        tableBody.appendChild(row);
    });
}

// Toggle network view
document.addEventListener('DOMContentLoaded', function() {
    const viewButtons = document.querySelectorAll('[data-view]');
    viewButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            const view = this.getAttribute('data-view');
            viewButtons.forEach(b => b.classList.remove('active'));
            this.classList.add('active');
            
            if (view === 'graph') {
                document.getElementById('network-graph-view').classList.remove('hidden');
                document.getElementById('network-table-view').classList.add('hidden');
            } else {
                document.getElementById('network-graph-view').classList.add('hidden');
                document.getElementById('network-table-view').classList.remove('hidden');
            }
        });
    });
    
    // Refresh network
    document.getElementById('refresh-network')?.addEventListener('click', () => {
        initializeSupplyChainNetwork();
        showToast('Network refreshed successfully', 'success');
    });
});

// Modal handlers for suppliers
document.getElementById('add-supplier-btn')?.addEventListener('click', () => {
    document.getElementById('add-supplier-modal').classList.add('active');
});

document.getElementById('close-supplier-modal')?.addEventListener('click', () => {
    document.getElementById('add-supplier-modal').classList.remove('active');
});

document.getElementById('cancel-supplier')?.addEventListener('click', () => {
    document.getElementById('add-supplier-modal').classList.remove('active');
});

// Modal handlers for transactions
document.getElementById('add-transaction-btn')?.addEventListener('click', () => {
    document.getElementById('add-transaction-modal').classList.add('active');
    // Populate supplier dropdowns
    const fromSelect = document.getElementById('from-supplier');
    const toSelect = document.getElementById('to-supplier');
    
    syntheticSuppliers.forEach(supplier => {
        const option1 = new Option(supplier.name, supplier.id);
        const option2 = new Option(supplier.name, supplier.id);
        fromSelect.add(option1);
        toSelect.add(option2);
    });
});

document.getElementById('close-transaction-modal')?.addEventListener('click', () => {
    document.getElementById('add-transaction-modal').classList.remove('active');
});

document.getElementById('cancel-transaction')?.addEventListener('click', () => {
    document.getElementById('add-transaction-modal').classList.remove('active');
});