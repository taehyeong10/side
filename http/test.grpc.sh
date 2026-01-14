#!/bin/bash
# gRPC Test Commands
# Run individual commands by copying them to your terminal
# Or run this entire file: ./http/test.grpc.sh

BASE_URL="localhost:9090"

echo "gRPC Tests for Member Service"
echo "=============================="
echo ""

# Uncomment the tests you want to run:

# Test 1: List all services
echo "1. List all services:"
grpcurl -plaintext $BASE_URL list
echo ""

# Test 2: List MemberService methods
echo "2. List MemberService methods:"
grpcurl -plaintext $BASE_URL list member.MemberService
echo ""

# Test 3: Get member by ID = 1
echo "3. Get member by ID = 1:"
grpcurl -plaintext -d '{"id": 1}' $BASE_URL member.MemberService/GetMember
echo ""

# Test 4: Get member by ID = 2
echo "4. Get member by ID = 2:"
grpcurl -plaintext -d '{"id": 2}' $BASE_URL member.MemberService/GetMember
echo ""

# Test 5: Get non-existing member
echo "5. Get non-existing member (ID = 999):"
grpcurl -plaintext -d '{"id": 999}' $BASE_URL member.MemberService/GetMember
echo ""

# Test 6: List all members
echo "6. List all members:"
grpcurl -plaintext -d '{}' $BASE_URL member.MemberService/ListMembers
echo ""

# Test 7: Try to create member (will fail - UNIMPLEMENTED)
echo "7. Try to create member (expect UNIMPLEMENTED):"
grpcurl -plaintext -d '{"name": "New User"}' $BASE_URL member.MemberService/CreateMember 2>&1 || true
echo ""

echo "All tests completed!"
