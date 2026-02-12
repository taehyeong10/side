package com.make.side.service;

import com.make.side.dto.PermissionCheckDto;
import com.make.side.dto.TextRequestDto;
import com.make.side.dto.TextResponseDto;
import com.make.side.dto.TextWithPermissionDto;
import com.make.side.entity.TextDocument;
import com.make.side.entity.TextPermission;
import com.make.side.repository.TextRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class TextService {

    private final TextRepository textRepository;
    private final PermissionService permissionService;

    public TextService(
        TextRepository textRepository,
        PermissionService permissionService
    ) {
        this.textRepository = textRepository;
        this.permissionService = permissionService;
    }

    public TextResponseDto saveText(TextRequestDto request) {
        TextDocument document = new TextDocument(request.getMemberId(), request.getText());
        TextDocument saved = textRepository.save(document);
        return toResponseDto(saved);
    }

    public TextResponseDto getTextById(String textId, Long requestingMemberId) {
        TextDocument document = textRepository.findById(textId)
            .orElseThrow(() -> new IllegalArgumentException("Text not found: " + textId));

        // Check READ permission (creator always has access)
        boolean hasPermission = permissionService.hasPermission(
            textId,
            requestingMemberId,
            document.getMemberId(),
            TextPermission.OperationType.READ
        );

        if (!hasPermission) {
            throw new SecurityException("No permission to read this text");
        }

        return toResponseDto(document);
    }

    public List<TextResponseDto> getTextsByMemberId(Long memberId, Long requestingMemberId) {
        List<TextDocument> documents = textRepository.findByMemberId(memberId);

        // Filter by READ permission
        return documents.stream()
            .filter(doc -> permissionService.hasPermission(
                doc.getId(),
                requestingMemberId,
                doc.getMemberId(),
                TextPermission.OperationType.READ
            ))
            .map(this::toResponseDto)
            .collect(Collectors.toList());
    }

    public TextResponseDto updateText(String textId, String newText, Long requestingMemberId) {
        TextDocument document = textRepository.findById(textId)
            .orElseThrow(() -> new IllegalArgumentException("Text not found: " + textId));

        // Check EDIT permission (creator always has access)
        boolean hasPermission = permissionService.hasPermission(
            textId,
            requestingMemberId,
            document.getMemberId(),
            TextPermission.OperationType.EDIT
        );

        if (!hasPermission) {
            throw new SecurityException("No permission to edit this text");
        }

        document.setText(newText);
        TextDocument updated = textRepository.save(document);
        return toResponseDto(updated);
    }

    public void deleteText(String textId, Long requestingMemberId) {
        TextDocument document = textRepository.findById(textId)
            .orElseThrow(() -> new IllegalArgumentException("Text not found: " + textId));

        // Check DELETE permission (creator always has access)
        boolean hasPermission = permissionService.hasPermission(
            textId,
            requestingMemberId,
            document.getMemberId(),
            TextPermission.OperationType.DELETE
        );

        if (!hasPermission) {
            throw new SecurityException("No permission to delete this text");
        }

        textRepository.delete(document);
    }

    public List<TextWithPermissionDto> getReadableTexts(Long requestingMemberId) {
        // Fetch all texts from Elasticsearch
        List<TextDocument> allDocuments = StreamSupport
            .stream(textRepository.findAll().spliterator(), false)
            .collect(Collectors.toList());

        List<TextWithPermissionDto> result = new ArrayList<>();

        for (TextDocument doc : allDocuments) {
            boolean canRead = permissionService.hasPermission(
                doc.getId(), requestingMemberId, doc.getMemberId(),
                TextPermission.OperationType.READ
            );

            if (!canRead) {
                continue;
            }

            boolean canEdit = permissionService.hasPermission(
                doc.getId(), requestingMemberId, doc.getMemberId(),
                TextPermission.OperationType.EDIT
            );

            boolean canDelete = permissionService.hasPermission(
                doc.getId(), requestingMemberId, doc.getMemberId(),
                TextPermission.OperationType.DELETE
            );

            boolean isCreator = requestingMemberId.equals(doc.getMemberId());

            result.add(new TextWithPermissionDto(
                doc.getId(),
                doc.getMemberId(),
                doc.getText(),
                doc.getCreatedAt(),
                canEdit,
                canDelete,
                isCreator
            ));
        }

        return result;
    }

    public PermissionCheckDto checkPermissions(String textId, Long requestingMemberId) {
        TextDocument document = textRepository.findById(textId)
            .orElseThrow(() -> new IllegalArgumentException("Text not found: " + textId));

        return permissionService.getPermissions(textId, requestingMemberId, document.getMemberId());
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
