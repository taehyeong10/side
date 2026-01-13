package com.make.side.grpc;

import com.make.side.grpc.generated.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;

/**
 * Standalone gRPC client for testing the MemberService
 * 
 * Usage:
 * 1. Start the application (./gradlew bootRun)
 * 2. Run this class as a Java application
 * 3. It will connect to the gRPC server and make test calls
 */
public class MemberGrpcClient {
    
    private final ManagedChannel channel;
    private final MemberServiceGrpc.MemberServiceBlockingStub blockingStub;
    
    public MemberGrpcClient(String host, int port) {
        // Create a channel
        this.channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext() // Disable TLS for local development
                .build();
        
        // Create a blocking stub
        this.blockingStub = MemberServiceGrpc.newBlockingStub(channel);
    }
    
    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
    
    /**
     * Get a member by ID
     */
    public void getMember(long id) {
        System.out.println("\n=== Getting Member with ID: " + id + " ===");
        
        GetMemberRequest request = GetMemberRequest.newBuilder()
                .setId(id)
                .build();
        
        try {
            GetMemberResponse response = blockingStub.getMember(request);
            
            if (response.getFound()) {
                System.out.println("✓ Member found!");
                System.out.println("  ID: " + response.getMember().getId());
                System.out.println("  Name: " + response.getMember().getName());
            } else {
                System.out.println("✗ Member not found");
            }
        } catch (StatusRuntimeException e) {
            System.err.println("✗ RPC failed: " + e.getStatus());
        }
    }
    
    /**
     * Try to create a member (currently unimplemented)
     */
    public void createMember(String name) {
        System.out.println("\n=== Creating Member with name: " + name + " ===");
        
        CreateMemberRequest request = CreateMemberRequest.newBuilder()
                .setName(name)
                .build();
        
        try {
            CreateMemberResponse response = blockingStub.createMember(request);
            System.out.println("✓ Member created!");
            System.out.println("  ID: " + response.getMember().getId());
            System.out.println("  Name: " + response.getMember().getName());
        } catch (StatusRuntimeException e) {
            System.err.println("✗ RPC failed: " + e.getStatus());
        }
    }
    
    /**
     * List all members (currently unimplemented)
     */
    public void listMembers() {
        System.out.println("\n=== Listing all Members ===");
        
        ListMembersRequest request = ListMembersRequest.newBuilder().build();
        
        try {
            ListMembersResponse response = blockingStub.listMembers(request);
            System.out.println("✓ Found " + response.getMembersCount() + " members:");
            for (Member member : response.getMembersList()) {
                System.out.println("  - ID: " + member.getId() + ", Name: " + member.getName());
            }
        } catch (StatusRuntimeException e) {
            System.err.println("✗ RPC failed: " + e.getStatus());
        }
    }
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   gRPC Client for Member Service      ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Connecting to gRPC server at localhost:9090...");
        
        MemberGrpcClient client = new MemberGrpcClient("localhost", 9090);
        
        try {
            // Test 1: Get existing member
            client.getMember(1);
            
            // Test 2: Get non-existing member
            client.getMember(999);
            
            // Test 3: Try to create a member (will fail - unimplemented)
            client.createMember("New User");
            
            // Test 4: Try to list members (will fail - unimplemented)
            client.listMembers();
            
            System.out.println("\n=== Tests completed ===");
            
        } finally {
            try {
                client.shutdown();
                System.out.println("\nConnection closed.");
            } catch (InterruptedException e) {
                System.err.println("Error shutting down: " + e.getMessage());
            }
        }
    }
}
