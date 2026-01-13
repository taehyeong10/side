#!/bin/bash

# gRPC Testing Script
# Run this after starting your application with: ./gradlew bootRun

echo "╔════════════════════════════════════════╗"
echo "║   gRPC Endpoint Testing Script        ║"
echo "╚════════════════════════════════════════╝"
echo ""

# Check if grpcurl is installed
if ! command -v grpcurl &> /dev/null; then
    echo "❌ grpcurl is not installed!"
    echo "Install it with: brew install grpcurl"
    exit 1
fi

echo "✓ grpcurl is installed"
echo ""

# Check if server is running
echo "Checking if gRPC server is running on port 9090..."
if ! nc -z localhost 9090 2>/dev/null; then
    echo "❌ gRPC server is not running on port 9090"
    echo "Start it with: ./gradlew bootRun"
    exit 1
fi

echo "✓ gRPC server is running"
echo ""

# Test 1: List services
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Test 1: List Available Services"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
grpcurl -plaintext localhost:9090 list
echo ""

# Test 2: List methods
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Test 2: List MemberService Methods"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
grpcurl -plaintext localhost:9090 list member.MemberService
echo ""

# Test 3: Get member by ID
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Test 3: Get Member by ID (ID=1)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
grpcurl -plaintext -d '{"id": 1}' \
  localhost:9090 member.MemberService/GetMember
echo ""

# Test 4: Get non-existing member
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Test 4: Get Non-Existing Member (ID=999)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
grpcurl -plaintext -d '{"id": 999}' \
  localhost:9090 member.MemberService/GetMember
echo ""

# Test 5: List all members
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Test 5: List All Members"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
grpcurl -plaintext -d '{}' \
  localhost:9090 member.MemberService/ListMembers
echo ""

# Test 6: Create member (should fail with UNIMPLEMENTED)
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Test 6: Create Member (Expect UNIMPLEMENTED)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
grpcurl -plaintext -d '{"name": "Test User"}' \
  localhost:9090 member.MemberService/CreateMember 2>&1 || true
echo ""

# Test 7: Compare with REST
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Test 7: Compare gRPC vs REST"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "REST API Response (port 8080):"
curl -s http://localhost:8080/member/1 | jq '.' || curl -s http://localhost:8080/member/1
echo ""
echo ""
echo "gRPC API Response (port 9090):"
grpcurl -plaintext -d '{"id": 1}' \
  localhost:9090 member.MemberService/GetMember
echo ""

echo "╔════════════════════════════════════════╗"
echo "║   All Tests Completed!                 ║"
echo "╚════════════════════════════════════════╝"
