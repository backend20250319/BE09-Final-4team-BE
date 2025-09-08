package com.hermes.approvalservice.service;

import com.hermes.approvalservice.dto.response.MyApprovalInfo;
import com.hermes.approvalservice.entity.ApprovalDocument;
import com.hermes.approvalservice.entity.DocumentApprovalTarget;
import com.hermes.approvalservice.enums.ApprovalStatus;
import com.hermes.approvalservice.enums.DocumentStatus;
import com.hermes.approvalservice.enums.DocumentRole;
import com.hermes.auth.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentPermissionService {

    public boolean canViewDocument(ApprovalDocument document, UserPrincipal user) {
        Long userId = user.getId();
        // 작성자는 항상 조회 가능
        if (document.getAuthorId().equals(userId)) {
            return true;
        }

        // 관리자는 항상 조회 가능
        if (user.isAdmin()) {
            return true;
        }

        // 승인 대상자 또는 참조 대상자인 경우 조회 가능
        return document.getApprovalStages().stream()
        .flatMap(stage -> stage.getApprovalTargets().stream())
                .anyMatch(target -> isTargetUser(target, userId)) ||
               document.getReferenceTargets().stream()
                .anyMatch(target -> isTargetUser(target, userId));
    }

    public boolean canEditDocument(ApprovalDocument document, UserPrincipal user) {
        Long userId = user.getId();
        
        // 작성자만 수정 가능 (임시저장 상태일 때)
        return document.getAuthorId().equals(userId);
    }

    public boolean canApproveDocument(ApprovalDocument document, Integer stageOrder, UserPrincipal user) {
        Long userId = user.getId();
        
        // 해당 단계의 승인 대상자인지 확인
        return document.getApprovalStages().stream()
                .filter(stage -> stage.getStageOrder().equals(stageOrder))
                .flatMap(stage -> stage.getApprovalTargets().stream())
                .filter(target -> !target.getIsReference())
                .anyMatch(target -> isTargetUser(target, userId) && target.getApprovalStatus() == ApprovalStatus.PENDING);
    }

    public boolean canDeleteDocument(ApprovalDocument document, UserPrincipal user) {
        Long userId = user.getId();
        // 관리자는 항상 삭제 가능
        if (user.isAdmin()) {
            return true;
        }
        
        // 일반 사용자: 본인이 작성한 임시저장 상태인 문서만 삭제 가능
        return document.getAuthorId().equals(userId) && document.getStatus() == DocumentStatus.DRAFT;
    }

    public DocumentRole getMyRole(UserPrincipal user, ApprovalDocument document) {
        Long userId = user.getId();
        // 작성자인 경우
        if (document.getAuthorId().equals(userId)) {
            return DocumentRole.AUTHOR;
        }

        // 승인 대상자인지 확인
        boolean isApprover = document.getApprovalStages().stream()
                .flatMap(stage -> stage.getApprovalTargets().stream())
                .filter(target -> !target.getIsReference())
                .anyMatch(target -> isTargetUser(target, userId));

        if (isApprover) {
            return DocumentRole.APPROVER;
        }

        // 참조 대상자인지 확인
        boolean isReference = document.getApprovalStages().stream()
                .flatMap(stage -> stage.getApprovalTargets().stream())
                .filter(DocumentApprovalTarget::getIsReference)
                .anyMatch(target -> isTargetUser(target, userId)) ||
                document.getReferenceTargets().stream()
                        .anyMatch(target -> isTargetUser(target, userId));

        if (isReference) {
            return DocumentRole.REFERENCE;
        }

        // 그 외의 경우 (권한이 있어서 조회는 가능하지만 특별한 역할이 없는 경우)
        return DocumentRole.VIEWER;
    }

    public MyApprovalInfo getMyApprovalInfo(UserPrincipal user, ApprovalDocument document) {
        Long userId = user.getId();
        
        MyApprovalInfo approvalInfo = new MyApprovalInfo();
        
        // 현재 사용자의 승인 상태와 단계 정보 찾기
        for (var stage : document.getApprovalStages()) {
            for (var target : stage.getApprovalTargets()) {
                if (!target.getIsReference() && isTargetUser(target, userId)) {
                    approvalInfo.setMyApprovalStatus(target.getApprovalStatus());
                    approvalInfo.setMyApprovalStage(stage.getStageOrder());
                    
                    // 현재 단계에서 승인이 필요한지 확인
                    boolean isCurrentStage = stage.getStageOrder().equals(document.getCurrentStage());
                    boolean isPending = target.getApprovalStatus() == ApprovalStatus.PENDING;
                    approvalInfo.setIsApprovalRequired(isCurrentStage && isPending);
                    
                    return approvalInfo;
                }
            }
        }
        
        return approvalInfo;
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