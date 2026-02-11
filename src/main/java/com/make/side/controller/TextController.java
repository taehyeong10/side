package com.make.side.controller;

import com.make.side.dto.PermissionCheckDto;
import com.make.side.dto.TextRequestDto;
import com.make.side.dto.TextResponseDto;
import com.make.side.security.AuthenticationHelper;
import com.make.side.service.TextService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/texts")
public class TextController {

    private final TextService textService;
    private final AuthenticationHelper authHelper;

    public TextController(TextService textService, AuthenticationHelper authHelper) {
        this.textService = textService;
        this.authHelper = authHelper;
    }

    @PostMapping
    public ResponseEntity<TextResponseDto> saveText(@RequestBody TextRequestDto request) {
        // Use authenticated member as creator
        Long currentMemberId = authHelper.getCurrentMemberId();
        request.setMemberId(currentMemberId);

        TextResponseDto response = textService.saveText(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TextResponseDto> getTextById(@PathVariable String id) {
        Long currentMemberId = authHelper.getCurrentMemberId();
        TextResponseDto response = textService.getTextById(id, currentMemberId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TextResponseDto> updateText(
        @PathVariable String id,
        @RequestBody TextRequestDto request
    ) {
        Long currentMemberId = authHelper.getCurrentMemberId();
        TextResponseDto response = textService.updateText(id, request.getText(), currentMemberId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteText(@PathVariable String id) {
        Long currentMemberId = authHelper.getCurrentMemberId();
        textService.deleteText(id, currentMemberId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<TextResponseDto>> getTextsByMemberId(@PathVariable Long memberId) {
        Long currentMemberId = authHelper.getCurrentMemberId();
        List<TextResponseDto> texts = textService.getTextsByMemberId(memberId, currentMemberId);
        return ResponseEntity.ok(texts);
    }

    @GetMapping("/{id}/permissions")
    public ResponseEntity<PermissionCheckDto> checkPermissions(@PathVariable String id) {
        Long currentMemberId = authHelper.getCurrentMemberId();
        PermissionCheckDto permissions = textService.checkPermissions(id, currentMemberId);
        return ResponseEntity.ok(permissions);
    }
}

