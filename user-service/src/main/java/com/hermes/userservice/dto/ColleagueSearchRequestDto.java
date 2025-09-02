package com.hermes.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColleagueSearchRequestDto {
    private String searchKeyword;
    private String department;
    private String position;
    private Integer page;
    private Integer size;
}
