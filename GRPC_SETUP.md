# gRPC Setup Guide

## Overview

This application now supports both **REST API** (port 8080) and **gRPC** (port 9090) for accessing the same services.

## Architecture

```
┌─────────────────┐
│   REST Client   │──────> Port 8080 (HTTP/REST)
└─────────────────┘              │
                                 v
┌─────────────────┐         ┌──────────────────┐
│   gRPC Client   │──────>  │  Spring Boot App │
└─────────────────┘         └──────────────────┘
    Port 9090 (gRPC)               │
                                   v
                            ┌──────────────┐
                            │  Services    │
                            │  (Shared)    │
                            └──────────────┘
```

## What Was Added

### 1. Dependencies (build.gradle)
- `grpc-spring-boot-starter`: Spring Boot integration for gRPC
- `grpc-protobuf` & `grpc-stub`: Core gRPC libraries
- `protobuf-java`: Protocol Buffers support
- Protobuf Gradle plugin for code generation

### 2. Proto Files (src/main/proto/)
- `member.proto`: Defines the gRPC service contract

### 3. gRPC Service Implementation
- `MemberGrpcService.java`: Implements the gRPC endpoints
- Uses the existing `MemberService` (shared with REST API)

### 4. Configuration (application.yaml)
- gRPC server port: 9090
- gRPC reflection enabled for testing

## Building the Project

After making changes to `.proto` files, generate the gRPC code:

```bash
./gradlew clean generateProto build
```

This generates Java classes in `build/generated/source/proto/main/`:
- `Member.java`, `GetMemberRequest.java`, etc. - Message classes
- `MemberServiceGrpc.java` - Service stub classes

## Running the Application

Start the application normally:

```bash
./gradlew bootRun
```

You'll see both servers start:
- REST API: http://localhost:8080
- gRPC Server: localhost:9090

## Testing the gRPC Service

### Option 1: Using grpcurl (Recommended)

Install grpcurl:
```bash
# macOS
brew install grpcurl

# Linux
go install github.com/fullstorydev/grpcurl/cmd/grpcurl@latest
```

List available services:
```bash
grpcurl -plaintext localhost:9090 list
```

Get member by ID:
```bash
grpcurl -plaintext -d '{"id": 1}' \
  localhost:9090 member.MemberService/GetMember
```

Expected response:
```json
{
  "member": {
    "id": "1",
    "name": "John Doe"
  },
  "found": true
}
```

### Option 2: Using BloomRPC / Postman

1. Download [BloomRPC](https://github.com/bloomrpc/bloomrpc) or use Postman (with gRPC support)
2. Import the proto file: `src/main/proto/member.proto`
3. Set the server address: `localhost:9090`
4. Make requests through the GUI

### Option 3: Java Client

Create a Java gRPC client:

```java
import com.make.side.grpc.generated.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GrpcClientExample {
    public static void main(String[] args) {
        // Create a channel to the server
        ManagedChannel channel = ManagedChannelBuilder
            .forAddress("localhost", 9090)
            .usePlaintext()
            .build();
        
        // Create a blocking stub
        MemberServiceGrpc.MemberServiceBlockingStub stub = 
            MemberServiceGrpc.newBlockingStub(channel);
        
        // Make a request
        GetMemberRequest request = GetMemberRequest.newBuilder()
            .setId(1L)
            .build();
        
        GetMemberResponse response = stub.getMember(request);
        
        if (response.getFound()) {
            System.out.println("Member found:");
            System.out.println("  ID: " + response.getMember().getId());
            System.out.println("  Name: " + response.getMember().getName());
        } else {
            System.out.println("Member not found");
        }
        
        // Shutdown the channel
        channel.shutdown();
    }
}
```

## Adding More gRPC Services

### 1. Define the service in a .proto file

Create `src/main/proto/your_service.proto`:

```protobuf
syntax = "proto3";

option java_multiple_files = true;
option java_package = "com.make.side.grpc.generated";

package yourservice;

service YourService {
  rpc YourMethod (YourRequest) returns (YourResponse);
}

message YourRequest {
  string input = 1;
}

message YourResponse {
  string output = 1;
}
```

### 2. Generate code

```bash
./gradlew generateProto
```

### 3. Implement the service

Create a new service class:

```java
@GrpcService
public class YourGrpcService extends YourServiceGrpc.YourServiceImplBase {
    
    @Override
    public void yourMethod(YourRequest request, 
                          StreamObserver<YourResponse> responseObserver) {
        // Your implementation
        YourResponse response = YourResponse.newBuilder()
            .setOutput("Hello " + request.getInput())
            .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
```

## gRPC vs REST: When to Use What?

### Use gRPC when:
- ✅ You need high performance / low latency
- ✅ Building microservice-to-microservice communication
- ✅ Streaming data (server streaming, client streaming, bi-directional)
- ✅ Strong type safety is important
- ✅ Your clients are also Java/Go/C++/Python (good gRPC support)

### Use REST when:
- ✅ Building public APIs for web browsers
- ✅ You need simple, human-readable requests
- ✅ Your clients are diverse (JavaScript, mobile, etc.)
- ✅ You want to use standard HTTP tools (curl, browsers)

## Common Issues & Solutions

### Issue: "io.grpc.StatusRuntimeException: UNAVAILABLE"
- **Solution**: Make sure the gRPC server is running and listening on port 9090

### Issue: Generated classes not found
- **Solution**: Run `./gradlew generateProto` to generate the code

### Issue: Port 9090 already in use
- **Solution**: Change the port in `application.yaml` under `grpc.server.port`

### Issue: Cannot find javax.annotation.Generated
- **Solution**: Already handled by adding `annotations-api` dependency

## Performance Tips

1. **Use Async Stubs** for non-blocking calls
2. **Enable Keep-Alive** for long-lived connections
3. **Use Compression** for large payloads
4. **Connection Pooling** for client-side performance

## Next Steps

1. Implement the remaining methods (`CreateMember`, `ListMembers`)
2. Add gRPC interceptors for authentication/logging
3. Add gRPC metrics and monitoring
4. Create a Text service with gRPC support
5. Consider using gRPC-Web for browser clients

## References

- [gRPC Official Documentation](https://grpc.io/docs/)
- [grpc-spring-boot-starter](https://github.com/yidongnan/grpc-spring-boot-starter)
- [Protocol Buffers Guide](https://protobuf.dev/programming-guides/proto3/)
