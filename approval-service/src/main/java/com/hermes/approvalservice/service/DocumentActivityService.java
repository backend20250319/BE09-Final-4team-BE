package com.hermes.approvalservice.service;

import com.hermes.approvalservice.dto.response.DocumentActivityResponse;
import com.hermes.approvalservice.entity.ApprovalDocument;
import com.hermes.approvalservice.entity.DocumentActivity;
import com.hermes.approvalservice.enums.ActivityType;
import com.hermes.approvalservice.repository.DocumentActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentActivityService {

    private final DocumentActivityRepository activityRepository;

    public List<DocumentActivityResponse> getActivities(Long documentId) {
        return activityRepository.findByDocumentIdOrderByCreatedAtAsc(documentId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional
    public void recordActivity(ApprovalDocument document, Long userId, ActivityType activityType, String description) {
        recordActivity(document, userId, activityType, description, null);
    }

    @Transactional
    public void recordActivity(ApprovalDocument document, Long userId, ActivityType activityType, String description, String reason) {
        DocumentActivity activity = DocumentActivity.builder()
                .activityType(activityType)
                .userId(userId)
                .description(description)
                .reason(reason)
                .document(document)
                .build();

        activityRepository.save(activity);
    }

    private DocumentActivityResponse convertToResponse(DocumentActivity activity) {
        DocumentActivityResponse response = new DocumentActivityResponse();
        response.setId(activity.getId());
        response.setActivityType(activity.getActivityType());
        response.setUserId(activity.getUserId());
        response.setDescription(activity.getDescription());
        response.setReason(activity.getReason());
        response.setCreatedAt(activity.getCreatedAt());
        return response;
    }
}