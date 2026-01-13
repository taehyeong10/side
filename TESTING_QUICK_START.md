# gRPC Testing - Quick Start

## 🚀 Step-by-Step Testing Guide

### Step 1: Build & Generate gRPC Code

```bash
./gradlew clean generateProto build
```

This generates Java classes from your `.proto` files.

### Step 2: Start the Application

```bash
./gradlew bootRun
```

You should see logs indicating both servers started:
- ✅ Tomcat started on port 8080 (REST)
- ✅ gRPC Server started on port 9090

### Step 3: Test the Endpoints

Choose one of the methods below:

---

## 📱 Method 1: Use the Test Script (Easiest!)

I've created a test script that runs all the tests for you:

```bash
# Install grpcurl first (one-time setup)
brew install grpcurl

# Run the test script
./test-grpc.sh
```

This will automatically test all gRPC endpoints and show you the results!

---

## 🔧 Method 2: Manual Testing with grpcurl

### Install grpcurl (one-time)

```bash
brew install grpcurl
```

### Basic Commands

```bash
# List all services
grpcurl -plaintext localhost:9090 list

# Get member by ID = 1
grpcurl -plaintext -d '{"id": 1}' \
  localhost:9090 member.MemberService/GetMember

# List all members
grpcurl -plaintext -d '{}' \
  localhost:9090 member.MemberService/ListMembers
```

---

## ☕ Method 3: Run the Java Client

You have a ready-to-use Java client in your project:

### Option A: Run from IntelliJ IDEA

1. Open `src/main/java/com/make/side/grpc/MemberGrpcClient.java`
2. Right-click on the file
3. Select **"Run 'MemberGrpcClient.main()'"**

### Option B: Run from Terminal

```bash
# Make sure you've built first
./gradlew build

# Run the client
java -cp "build/classes/java/main:build/libs/*" \
  com.make.side.grpc.MemberGrpcClient
```

---

## 🧪 Method 4: Run JUnit Tests

```bash
# Run all tests
./gradlew test

# Run just the gRPC test (requires server running)
# First, remove @Disabled from GrpcClientTest.java
# Then run:
./gradlew test --tests GrpcClientTest
```

---

## 📊 Expected Results

### ✅ Successful GetMember Request

**Request:**
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

### ✅ Member Not Found

**Request:**
```bash
grpcurl -plaintext -d '{"id": 999}' \
  localhost:9090 member.MemberService/GetMember
```

**Response:**
```json
{
  "found": false
}
```

### ✅ List All Members

**Request:**
```bash
grpcurl -plaintext -d '{}' \
  localhost:9090 member.MemberService/ListMembers
```

**Response:**
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

---

## 🔍 Troubleshooting

### Problem: "Failed to dial target host"

**Solution:** Make sure your application is running:
```bash
./gradlew bootRun
```

Check if port 9090 is listening:
```bash
lsof -i :9090
# or
netstat -an | grep 9090
```

### Problem: "command not found: grpcurl"

**Solution:** Install grpcurl:
```bash
brew install grpcurl
```

### Problem: "Unknown service member.MemberService"

**Solution:** Make sure you generated the proto files:
```bash
./gradlew generateProto build
```

Then restart your application.

### Problem: Generated classes not found

**Solution:** The generated files are in `build/generated/source/proto/main/`. Run:
```bash
./gradlew clean generateProto
```

Then refresh your IDE (IntelliJ: File → Invalidate Caches → Restart)

---

## 🎯 Quick Comparison: REST vs gRPC

Test both endpoints side-by-side:

```bash
# REST API (port 8080)
curl http://localhost:8080/member/1

# gRPC API (port 9090)
grpcurl -plaintext -d '{"id": 1}' \
  localhost:9090 member.MemberService/GetMember
```

Notice:
- gRPC response includes a `found` field
- gRPC uses typed messages (more structured)
- REST returns 404 for not found, gRPC returns `found: false`

---

## 📚 Next Steps

Once you've verified the basic endpoints work:

1. **Implement CreateMember**: Add member creation functionality
2. **Add Text Service**: Create gRPC endpoints for text operations
3. **Add Authentication**: Implement gRPC interceptors
4. **Performance Test**: Use `ghz` to benchmark (install with `brew install ghz`)
5. **Streaming Examples**: Try real-time streaming with gRPC

---

## 🎓 Learning Resources

- 📖 Full setup guide: `GRPC_SETUP.md`
- 💻 Command reference: `GRPC_COMMANDS.md`
- ⚖️ REST vs gRPC comparison: `REST_VS_GRPC.md`

---

## 💡 Pro Tips

1. **Use `-v` flag for verbose output:**
   ```bash
   grpcurl -plaintext -v -d '{"id": 1}' \
     localhost:9090 member.MemberService/GetMember
   ```

2. **Pretty print with jq:**
   ```bash
   grpcurl -plaintext -d '{"id": 1}' \
     localhost:9090 member.MemberService/GetMember | jq
   ```

3. **Save requests to files:**
   ```bash
   echo '{"id": 1}' > request.json
   grpcurl -plaintext -d @ \
     localhost:9090 member.MemberService/GetMember < request.json
   ```

4. **Test with timeout:**
   ```bash
   grpcurl -plaintext -max-time 5 -d '{"id": 1}' \
     localhost:9090 member.MemberService/GetMember
   ```

Happy testing! 🚀
