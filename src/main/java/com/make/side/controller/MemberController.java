package com.make.side.controller;

import com.make.side.dto.MemberDto;
import com.make.side.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/member/{id}")
    public ResponseEntity<MemberDto> getMemberById(@PathVariable Long id) {
        Optional<MemberDto> memberDtoOptional = memberService.findById(id);
        return memberDtoOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());

    }
}
