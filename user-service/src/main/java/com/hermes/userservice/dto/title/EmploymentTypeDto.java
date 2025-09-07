package com.hermes.userservice.dto.title;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmploymentTypeDto {
    private Long id;
    private String name;
    private Integer sortOrder;
}
