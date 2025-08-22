package com.hermes.approvalservice.service;

import com.hermes.approvalservice.entity.ApprovalDocument;
import com.hermes.approvalservice.entity.DocumentApprovalTarget;
import com.hermes.jwt.util.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentPermissionService {

    public boolean canViewDocument(ApprovalDocument document, Long userId) {
        // 작성자는 항상 조회 가능
        if (document.getAuthorId().equals(userId)) {
            return true;
        }

        // 관리자는 항상 조회 가능
        if (AuthUtils.isAdmin()) {
            return true;
        }

        // 승인 대상자 또는 참조 대상자인 경우 조회 가능
        return document.getApprovalStages().stream()
        .flatMap(stage -> stage.getApprovalTargets().stream())
                .anyMatch(target -> isTargetUser(target, userId)) ||
               document.getReferenceTargets().stream()
                .anyMatch(target -> isTargetUser(target, userId));
    }

    public boolean canEditDocument(ApprovalDocument document, Long userId) {
        // 작성자만 수정 가능 (임시저장 상태일 때)
        return document.getAuthorId().equals(userId);
    }

    public boolean canApproveDocument(ApprovalDocument document, Long userId, Integer stageOrder) {
        // 해당 단계의 승인 대상자인지 확인
        return document.getApprovalStages().stream()
                .filter(stage -> stage.getStageOrder().equals(stageOrder))
                .flatMap(stage -> stage.getApprovalTargets().stream())
                .filter(target -> !target.getIsReference())
                .anyMatch(target -> isTargetUser(target, userId) && !target.getIsApproved());
    }

    private boolean isTargetUser(DocumentApprovalTarget target, Long userId) {
        if (target.getUserId() != null) {
            return target.getUserId().equals(userId);
        }
        
        // 조직 또는 n차 조직장 로직은 추후 구현
        // OrganizationService와 연동 필요
        
        return false;
    }
}