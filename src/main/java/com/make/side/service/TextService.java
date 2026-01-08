package com.make.side.service;

import com.make.side.dto.TextRequestDto;
import com.make.side.dto.TextResponseDto;
import com.make.side.entity.TextDocument;
import com.make.side.repository.TextRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TextService {

    private final TextRepository textRepository;

    public TextService(TextRepository textRepository) {
        this.textRepository = textRepository;
    }

    public TextResponseDto saveText(TextRequestDto request) {
        TextDocument document = new TextDocument(request.getMemberId(), request.getText());
        TextDocument saved = textRepository.save(document);
        return toResponseDto(saved);
    }

    public List<TextResponseDto> getTextsByMemberId(Long memberId) {
        List<TextDocument> documents = textRepository.findByMemberId(memberId);
        return documents.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    private TextResponseDto toResponseDto(TextDocument document) {
        return new TextResponseDto(
                document.getId(),
                document.getMemberId(),
                document.getText(),
                document.getCreatedAt()
        );
    }
}
