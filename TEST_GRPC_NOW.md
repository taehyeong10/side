# Test gRPC Right Now - Step by Step Guide

Follow these steps to test your gRPC endpoints in the next 2 minutes!

## ⚡ Quick Start (2 minutes)

### Terminal 1: Start Your App

```bash
cd /Users/rook/IdeaProjects/side
./gradlew bootRun
```

**Wait for these log messages:**
```
Started SideApplication in X.XXX seconds
Tomcat started on port(s): 8080
gRPC Server started, listening on address: *, port: 9090
```

### Terminal 2: Install grpcurl (if not installed)

```bash
# Check if already installed
which grpcurl

# If not installed, install it:
brew install grpcurl
```

### Terminal 2: Run Your First gRPC Call!

```bash
# Test 1: Is gRPC working?
grpcurl -plaintext localhost:9090 list
```

**Expected output:**
```
grpc.reflection.v1alpha.ServerReflection
member.MemberService
```

✅ If you see this, gRPC is working!

```bash
# Test 2: Get a member
grpcurl -plaintext -d '{"id": 1}' \
  localhost:9090 member.MemberService/GetMember
```

**Expected output:**
```json
{
  "member": {
    "id": "1",
    "name": "John Doe"
  },
  "found": true
}
```

✅ Success! You just made your first gRPC call!

---

## 🎯 Full Test Suite (5 minutes)

Now try all the endpoints:

### Test 1: Get Member by ID

```bash
# Get member ID 1
grpcurl -plaintext -d '{"id": 1}' \
  localhost:9090 member.MemberService/GetMember

# Get member ID 2
grpcurl -plaintext -d '{"id": 2}' \
  localhost:9090 member.MemberService/GetMember
```

### Test 2: Member Not Found

```bash
# Try non-existing member
grpcurl -plaintext -d '{"id": 999}' \
  localhost:9090 member.MemberService/GetMember
```

**Expected:**
```json
{
  "found": false
}
```

### Test 3: List All Members

```bash
grpcurl -plaintext -d '{}' \
  localhost:9090 member.MemberService/ListMembers
```

**Expected:**
```json
{
  "members": [
    {
      "id": "1",
      "name": "John Doe"
    },
    {
      "id": "2",
      "name": "Jane Smith"
    }
  ]
}
```

### Test 4: Try Create Member (expect error)

```bash
grpcurl -plaintext -d '{"name": "New User"}' \
  localhost:9090 member.MemberService/CreateMember
```

**Expected:**
```
ERROR:
  Code: Unimplemented
  Message: CreateMember not yet implemented
```

✅ This is correct! We haven't implemented CreateMember yet.

---

## 🔍 Advanced Testing

### See Full Request/Response Details

```bash
grpcurl -plaintext -v -d '{"id": 1}' \
  localhost:9090 member.MemberService/GetMember
```

This shows headers, timing, and more!

### Pretty Print with jq

```bash
# Install jq if needed
brew install jq

# Pretty print JSON
grpcurl -plaintext -d '{"id": 1}' \
  localhost:9090 member.MemberService/GetMember | jq
```

### Test with Timeout

```bash
grpcurl -plaintext -max-time 5 -d '{"id": 1}' \
  localhost:9090 member.MemberService/GetMember
```

### Describe a Method (see the signature)

```bash
grpcurl -plaintext localhost:9090 \
  describe member.MemberService.GetMember
```

**Output:**
```
member.MemberService.GetMember is a method:
rpc GetMember ( .member.GetMemberRequest ) returns ( .member.GetMemberResponse );
```

---

## 🆚 Compare REST vs gRPC

Run both and see the difference:

### REST API (port 8080):

```bash
curl http://localhost:8080/member/1
```

**Response:**
```json
{"id":1,"name":"John Doe"}
```

### gRPC API (port 9090):

```bash
grpcurl -plaintext -d '{"id": 1}' \
  localhost:9090 member.MemberService/GetMember
```

**Response:**
```json
{
  "member": {
    "id": "1",
    "name": "John Doe"
  },
  "found": true
}
```

**Notice:**
- gRPC has explicit `found` field
- gRPC uses nested structure
- Both hit the same business logic!

---

## 🚀 Automated Testing

### Option 1: Run the main test script

```bash
./test-grpc.sh
```

This runs all tests and shows results.

### Option 2: Run the HTTP directory script

```bash
./http/test.grpc.sh
```

### Option 3: Run the Java client

From IntelliJ:
1. Open `src/main/java/com/make/side/grpc/MemberGrpcClient.java`
2. Right-click → Run 'MemberGrpcClient.main()'

Or from terminal:
```bash
./gradlew build
java -cp "build/classes/java/main:build/libs/*" \
  com.make.side.grpc.MemberGrpcClient
```

---

## 📊 Testing Checklist

- [ ] Installed grpcurl
- [ ] Started application (`./gradlew bootRun`)
- [ ] Listed services (saw `member.MemberService`)
- [ ] Got member by ID (received member data)
- [ ] Got non-existing member (received `found: false`)
- [ ] Listed all members (received list)
- [ ] Tried CreateMember (received UNIMPLEMENTED error)
- [ ] Compared REST vs gRPC
- [ ] Ran automated test script

---

## 🎓 What You've Learned

After completing these tests, you now know how to:

✅ Start a gRPC server (Spring Boot with @GrpcService)  
✅ Call gRPC endpoints with grpcurl  
✅ Read gRPC responses  
✅ Handle missing data (found: false)  
✅ Test both REST and gRPC endpoints  
✅ Use automated test scripts  

---

## 🐛 Common Issues

### "Cannot assign requested address"

Your app isn't running. Start it with:
```bash
./gradlew bootRun
```

### "Unknown service"

Generate proto files:
```bash
./gradlew generateProto build
```
Then restart your app.

### "Connection refused"

Check if port 9090 is in use:
```bash
lsof -i :9090
```

---

## 📚 Next Steps

Now that you can test gRPC:

1. **Implement CreateMember** - Add the missing functionality
2. **Add Text Service** - Create gRPC endpoints for text operations
3. **Performance Test** - Use `ghz` to benchmark:
   ```bash
   brew install ghz
   ghz --insecure --proto ./src/main/proto/member.proto \
     --call member.MemberService.GetMember \
     -d '{"id": 1}' -n 1000 -c 10 localhost:9090
   ```
4. **Create a Real Client** - Build another microservice that calls your gRPC API

---

## 🎉 Congratulations!

You now know how to test gRPC endpoints! 🚀

For more details, see:
- `GRPC_EXPLAINED.md` - Deep dive into how gRPC works
- `GRPC_SETUP.md` - Full setup documentation
- `GRPC_COMMANDS.md` - Command reference
- `REST_VS_GRPC.md` - Comparison guide
