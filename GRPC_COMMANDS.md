# gRPC Quick Reference Commands

## Using grpcurl

### Install grpcurl

```bash
# macOS
brew install grpcurl

# Linux with Go
go install github.com/fullstorydev/grpcurl/cmd/grpcurl@latest

# Or download from: https://github.com/fullstorydev/grpcurl/releases
```

### List all services

```bash
grpcurl -plaintext localhost:9090 list
```

Expected output:
```
grpc.reflection.v1alpha.ServerReflection
member.MemberService
```

### List methods of MemberService

```bash
grpcurl -plaintext localhost:9090 list member.MemberService
```

Expected output:
```
member.MemberService.CreateMember
member.MemberService.GetMember
member.MemberService.ListMembers
```

### Describe a method

```bash
grpcurl -plaintext localhost:9090 describe member.MemberService.GetMember
```

### Get Member by ID

```bash
# Get member with ID 1
grpcurl -plaintext -d '{"id": 1}' \
  localhost:9090 member.MemberService/GetMember

# Expected response:
# {
#   "member": {
#     "id": "1",
#     "name": "John Doe"
#   },
#   "found": true
# }
```

```bash
# Get non-existing member
grpcurl -plaintext -d '{"id": 999}' \
  localhost:9090 member.MemberService/GetMember

# Expected response:
# {
#   "found": false
# }
```

### List All Members

```bash
grpcurl -plaintext -d '{}' \
  localhost:9090 member.MemberService/ListMembers

# Expected response:
# {
#   "members": [
#     {
#       "id": "1",
#       "name": "John Doe"
#     },
#     {
#       "id": "2",
#       "name": "Jane Smith"
#     }
#   ]
# }
```

### Create Member (currently returns UNIMPLEMENTED)

```bash
grpcurl -plaintext -d '{"name": "New User"}' \
  localhost:9090 member.MemberService/CreateMember

# Expected response:
# ERROR:
#   Code: Unimplemented
#   Message: CreateMember not yet implemented
```

## Using the Java Client

### Run the standalone client

```bash
# Make sure the server is running first
./gradlew bootRun

# In another terminal, run the client
./gradlew run --args="com.make.side.grpc.MemberGrpcClient"
```

Or compile and run directly:

```bash
./gradlew build
java -cp build/libs/side-0.0.1-SNAPSHOT.jar:build/classes/java/main \
  com.make.side.grpc.MemberGrpcClient
```

## Comparing REST vs gRPC

### REST API (Port 8080)

```bash
# Get member via REST
curl http://localhost:8080/member/1

# Response:
# {"id":1,"name":"John Doe"}
```

### gRPC (Port 9090)

```bash
# Get member via gRPC
grpcurl -plaintext -d '{"id": 1}' \
  localhost:9090 member.MemberService/GetMember

# Response:
# {
#   "member": {
#     "id": "1",
#     "name": "John Doe"
#   },
#   "found": true
# }
```

## Performance Testing with ghz

Install ghz (gRPC benchmarking tool):

```bash
# macOS
brew install ghz

# Or: go install github.com/bojand/ghz/cmd/ghz@latest
```

Benchmark the GetMember endpoint:

```bash
ghz --insecure \
  --proto ./src/main/proto/member.proto \
  --call member.MemberService.GetMember \
  -d '{"id": 1}' \
  -n 10000 \
  -c 100 \
  localhost:9090
```

This will:
- Make 10,000 requests
- With 100 concurrent connections
- Show latency, throughput, and error rates

## Debugging Tips

### Check if gRPC server is running

```bash
# Check if port 9090 is listening
lsof -i :9090

# Or
netstat -an | grep 9090
```

### View gRPC server logs

The Spring Boot application will log gRPC calls:

```
INFO  c.m.s.grpc.MemberGrpcService - gRPC GetMember called with id: 1
INFO  c.m.s.grpc.MemberGrpcService - Returned 5 members
```

### Test with timeout

```bash
# Set a 5-second timeout
grpcurl -plaintext -max-time 5 \
  -d '{"id": 1}' \
  localhost:9090 member.MemberService/GetMember
```

### Pretty print JSON response

```bash
grpcurl -plaintext -d '{"id": 1}' \
  localhost:9090 member.MemberService/GetMember | jq
```

## Advanced Usage

### Using metadata (headers)

```bash
grpcurl -plaintext \
  -H "authorization: Bearer token123" \
  -d '{"id": 1}' \
  localhost:9090 member.MemberService/GetMember
```

### Verbose output (see request/response details)

```bash
grpcurl -plaintext -v \
  -d '{"id": 1}' \
  localhost:9090 member.MemberService/GetMember
```

### Save request/response to files

```bash
# Request
echo '{"id": 1}' > request.json

# Call with file input
grpcurl -plaintext -d @ localhost:9090 member.MemberService/GetMember < request.json
```

## Troubleshooting

### Error: "Failed to dial target host"

- Check if the application is running: `./gradlew bootRun`
- Verify the port: `lsof -i :9090`
- Check application.yaml for correct gRPC port configuration

### Error: "server does not support the reflection API"

- Ensure reflection is enabled in application.yaml:
  ```yaml
  grpc:
    server:
      enable-reflection: true
  ```

### Error: "could not resolve method"

- Make sure you've run `./gradlew generateProto` after creating/modifying .proto files
- Rebuild the project: `./gradlew clean build`

### Error: "UNIMPLEMENTED"

- This is expected for methods not yet implemented (like CreateMember)
- Check the server implementation in `MemberGrpcService.java`
