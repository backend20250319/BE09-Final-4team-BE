package com.hermes.communicationservice.archive.service;

import com.hermes.communicationservice.archive.dto.ArchiveCreateRequestDto;
import com.hermes.communicationservice.archive.dto.ArchiveCreateResponseDto;
import com.hermes.communicationservice.archive.dto.ArchiveResponseDto;
import com.hermes.communicationservice.archive.dto.ArchiveUpdateRequestDto;
import com.hermes.communicationservice.archive.entity.Archive;
import com.hermes.communicationservice.archive.exception.ArchiveNotFoundException;
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
  public ArchiveCreateResponseDto createArchive(ArchiveCreateRequestDto request, Long authorId) {
    log.info("사내 문서 생성 - title: {}, authorId: {}", request.getTitle(), authorId);

    Archive archive = Archive.builder()
        .title(request.getTitle())
        .description(request.getDescription())
        .authorId(authorId)
        .fileIds(request.getFileIds())
        .build();

    Archive savedArchive = archiveRepository.save(archive);
    log.info("사내 문서 생성 완료 - id: {}", savedArchive.getId());

    return convertToCreateResponse(savedArchive);
  }

  public List<ArchiveResponseDto> getAllArchives() {
    log.info("사내 문서 목록 조회");

    return archiveRepository.findAllOrderByCreatedAtDesc().stream()
        .map(this::convertToResponse)
        .toList();
  }

  public ArchiveResponseDto getArchive(Long id) {
    log.info("사내 문서 상세 조회 - id: {}", id);

    Archive archive = archiveRepository.findById(id)
        .orElseThrow(() -> new ArchiveNotFoundException(id));

    return convertToResponse(archive);
  }

  @Transactional
  public ArchiveResponseDto updateArchive(ArchiveUpdateRequestDto request, Long id, Long authorId) {
    log.info("사내 문서 수정 - id: {}, authorId: {}", id, authorId);

    Archive archive = archiveRepository.findById(id)
        .orElseThrow(() -> new ArchiveNotFoundException(id));

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

    return convertToResponse(updatedArchive);
  }

  @Transactional
  public void deleteArchive(Long id) {
    log.info("사내 문서 삭제 - id: {}", id);

    Archive archive = archiveRepository.findById(id)
        .orElseThrow(() -> new ArchiveNotFoundException(id));

    archiveRepository.delete(archive);
    log.info("사내 문서 삭제 완료 - id: {}", id);
  }

  public List<ArchiveResponseDto> searchArchives(String keyword) {
    log.info("사내 문서 검색 - keyword: {}", keyword);

    return archiveRepository.findByTitleContaining(keyword).stream()
        .map(this::convertToResponse)
        .toList();
  }

  private ArchiveCreateResponseDto convertToCreateResponse(Archive archive) {
    return ArchiveCreateResponseDto.builder()
        .id(archive.getId())
        .title(archive.getTitle())
        .authorId(archive.getAuthorId())
        .description(archive.getDescription())
        .fileIds(archive.getFileIds())
        .createdAt(archive.getCreatedAt())
        .build();
  }

  private ArchiveResponseDto convertToResponse(Archive archive) {
    return ArchiveResponseDto.builder()
        .id(archive.getId())
        .title(archive.getTitle())
        .description(archive.getDescription())
        .fileIds(archive.getFileIds())
        .build();
  }

}
