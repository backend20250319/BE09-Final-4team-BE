package com.hermes.userservice.service;

import com.hermes.userservice.dto.title.*;
import com.hermes.userservice.entity.Job;
import com.hermes.userservice.entity.Position;
import com.hermes.userservice.entity.Rank;
import com.hermes.userservice.exception.BusinessException;
import com.hermes.userservice.repository.JobRepository;
import com.hermes.userservice.repository.PositionRepository;
import com.hermes.userservice.repository.RankRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TitleService {

    private final RankRepository rankRepository;
    private final PositionRepository positionRepository;
    private final JobRepository jobRepository;

    public List<RankDto> getAllRanks() {
        log.info("모든 직급 조회 요청");
        return rankRepository.findAllByOrderBySortOrderAsc().stream()
                .map(this::convertToRankDto)
                .collect(Collectors.toList());
    }

    public RankDto getRankById(Long id) {
        log.info("직급 조회 요청: id={}", id);
        Rank rank = rankRepository.findById(id)
                .orElseThrow(() -> new BusinessException("직급을 찾을 수 없습니다. ID: " + id));
        return convertToRankDto(rank);
    }

    @Transactional
    public RankDto createRank(CreateRankRequest request) {
        log.info("직급 생성 요청: name={}, sortOrder={}", request.getName(), request.getSortOrder());
        
        if (rankRepository.existsByName(request.getName())) {
            throw new BusinessException("이미 존재하는 직급명입니다: " + request.getName());
        }

        Rank rank = Rank.builder()
                .name(request.getName())
                .sortOrder(request.getSortOrder())
                .build();

        Rank savedRank = rankRepository.save(rank);
        log.info("직급 생성 완료: id={}, name={}", savedRank.getId(), savedRank.getName());
        
        return convertToRankDto(savedRank);
    }

    @Transactional
    public RankDto updateRank(Long id, UpdateRankRequest request) {
        log.info("직급 수정 요청: id={}, name={}, sortOrder={}", id, request.getName(), request.getSortOrder());
        
        Rank rank = rankRepository.findById(id)
                .orElseThrow(() -> new BusinessException("직급을 찾을 수 없습니다. ID: " + id));

        if (!rank.getName().equals(request.getName()) && rankRepository.existsByName(request.getName())) {
            throw new BusinessException("이미 존재하는 직급명입니다: " + request.getName());
        }

        rank.setName(request.getName());
        rank.setSortOrder(request.getSortOrder());

        Rank updatedRank = rankRepository.save(rank);
        log.info("직급 수정 완료: id={}, name={}", updatedRank.getId(), updatedRank.getName());
        
        return convertToRankDto(updatedRank);
    }

    @Transactional
    public void deleteRank(Long id) {
        log.info("직급 삭제 요청: id={}", id);
        
        Rank rank = rankRepository.findById(id)
                .orElseThrow(() -> new BusinessException("직급을 찾을 수 없습니다. ID: " + id));

        rankRepository.delete(rank);
        log.info("직급 삭제 완료: id={}, name={}", rank.getId(), rank.getName());
    }

    public List<PositionDto> getAllPositions() {
        log.info("모든 직위 조회 요청");
        return positionRepository.findAllByOrderBySortOrderAsc().stream()
                .map(this::convertToPositionDto)
                .collect(Collectors.toList());
    }

    public PositionDto getPositionById(Long id) {
        log.info("직위 조회 요청: id={}", id);
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("직위를 찾을 수 없습니다. ID: " + id));
        return convertToPositionDto(position);
    }

    @Transactional
    public PositionDto createPosition(CreatePositionRequest request) {
        log.info("직위 생성 요청: name={}, sortOrder={}", request.getName(), request.getSortOrder());
        
        if (positionRepository.existsByName(request.getName())) {
            throw new BusinessException("이미 존재하는 직위명입니다: " + request.getName());
        }

        Position position = Position.builder()
                .name(request.getName())
                .sortOrder(request.getSortOrder())
                .build();

        Position savedPosition = positionRepository.save(position);
        log.info("직위 생성 완료: id={}, name={}", savedPosition.getId(), savedPosition.getName());
        
        return convertToPositionDto(savedPosition);
    }

    @Transactional
    public PositionDto updatePosition(Long id, UpdatePositionRequest request) {
        log.info("직위 수정 요청: id={}, name={}, sortOrder={}", id, request.getName(), request.getSortOrder());
        
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("직위를 찾을 수 없습니다. ID: " + id));

        if (!position.getName().equals(request.getName()) && positionRepository.existsByName(request.getName())) {
            throw new BusinessException("이미 존재하는 직위명입니다: " + request.getName());
        }

        position.setName(request.getName());
        position.setSortOrder(request.getSortOrder());

        Position updatedPosition = positionRepository.save(position);
        log.info("직위 수정 완료: id={}, name={}", updatedPosition.getId(), updatedPosition.getName());
        
        return convertToPositionDto(updatedPosition);
    }

    @Transactional
    public void deletePosition(Long id) {
        log.info("직위 삭제 요청: id={}", id);
        
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("직위를 찾을 수 없습니다. ID: " + id));

        positionRepository.delete(position);
        log.info("직위 삭제 완료: id={}, name={}", position.getId(), position.getName());
    }

    public List<JobDto> getAllJobs() {
        log.info("모든 직책 조회 요청");
        return jobRepository.findAllByOrderBySortOrderAsc().stream()
                .map(this::convertToJobDto)
                .collect(Collectors.toList());
    }

    public JobDto getJobById(Long id) {
        log.info("직책 조회 요청: id={}", id);
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new BusinessException("직책을 찾을 수 없습니다. ID: " + id));
        return convertToJobDto(job);
    }

    @Transactional
    public JobDto createJob(CreateJobRequest request) {
        log.info("직책 생성 요청: name={}, sortOrder={}", request.getName(), request.getSortOrder());
        
        if (jobRepository.existsByName(request.getName())) {
            throw new BusinessException("이미 존재하는 직책명입니다: " + request.getName());
        }

        Job job = Job.builder()
                .name(request.getName())
                .sortOrder(request.getSortOrder())
                .build();

        Job savedJob = jobRepository.save(job);
        log.info("직책 생성 완료: id={}, name={}", savedJob.getId(), savedJob.getName());
        
        return convertToJobDto(savedJob);
    }

    @Transactional
    public JobDto updateJob(Long id, UpdateJobRequest request) {
        log.info("직책 수정 요청: id={}, name={}, sortOrder={}", id, request.getName(), request.getSortOrder());
        
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new BusinessException("직책을 찾을 수 없습니다. ID: " + id));

        if (!job.getName().equals(request.getName()) && jobRepository.existsByName(request.getName())) {
            throw new BusinessException("이미 존재하는 직책명입니다: " + request.getName());
        }

        job.setName(request.getName());
        job.setSortOrder(request.getSortOrder());

        Job updatedJob = jobRepository.save(job);
        log.info("직책 수정 완료: id={}, name={}", updatedJob.getId(), updatedJob.getName());
        
        return convertToJobDto(updatedJob);
    }

    @Transactional
    public void deleteJob(Long id) {
        log.info("직책 삭제 요청: id={}", id);
        
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new BusinessException("직책을 찾을 수 없습니다. ID: " + id));

        jobRepository.delete(job);
        log.info("직책 삭제 완료: id={}, name={}", job.getId(), job.getName());
    }

    private RankDto convertToRankDto(Rank rank) {
        return RankDto.builder()
                .id(rank.getId())
                .name(rank.getName())
                .sortOrder(rank.getSortOrder())
                .build();
    }

    private PositionDto convertToPositionDto(Position position) {
        return PositionDto.builder()
                .id(position.getId())
                .name(position.getName())
                .sortOrder(position.getSortOrder())
                .build();
    }

    private JobDto convertToJobDto(Job job) {
        return JobDto.builder()
                .id(job.getId())
                .name(job.getName())
                .sortOrder(job.getSortOrder())
                .build();
    }
}
