package com.hermes.tenantservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 페이징된 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "페이징된 응답")
public class PagedResponse<T> {

    @Schema(description = "데이터 목록")
    @JsonProperty("content")
    private List<T> content;

    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    @JsonProperty("page")
    private int page;

    @Schema(description = "페이지 크기", example = "20")
    @JsonProperty("size")
    private int size;

    @Schema(description = "총 요소 개수", example = "100")
    @JsonProperty("totalElements")
    private long totalElements;

    @Schema(description = "총 페이지 수", example = "5")
    @JsonProperty("totalPages")
    private int totalPages;

    @Schema(description = "첫 번째 페이지 여부", example = "true")
    @JsonProperty("first")
    private boolean first;

    @Schema(description = "마지막 페이지 여부", example = "false")
    @JsonProperty("last")
    private boolean last;

    /**
     * Spring Data Page 객체로부터 PagedResponse 생성
     */
    public static <T> PagedResponse<T> from(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
