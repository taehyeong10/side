package com.make.side.grpc;

import com.make.side.dto.MemberDto;
import com.make.side.grpc.generated.*;
import com.make.side.service.MemberService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * gRPC service implementation for Member operations
 * This runs alongside the REST API endpoints
 */
@GrpcService
public class MemberGrpcService extends MemberServiceGrpc.MemberServiceImplBase {
    
    private static final Logger log = LoggerFactory.getLogger(MemberGrpcService.class);
    
    private final MemberService memberService;
    
    public MemberGrpcService(MemberService memberService) {
        this.memberService = memberService;
    }
    
    @Override
    public void getMember(GetMemberRequest request, StreamObserver<GetMemberResponse> responseObserver) {
        log.info("gRPC GetMember called with id: {}", request.getId());
        
        try {
            Optional<MemberDto> memberDto = memberService.findById(request.getId());
            
            GetMemberResponse.Builder responseBuilder = GetMemberResponse.newBuilder();
            
            if (memberDto.isPresent()) {
                MemberDto dto = memberDto.get();
                Member grpcMember = Member.newBuilder()
                        .setId(dto.id())
                        .setName(dto.name())
                        .build();
                
                responseBuilder.setMember(grpcMember).setFound(true);
            } else {
                responseBuilder.setFound(false);
            }
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Error in getMember", e);
            responseObserver.onError(e);
        }
    }
    
    @Override
    public void createMember(CreateMemberRequest request, StreamObserver<CreateMemberResponse> responseObserver) {
        log.info("gRPC CreateMember called with name: {}", request.getName());
        
        // TODO: Implement member creation in MemberService
        // For now, returning a placeholder response
        responseObserver.onError(
            io.grpc.Status.UNIMPLEMENTED
                .withDescription("CreateMember not yet implemented")
                .asRuntimeException()
        );
    }
    
    @Override
    public void listMembers(ListMembersRequest request, StreamObserver<ListMembersResponse> responseObserver) {
        log.info("gRPC ListMembers called");
        
        try {
            java.util.List<MemberDto> members = memberService.findAll();
            
            ListMembersResponse.Builder responseBuilder = ListMembersResponse.newBuilder();
            
            for (MemberDto dto : members) {
                Member grpcMember = Member.newBuilder()
                        .setId(dto.id())
                        .setName(dto.name())
                        .build();
                responseBuilder.addMembers(grpcMember);
            }
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            
            log.info("Returned {} members", members.size());
            
        } catch (Exception e) {
            log.error("Error in listMembers", e);
            responseObserver.onError(e);
        }
    }
}
