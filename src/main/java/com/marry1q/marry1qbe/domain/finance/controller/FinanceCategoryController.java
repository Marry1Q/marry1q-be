package com.marry1q.marry1qbe.domain.finance.controller;

import com.marry1q.marry1qbe.domain.finance.dto.request.CreateCategoryRequest;
import com.marry1q.marry1qbe.domain.finance.dto.request.UpdateCategoryRequest;
import com.marry1q.marry1qbe.domain.finance.dto.response.CategoryListResponse;
import com.marry1q.marry1qbe.domain.finance.dto.response.CategoryResponse;
import com.marry1q.marry1qbe.domain.finance.service.FinanceCategoryService;
import com.marry1q.marry1qbe.domain.couple.service.CoupleService;
import com.marry1q.marry1qbe.grobal.dto.CustomApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/finance/categories")
@RequiredArgsConstructor
@Tag(name = "Finance Category", description = "가계부 카테고리 관리 API")
@SecurityRequirement(name = "Bearer Authentication")
public class FinanceCategoryController {
    
    private final FinanceCategoryService financeCategoryService;
    private final CoupleService coupleService;
    
    @Operation(
        summary = "카테고리 목록 조회",
        description = "모든 카테고리를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "카테고리 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CategoryListResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답 예시",
                    value = """
                    {
                        "categories": [
                            {
                                "financeCategoryId": 1,
                                "name": "웨딩홀",
                                "coupleId": 1,
                                "iconName": "Heart",
                                "colorName": "red",
                                "createdAt": "2024-01-01T00:00:00",
                                "updatedAt": "2024-01-01T00:00:00"
                            },
                            {
                                "financeCategoryId": 2,
                                "name": "드레스",
                                "coupleId": 1,
                                "iconName": "ShoppingBag",
                                "colorName": "purple",
                                "createdAt": "2024-01-01T00:00:00",
                                "updatedAt": "2024-01-01T00:00:00"
                            },
                            {
                                "financeCategoryId": 11,
                                "name": "커스텀 카테고리",
                                "coupleId": 1,
                                "iconName": "Settings",
                                "colorName": "gray",
                                "createdAt": "2024-01-01T00:00:00",
                                "updatedAt": "2024-01-01T00:00:00"
                            }
                        ],
                        "totalCount": 3
                    }
                    """
                )
            )
        )
    })
    @GetMapping
    public CustomApiResponse<CategoryListResponse> getCategories(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        CategoryListResponse categories = financeCategoryService.getCategories(coupleId);
        
        return CustomApiResponse.success(categories, "카테고리 목록 조회가 완료되었습니다.");
    }
    
    @Operation(
        summary = "예산 미설정 카테고리 목록 조회",
        description = "예산이 설정되지 않은 카테고리만 조회합니다. (예산 설정 드롭다운용)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "예산 미설정 카테고리 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CategoryListResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답 예시",
                    value = """
                    {
                        "categories": [
                            {
                                "financeCategoryId": 3,
                                "name": "식비",
                                "coupleId": 1,
                                "iconName": "Utensils",
                                "colorName": "yellow",
                                "createdAt": "2024-01-01T00:00:00",
                                "updatedAt": "2024-01-01T00:00:00"
                            },
                            {
                                "financeCategoryId": 4,
                                "name": "교통비",
                                "coupleId": 1,
                                "iconName": "Car",
                                "colorName": "blue",
                                "createdAt": "2024-01-01T00:00:00",
                                "updatedAt": "2024-01-01T00:00:00"
                            }
                        ],
                        "totalCount": 2
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/unassigned-budgets")
    public CustomApiResponse<CategoryListResponse> getUnassignedBudgetCategories(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        CategoryListResponse categories = financeCategoryService.getUnassignedBudgetCategories(coupleId);
        
        return CustomApiResponse.success(categories, "예산 미설정 카테고리 목록 조회가 완료되었습니다.");
    }
    
    @Operation(
        summary = "카테고리 단건 조회",
        description = "특정 카테고리의 상세 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "카테고리 단건 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CategoryResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답 예시",
                    value = """
                    {
                        "financeCategoryId": 1,
                        "name": "웨딩홀",
                        "coupleId": 1,
                        "iconName": "Heart",
                        "colorName": "red",
                        "createdAt": "2024-01-01T00:00:00",
                        "updatedAt": "2024-01-01T00:00:00"
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/{categoryId}")
    public CustomApiResponse<CategoryResponse> getCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "카테고리 ID") @PathVariable Long categoryId) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        CategoryResponse category = financeCategoryService.getCategory(categoryId, coupleId);
        
        return CustomApiResponse.success(category, "카테고리 조회가 완료되었습니다.");
    }
    
    @Operation(
        summary = "카테고리 생성",
        description = "새로운 카테고리를 생성합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "카테고리 생성 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CategoryResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답 예시",
                    value = """
                    {
                        "financeCategoryId": 11,
                        "name": "새로운 카테고리",
                        "coupleId": 1,
                        "iconName": "Plus",
                        "colorName": "green",
                        "createdAt": "2024-01-01T00:00:00",
                        "updatedAt": "2024-01-01T00:00:00"
                    }
                    """
                )
            )
        )
    })
    @PostMapping
    public CustomApiResponse<CategoryResponse> createCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateCategoryRequest request) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        CategoryResponse category = financeCategoryService.createCategory(request, coupleId);
        
        return CustomApiResponse.success(category, "카테고리가 성공적으로 생성되었습니다.");
    }
    
    @Operation(
        summary = "카테고리 수정",
        description = "카테고리의 이름을 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "카테고리 수정 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CategoryResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답 예시",
                    value = """
                    {
                        "financeCategoryId": 11,
                        "name": "수정된 카테고리명",
                        "coupleId": 1,
                        "iconName": "Plus",
                        "colorName": "green",
                        "createdAt": "2024-01-01T00:00:00",
                        "updatedAt": "2024-01-15T10:00:00"
                    }
                    """
                )
            )
        )
    })
    @PutMapping("/{categoryId}")
    public CustomApiResponse<CategoryResponse> updateCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "카테고리 ID") @PathVariable Long categoryId,
            @Valid @RequestBody UpdateCategoryRequest request) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        CategoryResponse category = financeCategoryService.updateCategory(categoryId, request, coupleId);
        
        return CustomApiResponse.success(category, "카테고리가 성공적으로 수정되었습니다.");
    }
    
    @Operation(
        summary = "카테고리 삭제",
        description = "카테고리를 삭제합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "카테고리 삭제 성공"
        )
    })
    @DeleteMapping("/{categoryId}")
    public CustomApiResponse<Void> deleteCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "카테고리 ID") @PathVariable Long categoryId) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        financeCategoryService.deleteCategory(categoryId, coupleId);
        
        return CustomApiResponse.success(null, "카테고리가 성공적으로 삭제되었습니다.");
    }
}
