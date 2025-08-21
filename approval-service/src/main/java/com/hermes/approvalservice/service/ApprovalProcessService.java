package com.hermes.approvalservice.service;

import com.hermes.approvalservice.dto.request.ApprovalActionRequest;
import com.hermes.approvalservice.entity.ApprovalDocument;
import com.hermes.approvalservice.entity.DocumentApprovalStage;
import com.hermes.approvalservice.entity.DocumentApprovalTarget;
import com.hermes.approvalservice.enums.ActivityType;
import com.hermes.approvalservice.enums.DocumentStatus;
import com.hermes.approvalservice.exception.NotFoundException;
import com.hermes.approvalservice.exception.UnauthorizedException;
import com.hermes.approvalservice.repository.ApprovalDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ApprovalProcessService {

    private final ApprovalDocumentRepository documentRepository;
    private final DocumentPermissionService permissionService;
    private final DocumentActivityService activityService;

    public void approveDocument(Long documentId, Long userId, ApprovalActionRequest request) {
        ApprovalDocument document = documentRepository.findByIdWithDetails(documentId);
        if (document == null) {
            throw new NotFoundException("문서를 찾을 수 없습니다.");
        }

        if (!permissionService.canApproveDocument(document, userId, document.getCurrentStage())) {
            throw new UnauthorizedException("승인 권한이 없습니다.");
        }

        // 현재 단계의 승인 대상자 승인 처리
        DocumentApprovalStage currentStage = document.getApprovalStages().stream()
                .filter(stage -> stage.getStageOrder().equals(document.getCurrentStage()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("승인 단계를 찾을 수 없습니다."));

        // 해당 사용자의 승인 처리
        currentStage.getApprovalTargets().stream()
                .filter(target -> !target.getIsReference() && isTargetUser(target, userId))
                .forEach(target -> {
                    target.setIsApproved(true);
                    target.setApprovedBy(userId);
                    target.setApprovedAt(LocalDateTime.now());
                });

        // 현재 단계의 모든 승인이 완료되었는지 확인
        boolean allApproved = currentStage.getApprovalTargets().stream()
                .filter(target -> !target.getIsReference())
                .allMatch(DocumentApprovalTarget::getIsApproved);

        if (allApproved) {
            currentStage.setIsCompleted(true);
            currentStage.setCompletedAt(LocalDateTime.now());

            // 다음 단계로 진행 또는 최종 승인
            if (document.getCurrentStage() < document.getApprovalStages().size()) {
                document.setCurrentStage(document.getCurrentStage() + 1);
                activityService.recordActivity(document, userId, ActivityType.APPROVE, 
                    String.format("%d단계 승인 완료", currentStage.getStageOrder()), request.getReason());
            } else {
                document.setStatus(DocumentStatus.APPROVED);
                document.setApprovedAt(LocalDateTime.now());
                activityService.recordActivity(document, userId, ActivityType.APPROVE, 
                    "최종 승인 완료", request.getReason());
            }
        } else {
            activityService.recordActivity(document, userId, ActivityType.APPROVE, 
                String.format("%d단계 승인", currentStage.getStageOrder()), request.getReason());
        }
    }

    public void rejectDocument(Long documentId, Long userId, ApprovalActionRequest request) {
        ApprovalDocument document = documentRepository.findByIdWithDetails(documentId);
        if (document == null) {
            throw new NotFoundException("문서를 찾을 수 없습니다.");
        }

        if (!permissionService.canApproveDocument(document, userId, document.getCurrentStage())) {
            throw new UnauthorizedException("반려 권한이 없습니다.");
        }

        document.setStatus(DocumentStatus.REJECTED);
        activityService.recordActivity(document, userId, ActivityType.REJECT, "문서를 반려했습니다.", request.getReason());
    }

    private boolean isTargetUser(DocumentApprovalTarget target, Long userId) {
        if (target.getUserId() != null) {
            return target.getUserId().equals(userId);
        }
        
        // 조직 또는 n차 조직장 로직은 추후 구현
        return false;
    }
}