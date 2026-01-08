package com.make.side.repository;

import com.make.side.entity.TextDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface TextRepository extends ElasticsearchRepository<TextDocument, String> {
    List<TextDocument> findByMemberId(Long memberId);
}
