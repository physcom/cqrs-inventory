#!/bin/bash

# Load testing script using Apache Bench
echo "Starting load test for inventory system..."

# Test product creation
echo "Testing product creation..."
ab -n 1000 -c 50 -p product-data.json -T application/json \
   http://localhost:5003/api/products

# Test product queries
echo "Testing product queries..."
ab -n 5000 -c 100 http://localhost:5003/api/products/1

# Test search functionality
echo "Testing search..."
ab -n 2000 -c 50 "http://localhost:5003/api/products/search?q=test"

# Test sales processing
echo "Testing sales processing..."
ab -n 1000 -c 30 -p sale-data.json -T application/json \
   http://localhost:5003/api/sales

# Test analytics
echo "Testing analytics..."
ab -n 500 -c 25 "http://localhost:5003/api/analytics/sales?fromDate=2024-01-01T00:00:00&toDate=2025-12-31T23:59:59"

echo "Load test completed!"