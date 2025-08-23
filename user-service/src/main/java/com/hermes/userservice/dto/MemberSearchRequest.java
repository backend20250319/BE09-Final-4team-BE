package com.hermes.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberSearchRequest {
    private String search;
    private String organization;
    private String position;
    private String rank;
    private Boolean isAdmin;
    private Integer page = 0;
    private Integer size = 20;
    private String sort = "name";
    private String direction = "asc";     
}
