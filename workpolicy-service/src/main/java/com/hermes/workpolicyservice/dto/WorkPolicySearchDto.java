package com.hermes.workpolicyservice.dto;

import com.hermes.workpolicyservice.entity.WorkType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkPolicySearchDto {
    
    private String name; // 근무 정책 이름으로 검색
    private WorkType type; // 근무 유형으로 필터링
    private Boolean isCompliantWithLaborLaw; // 노동법 준수 여부로 필터링
    
    // 페이징 정보
    @Builder.Default
    private Integer page = 0;
    @Builder.Default
    private Integer size = 10;
    @Builder.Default
    private String sortBy = "createdAt";
    @Builder.Default
    private String sortDirection = "DESC";
} 