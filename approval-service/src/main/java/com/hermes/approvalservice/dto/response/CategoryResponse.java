package com.hermes.approvalservice.dto.response;

import lombok.Data;


@Data
public class CategoryResponse {
    
    private Long id;
    private String name;
    private Integer sortOrder;
}