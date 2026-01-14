# gRPC Components Explained

## 🎯 The Big Picture

Think of gRPC like a phone call between two applications. You need:
1. **A phone book** (member.proto) - defines what you can ask for
2. **Someone to answer calls** (MemberGrpcService.java) - handles incoming requests
3. **Someone to make calls** (MemberGrpcClient.java) - sends requests

```
┌─────────────────────────────────────────────────────────────────┐
│                         gRPC Flow                               │
└─────────────────────────────────────────────────────────────────┘

    member.proto (Contract)           Generated Code
         │                                  │
         │ ./gradlew generateProto          │
         │                                  │
         └──────────────┬───────────────────┘
                        │
         ┌──────────────┴──────────────┐
         │                             │
         ▼                             ▼
    SERVER SIDE                   CLIENT SIDE
    
MemberGrpcService.java      MemberGrpcClient.java
(Implements service)        (Calls service)
         │                             │
         │      gRPC Call (port 9090) │
         │◄───────────────────────────┤
         │                             │
         │      Response               │
         ├────────────────────────────►│
         │                             │
```

---

## 1️⃣ member.proto - The Contract (API Definition)

### What is it?
A **Protocol Buffer** file that defines your API contract - like an interface or OpenAPI spec.

### Why do we need it?
- **Single source of truth**: Both client and server agree on the API
- **Type safety**: Strongly typed messages (no more wrong JSON fields!)
- **Language agnostic**: Can generate code for Java, Go, Python, JavaScript, etc.
- **Versioning**: Easy to evolve APIs while maintaining compatibility

### What's inside?

```protobuf
// 1. Service Definition - What operations are available?
service MemberService {
  rpc GetMember (GetMemberRequest) returns (GetMemberResponse);
  rpc CreateMember (CreateMemberRequest) returns (CreateMemberResponse);
  rpc ListMembers (ListMembersRequest) returns (ListMembersResponse);
}

// 2. Message Definitions - What data is exchanged?
message Member {
  int64 id = 1;        // Field number 1
  string name = 2;     // Field number 2
}

message GetMemberRequest {
  int64 id = 1;
}

message GetMemberResponse {
  Member member = 1;
  bool found = 2;
}
```

### Breaking it down:

#### Service = Collection of Methods
```protobuf
service MemberService {
  rpc GetMember (GetMemberRequest) returns (GetMemberResponse);
  //  │           │                          │
  //  └─ Method   └─ Input                   └─ Output
}
```

This is like defining a Java interface:
```java
interface MemberService {
    GetMemberResponse getMember(GetMemberRequest request);
}
```

#### Messages = Data Structures
```protobuf
message Member {
  int64 id = 1;      // The numbers (1, 2) are field tags, not values!
  string name = 2;   // Used for efficient binary encoding
}
```

This becomes a Java class:
```java
public class Member {
    private long id;
    private String name;
    // ... getters, setters, builders
}
```

### What happens when you run `./gradlew generateProto`?

The proto file generates these Java classes (in `build/generated/source/proto/main/`):

```
member.proto
    │
    └──► Generates:
         ├── Member.java                    (Data class)
         ├── GetMemberRequest.java          (Request class)
         ├── GetMemberResponse.java         (Response class)
         ├── CreateMemberRequest.java
         ├── CreateMemberResponse.java
         ├── ListMembersRequest.java
         ├── ListMembersResponse.java
         └── MemberServiceGrpc.java         (Service stubs)
                 ├── MemberServiceImplBase  (For server)
                 ├── MemberServiceStub      (Async client)
                 └── MemberServiceBlockingStub (Sync client)
```

---

## 2️⃣ MemberGrpcService.java - The Server Implementation

### What is it?
The **server-side implementation** that handles incoming gRPC calls.

### Why do we need it?
- **Handles requests**: When a client calls `GetMember`, this code runs
- **Business logic**: Connects gRPC calls to your existing services
- **Runs in your app**: Starts when your Spring Boot app starts

### How does it work?

```java
@GrpcService  // ← Makes this a gRPC endpoint (like @RestController)
public class MemberGrpcService extends MemberServiceGrpc.MemberServiceImplBase {
    //                                  └─ Generated from member.proto
    
    private final MemberService memberService;  // ← Reuses existing service!
    
    @Override
    public void getMember(
        GetMemberRequest request,              // ← Input (from proto)
        StreamObserver<GetMemberResponse> responseObserver  // ← Output channel
    ) {
        // 1. Extract data from request
        long id = request.getId();
        
        // 2. Call your existing business logic
        Optional<MemberDto> memberDto = memberService.findById(id);
        
        // 3. Build gRPC response
        GetMemberResponse response = GetMemberResponse.newBuilder()
            .setMember(Member.newBuilder()
                .setId(memberDto.get().id())
                .setName(memberDto.get().name())
                .build())
            .setFound(true)
            .build();
        
        // 4. Send response back to client
        responseObserver.onNext(response);      // ← Send the response
        responseObserver.onCompleted();         // ← Mark as done
    }
}
```

### Key Concepts:

#### 1. StreamObserver - The Response Channel
Think of it like a pipe to send data back:

```java
responseObserver.onNext(response);    // Send data
responseObserver.onCompleted();       // Close the pipe (success)
responseObserver.onError(exception);  // Close with error
```

#### 2. Builder Pattern
All protobuf messages use builders:

```java
Member member = Member.newBuilder()
    .setId(1L)
    .setName("John")
    .build();
```

#### 3. Comparison with REST Controller

**REST Controller:**
```java
@RestController
public class MemberController {
    @GetMapping("/member/{id}")
    public ResponseEntity<MemberDto> getMember(@PathVariable Long id) {
        Optional<MemberDto> member = memberService.findById(id);
        return member.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }
}
```

**gRPC Service:**
```java
@GrpcService
public class MemberGrpcService extends MemberServiceGrpc.MemberServiceImplBase {
    @Override
    public void getMember(GetMemberRequest request, 
                         StreamObserver<GetMemberResponse> responseObserver) {
        Optional<MemberDto> member = memberService.findById(request.getId());
        // Build and send response...
    }
}
```

**Same business logic, different transport!**

---

## 3️⃣ MemberGrpcClient.java - The Client Implementation

### What is it?
A **standalone Java application** that calls your gRPC server (for testing or as an example).

### Why do we need it?
- **Testing**: Verify your gRPC server works
- **Example**: Shows developers how to call your gRPC service
- **Integration**: Can be used in other Java microservices

### How does it work?

```java
public class MemberGrpcClient {
    
    // 1. Create a connection to the server
    ManagedChannel channel = ManagedChannelBuilder
        .forAddress("localhost", 9090)  // ← gRPC server address
        .usePlaintext()                 // ← No TLS (for local dev)
        .build();
    
    // 2. Create a "stub" - a client that makes calls
    MemberServiceBlockingStub stub = MemberServiceGrpc.newBlockingStub(channel);
    //                       └─ "Blocking" = waits for response (synchronous)
    
    // 3. Build a request
    GetMemberRequest request = GetMemberRequest.newBuilder()
        .setId(1L)
        .build();
    
    // 4. Make the call!
    GetMemberResponse response = stub.getMember(request);
    //                                └─ Calls your server
    
    // 5. Use the response
    if (response.getFound()) {
        System.out.println("ID: " + response.getMember().getId());
        System.out.println("Name: " + response.getMember().getName());
    }
    
    // 6. Close the connection
    channel.shutdown();
}
```

### Types of Stubs:

gRPC provides different ways to call services:

```java
// 1. Blocking Stub (Synchronous) - waits for response
MemberServiceBlockingStub blockingStub = MemberServiceGrpc.newBlockingStub(channel);
GetMemberResponse response = blockingStub.getMember(request);  // Waits here

// 2. Async Stub (Asynchronous) - doesn't wait
MemberServiceStub asyncStub = MemberServiceGrpc.newStub(channel);
asyncStub.getMember(request, new StreamObserver<GetMemberResponse>() {
    @Override
    public void onNext(GetMemberResponse response) {
        // Handle response when it arrives
    }
    
    @Override
    public void onCompleted() {
        // Called when done
    }
    
    @Override
    public void onError(Throwable t) {
        // Handle errors
    }
});

// 3. Future Stub (Returns a Future)
MemberServiceFutureStub futureStub = MemberServiceGrpc.newFutureStub(channel);
ListenableFuture<GetMemberResponse> future = futureStub.getMember(request);
// ... use future.get() or add callbacks
```

---

## 📊 Complete Flow Example

Let's trace what happens when you call `getMember(1)`:

### Step 1: Client Makes Request

```java
// In MemberGrpcClient.java
GetMemberRequest request = GetMemberRequest.newBuilder()
    .setId(1L)
    .build();

GetMemberResponse response = blockingStub.getMember(request);
```

### Step 2: Request Sent Over Network

```
Client (port ?)  ────── gRPC Call ──────►  Server (port 9090)
                  (Binary Protobuf)
                  
Request:
{
  id: 1
}
```

### Step 3: Server Receives and Processes

```java
// In MemberGrpcService.java
@Override
public void getMember(GetMemberRequest request, 
                     StreamObserver<GetMemberResponse> responseObserver) {
    
    // 1. Extract ID from request
    long id = request.getId();  // id = 1
    
    // 2. Call business logic (same as REST!)
    Optional<MemberDto> memberDto = memberService.findById(id);
    //                              └─ Your existing service!
    
    // 3. Convert to gRPC message
    Member grpcMember = Member.newBuilder()
        .setId(memberDto.get().id())
        .setName(memberDto.get().name())
        .build();
    
    // 4. Build response
    GetMemberResponse response = GetMemberResponse.newBuilder()
        .setMember(grpcMember)
        .setFound(true)
        .build();
    
    // 5. Send back to client
    responseObserver.onNext(response);
    responseObserver.onCompleted();
}
```

### Step 4: Response Sent Back

```
Client (port ?)  ◄────── gRPC Response ──────  Server (port 9090)
                   (Binary Protobuf)
                   
Response:
{
  member: {
    id: 1,
    name: "John Doe"
  },
  found: true
}
```

### Step 5: Client Receives Response

```java
// Back in MemberGrpcClient.java
GetMemberResponse response = blockingStub.getMember(request);

if (response.getFound()) {
    System.out.println("ID: " + response.getMember().getId());      // 1
    System.out.println("Name: " + response.getMember().getName());  // John Doe
}
```

---

## 🔄 Architecture Overview

```
┌──────────────────────────────────────────────────────────────────┐
│                      Your Application                            │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌────────────────┐              ┌────────────────┐            │
│  │ REST Controller│              │ gRPC Service   │            │
│  │ (port 8080)    │              │ (port 9090)    │            │
│  └────────┬───────┘              └────────┬───────┘            │
│           │                               │                     │
│           └───────────┬───────────────────┘                     │
│                       │                                         │
│              ┌────────▼────────┐                               │
│              │  MemberService  │  ← Shared Business Logic      │
│              │  (Service Layer)│                               │
│              └────────┬────────┘                               │
│                       │                                         │
│              ┌────────▼────────┐                               │
│              │   Repository    │                               │
│              └────────┬────────┘                               │
│                       │                                         │
└───────────────────────┼─────────────────────────────────────────┘
                        │
                   ┌────▼────┐
                   │Database │
                   └─────────┘

External Clients:
┌────────────┐                        ┌─────────────┐
│Web Browser │─── HTTP/JSON ─────────►│REST (8080)  │
└────────────┘                        └─────────────┘

┌────────────┐                        ┌─────────────┐
│Java Client │─── gRPC/Protobuf ─────►│gRPC (9090)  │
└────────────┘                        └─────────────┘
```

---

## 🎯 Real-World Usage Scenarios

### Scenario 1: Web Application
```
Browser ──REST──► Your App (REST API on 8080)
                       │
                       └──► MemberService ──► Database
```
**Use REST** because browsers understand HTTP/JSON easily.

### Scenario 2: Microservice-to-Microservice
```
Payment Service ──gRPC──► Your App (gRPC on 9090)
                               │
                               └──► MemberService ──► Database
```
**Use gRPC** because it's faster and type-safe.

### Scenario 3: Hybrid (Your Current Setup!)
```
Browser ──REST──┐
                ├──► Your App ──► MemberService ──► Database
Java Client ──gRPC──┘
```
**Both!** REST for web clients, gRPC for internal services.

---

## 🔍 Key Differences from REST

| Aspect | REST | gRPC |
|--------|------|------|
| **Contract** | OpenAPI (optional) | .proto file (required) |
| **Server** | `@RestController` | `@GrpcService` |
| **Client** | `RestTemplate`, `fetch()` | Generated stubs |
| **Data Format** | JSON (text) | Protobuf (binary) |
| **Type Safety** | Runtime | Compile-time |

---

## 💡 Summary

### member.proto
- **What**: API contract definition
- **Why**: Single source of truth, type safety, multi-language support
- **When**: Write this first before implementing server/client

### MemberGrpcService.java
- **What**: Server-side implementation
- **Why**: Handles incoming gRPC requests
- **When**: Runs in your Spring Boot app, called by external clients

### MemberGrpcClient.java
- **What**: Example client implementation
- **Why**: Shows how to call your gRPC service
- **When**: Use for testing or as reference for other services

---

## 🚀 Quick Commands

```bash
# 1. Generate code from proto
./gradlew generateProto

# 2. Start server (runs MemberGrpcService)
./gradlew bootRun

# 3. Run client (calls MemberGrpcService)
java -cp build/classes/java/main:build/libs/* \
  com.make.side.grpc.MemberGrpcClient

# 4. Test with grpcurl (another client)
grpcurl -plaintext -d '{"id": 1}' \
  localhost:9090 member.MemberService/GetMember
```

---

## 📚 Next Steps

1. **Understand**: You now know what each file does!
2. **Experiment**: Modify the proto file, regenerate, see what changes
3. **Implement**: Add the CreateMember functionality
4. **Extend**: Create a proto file for your Text service

Questions? Just ask! 🎉
