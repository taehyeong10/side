package com.make.side.controller;

import com.make.side.dto.MemberCreateRequestDto;
import com.make.side.dto.MemberDto;
import com.make.side.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<MemberDto> getMemberById(@PathVariable Long id) {
        Optional<MemberDto> memberDtoOptional = memberService.findById(id);
        return memberDtoOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MemberDto> createMember(@RequestBody MemberCreateRequestDto request) {
        MemberDto created = memberService.create(request.getName());
        return ResponseEntity.ok(created);
    }

    // dirty checking 확인용: save() 없이 이름만 변경
    @PatchMapping("/{id}/name")
    public ResponseEntity<MemberDto> updateMemberName(@PathVariable Long id, @RequestBody MemberCreateRequestDto request) {
        MemberDto updated = memberService.updateName(id, request.getName());
        return ResponseEntity.ok(updated);
    }
}
