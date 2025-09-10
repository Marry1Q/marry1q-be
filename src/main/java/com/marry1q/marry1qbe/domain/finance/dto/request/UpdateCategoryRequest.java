package com.marry1q.marry1qbe.domain.finance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateCategoryRequest {
    
    @NotBlank(message = "카테고리 이름은 필수입니다.")
    @Size(max = 100, message = "카테고리 이름은 100자를 초과할 수 없습니다.")
    private String name;
    
    @Size(max = 50, message = "아이콘 이름은 50자를 초과할 수 없습니다.")
    private String iconName;
    
    @Size(max = 50, message = "색상 이름은 50자를 초과할 수 없습니다.")
    private String colorName;
}
