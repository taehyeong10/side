package com.make.side.controller;

import com.make.side.dto.TextRequestDto;
import com.make.side.dto.TextResponseDto;
import com.make.side.service.TextService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TextController {

    private final TextService textService;

    public TextController(TextService textService) {
        this.textService = textService;
    }

    @PostMapping("/text")
    public ResponseEntity<TextResponseDto> saveText(@RequestBody TextRequestDto request) {
        TextResponseDto response = textService.saveText(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/member/{id}/text/")
    public ResponseEntity<List<TextResponseDto>> getTextsByMemberId(@PathVariable Long id) {
        List<TextResponseDto> texts = textService.getTextsByMemberId(id);
        return ResponseEntity.ok(texts);
    }
}
