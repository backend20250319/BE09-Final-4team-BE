package com.hermes.communicationservice.archive.service;

import com.hermes.communicationservice.archive.dto.ArchiveCreateRequestDto;
import com.hermes.communicationservice.archive.dto.ArchiveResponseDto;
import com.hermes.communicationservice.archive.dto.ArchiveSummaryDto;
import com.hermes.communicationservice.archive.dto.ArchiveUpdateRequestDto;
import com.hermes.communicationservice.archive.entity.Archive;
import com.hermes.communicationservice.archive.repository.ArchiveRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ArchiveService {

  private final ArchiveRepository archiveRepository;

  @Transactional
  public ArchiveResponseDto createArchive(ArchiveCreateRequestDto request, Long authorId) {
    log.info("사내 문서 생성 - title: {}, authorId: {}", request.getTitle(), authorId);
    
    Archive archive = Archive.builder()
        .title(request.getTitle())
        .description(request.getDescription())
        .authorId(authorId)
        .fileIds(request.getFileIds())
        .build();

    Archive savedArchive = archiveRepository.save(archive);
    log.info("사내 문서 생성 완료 - id: {}", savedArchive.getId());

    return convertToResponseDto(savedArchive);
  }

  public List<ArchiveSummaryDto> getAllArchivesSummary() {
    log.info("사내 문서 목록 조회");
    
    return archiveRepository.findAll().stream()
        .map(this::convertToSummaryDto)
        .toList();
  }

  public ArchiveResponseDto getArchive(Long id) {
    log.info("사내 문서 상세 조회 - id: {}", id);
    
    Archive archive = archiveRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문서입니다: " + id));

    return convertToResponseDto(archive);
  }

  @Transactional
  public ArchiveResponseDto updateArchive(ArchiveUpdateRequestDto request, Long id, Long authorId) {
    log.info("사내 문서 수정 - id: {}, authorId: {}", id, authorId);
    
    Archive archive = archiveRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문서입니다: " + id));

    if (request.getTitle() != null) {
      archive.setTitle(request.getTitle());
    }
    if (request.getDescription() != null) {
      archive.setDescription(request.getDescription());
    }
    if (request.getFileIds() != null) {
      archive.setFileIds(request.getFileIds());
    }

    Archive updatedArchive = archiveRepository.save(archive);
    log.info("사내 문서 수정 완료 - id: {}", updatedArchive.getId());

    return convertToResponseDto(updatedArchive);
  }

  @Transactional
  public void deleteArchive(Long id) {
    log.info("사내 문서 삭제 - id: {}", id);
    
    Archive archive = archiveRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문서입니다: " + id));

    archiveRepository.delete(archive);
    log.info("사내 문서 삭제 완료 - id: {}", id);
  }

  public List<ArchiveSummaryDto> searchArchives(String keyword) {
    log.info("사내 문서 검색 - keyword: {}", keyword);
    
    return archiveRepository.findByTitleContaining(keyword).stream()
        .map(this::convertToSummaryDto)
        .toList();
  }

  private ArchiveResponseDto convertToResponseDto(Archive archive) {
    return ArchiveResponseDto.builder()
        .id(archive.getId())
        .title(archive.getTitle())
        .authorId(archive.getAuthorId())
        .description(archive.getDescription())
        .fileIds(archive.getFileIds())
        .createdAt(archive.getCreatedAt())
        .build();
  }

  private ArchiveSummaryDto convertToSummaryDto(Archive archive) {
    return ArchiveSummaryDto.builder()
        .id(archive.getId())
        .title(archive.getTitle())
        .build();
  }

}
