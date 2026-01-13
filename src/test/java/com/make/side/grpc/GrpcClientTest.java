package com.make.side.grpc;

import com.make.side.grpc.generated.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Example gRPC client test
 * 
 * NOTE: This test is disabled by default because it requires the server to be running.
 * To run this test:
 * 1. Start the application: ./gradlew bootRun
 * 2. Remove the @Disabled annotation
 * 3. Run the test
 */
@Disabled("Requires running server - enable manually for integration testing")
public class GrpcClientTest {
    
    private ManagedChannel channel;
    private MemberServiceGrpc.MemberServiceBlockingStub blockingStub;
    private MemberServiceGrpc.MemberServiceStub asyncStub;
    
    @BeforeEach
    public void setUp() {
        // Create a channel to the gRPC server
        channel = ManagedChannelBuilder
                .forAddress("localhost", 9090)
                .usePlaintext() // Disable TLS for local testing
                .build();
        
        // Create stubs
        blockingStub = MemberServiceGrpc.newBlockingStub(channel);
        asyncStub = MemberServiceGrpc.newStub(channel);
    }
    
    @AfterEach
    public void tearDown() throws InterruptedException {
        // Shutdown the channel
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
    
    @Test
    public void testGetMember_Found() {
        // Arrange
        GetMemberRequest request = GetMemberRequest.newBuilder()
                .setId(1L)
                .build();
        
        // Act
        GetMemberResponse response = blockingStub.getMember(request);
        
        // Assert
        assertTrue(response.getFound(), "Member should be found");
        assertNotNull(response.getMember(), "Member should not be null");
        assertEquals(1L, response.getMember().getId(), "Member ID should match");
        assertFalse(response.getMember().getName().isEmpty(), "Member name should not be empty");
        
        System.out.println("Found member: " + response.getMember().getName());
    }
    
    @Test
    public void testGetMember_NotFound() {
        // Arrange
        GetMemberRequest request = GetMemberRequest.newBuilder()
                .setId(999999L)
                .build();
        
        // Act
        GetMemberResponse response = blockingStub.getMember(request);
        
        // Assert
        assertFalse(response.getFound(), "Member should not be found");
    }
    
    @Test
    public void testGetMember_MultipleRequests() {
        // Test that we can make multiple requests on the same channel
        for (long id = 1; id <= 3; id++) {
            GetMemberRequest request = GetMemberRequest.newBuilder()
                    .setId(id)
                    .build();
            
            GetMemberResponse response = blockingStub.getMember(request);
            System.out.println("ID " + id + " - Found: " + response.getFound());
        }
    }
    
    @Test
    public void testCreateMember_Unimplemented() {
        // Arrange
        CreateMemberRequest request = CreateMemberRequest.newBuilder()
                .setName("Test User")
                .build();
        
        // Act & Assert
        StatusRuntimeException exception = assertThrows(
                StatusRuntimeException.class,
                () -> blockingStub.createMember(request)
        );
        
        assertTrue(exception.getMessage().contains("UNIMPLEMENTED"));
        System.out.println("Expected error: " + exception.getMessage());
    }
    
    /**
     * Example of async (non-blocking) call
     */
    @Test
    public void testGetMember_Async() throws InterruptedException {
        // Arrange
        GetMemberRequest request = GetMemberRequest.newBuilder()
                .setId(1L)
                .build();
        
        // Use a simple flag to track completion
        final boolean[] completed = {false};
        
        // Act - make async call
        asyncStub.getMember(request, new io.grpc.stub.StreamObserver<GetMemberResponse>() {
            @Override
            public void onNext(GetMemberResponse response) {
                System.out.println("Async response received: " + response.getFound());
                if (response.getFound()) {
                    System.out.println("Member: " + response.getMember().getName());
                }
            }
            
            @Override
            public void onError(Throwable t) {
                System.err.println("Error: " + t.getMessage());
                completed[0] = true;
            }
            
            @Override
            public void onCompleted() {
                System.out.println("Async call completed");
                completed[0] = true;
            }
        });
        
        // Wait for completion
        int maxWait = 5000; // 5 seconds
        int waited = 0;
        while (!completed[0] && waited < maxWait) {
            Thread.sleep(100);
            waited += 100;
        }
        
        assertTrue(completed[0], "Async call should complete");
    }
}
