package com.hermes.approvalservice.dto.request;

import com.hermes.approvalservice.validation.ValidBulkCategoryRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@ValidBulkCategoryRequest
@Schema(description = "카테고리 일괄 작업 요청",
        example = """
        {
          "operations": [
            {
              "type": "CREATE",
              "createRequest": {
                "name": "새 카테고리",
                "sortOrder": 1
              }
            },
            {
              "type": "UPDATE",
              "id": 10,
              "updateRequest": {
                "name": "수정된 카테고리",
                "sortOrder": 2
              }
            },
            {
              "type": "DELETE",
              "id": 15
            }
          ]
        }
        """)
public class BulkCategoryRequest {
    
    @NotEmpty(message = "작업 목록은 비어있을 수 없습니다")
    @Valid
    @Schema(description = "카테고리 작업 목록")
    private List<BulkCategoryOperation> operations;
}