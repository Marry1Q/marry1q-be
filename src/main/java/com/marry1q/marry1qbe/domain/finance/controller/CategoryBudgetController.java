package com.marry1q.marry1qbe.domain.finance.controller;

import com.marry1q.marry1qbe.domain.finance.dto.request.CreateCategoryBudgetRequest;
import com.marry1q.marry1qbe.domain.finance.dto.request.UpdateCategoryBudgetRequest;
import com.marry1q.marry1qbe.domain.finance.dto.response.BudgetOverviewResponse;
import com.marry1q.marry1qbe.domain.finance.dto.response.CategoryBudgetListResponse;
import com.marry1q.marry1qbe.domain.finance.dto.response.CategoryBudgetResponse;
import com.marry1q.marry1qbe.domain.finance.service.CategoryBudgetService;
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
@RequestMapping("/api/finance/category-budgets")
@RequiredArgsConstructor
@Tag(name = "Finance Category Budget", description = "가계부 카테고리별 예산 관리 API")
@SecurityRequirement(name = "Bearer Authentication")
public class CategoryBudgetController {
    
    private final CategoryBudgetService categoryBudgetService;
    private final CoupleService coupleService;
    
    @Operation(
        summary = "카테고리별 예산 목록 조회",
        description = "모든 카테고리별 예산 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "카테고리별 예산 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CategoryBudgetListResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답 예시",
                    value = """
                    {
                        "categoryBudgets": [
                            {
                                "categoryBudgetId": 1,
                                "categoryId": 1,
                                "categoryName": "웨딩홀",
                                "iconName": "Heart",
                                "colorName": "red",
                                "budgetAmount": 10000000,
                                "spentAmount": 5000000,
                                "remainingAmount": 5000000,
                                "usageRate": 50.0,
                                "createdAt": "2024-01-01T00:00:00",
                                "updatedAt": "2024-01-01T00:00:00"
                            },
                            {
                                "categoryBudgetId": 2,
                                "categoryId": 2,
                                "categoryName": "드레스",
                                "iconName": "ShoppingBag",
                                "colorName": "purple",
                                "budgetAmount": 3000000,
                                "spentAmount": 1500000,
                                "remainingAmount": 1500000,
                                "usageRate": 50.0,
                                "createdAt": "2024-01-01T00:00:00",
                                "updatedAt": "2024-01-01T00:00:00"
                            }
                        ]
                    }
                    """
                )
            )
        )
    })
    @GetMapping
    public ResponseEntity<CategoryBudgetListResponse> getCategoryBudgets(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        CategoryBudgetListResponse categoryBudgets = categoryBudgetService.getCategoryBudgets(coupleId);
        
        return ResponseEntity.ok(categoryBudgets);
    }
    
    @Operation(
        summary = "카테고리별 예산 단건 조회",
        description = "특정 카테고리의 예산 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "카테고리별 예산 단건 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CategoryBudgetResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답 예시",
                    value = """
                    {
                        "categoryBudgetId": 1,
                        "categoryId": 1,
                        "categoryName": "웨딩홀",
                        "iconName": "Heart",
                        "colorName": "red",
                        "budgetAmount": 10000000,
                        "spentAmount": 5000000,
                        "remainingAmount": 5000000,
                        "usageRate": 50.0,
                        "createdAt": "2024-01-01T00:00:00",
                        "updatedAt": "2024-01-01T00:00:00"
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/{categoryBudgetId}")
    public ResponseEntity<CategoryBudgetResponse> getCategoryBudget(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "카테고리별 예산 ID") @PathVariable Long categoryBudgetId) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        CategoryBudgetResponse categoryBudget = categoryBudgetService.getCategoryBudget(categoryBudgetId, coupleId);
        
        return ResponseEntity.ok(categoryBudget);
    }
    
    @Operation(
        summary = "카테고리별 예산 생성",
        description = "기존 카테고리에 예산을 설정합니다. (카테고리 생성과는 별도로 예산만 설정)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "카테고리별 예산 생성 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CategoryBudgetResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답 예시",
                    value = """
                    {
                        "categoryBudgetId": 1,
                        "categoryId": 1,
                        "categoryName": "웨딩홀",
                        "iconName": "Heart",
                        "colorName": "red",
                        "budgetAmount": 10000000,
                        "spentAmount": 0,
                        "remainingAmount": 10000000,
                        "usageRate": 0.0,
                        "createdAt": "2024-01-01T00:00:00",
                        "updatedAt": "2024-01-01T00:00:00"
                    }
                    """
                )
            )
        )
    })
    @PostMapping
    public ResponseEntity<CategoryBudgetResponse> createCategoryBudget(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateCategoryBudgetRequest request) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        CategoryBudgetResponse categoryBudget = categoryBudgetService.createCategoryBudget(request, coupleId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryBudget);
    }
    
    @Operation(
        summary = "카테고리별 예산 수정",
        description = "기존 카테고리별 예산을 수정합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "카테고리별 예산 수정 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CategoryBudgetResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답 예시",
                    value = """
                    {
                        "categoryBudgetId": 1,
                        "categoryId": 1,
                        "categoryName": "웨딩홀",
                        "iconName": "Heart",
                        "colorName": "red",
                        "budgetAmount": 12000000,
                        "spentAmount": 5000000,
                        "remainingAmount": 7000000,
                        "usageRate": 41.67,
                        "createdAt": "2024-01-01T00:00:00",
                        "updatedAt": "2024-01-15T10:00:00"
                    }
                    """
                )
            )
        )
    })
    @PutMapping("/{categoryBudgetId}")
    public ResponseEntity<CategoryBudgetResponse> updateCategoryBudget(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "카테고리별 예산 ID") @PathVariable Long categoryBudgetId,
            @Valid @RequestBody UpdateCategoryBudgetRequest request) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        CategoryBudgetResponse categoryBudget = categoryBudgetService.updateCategoryBudget(categoryBudgetId, request, coupleId);
        
        return ResponseEntity.ok(categoryBudget);
    }
    
    @Operation(
        summary = "카테고리별 예산 삭제",
        description = "카테고리별 예산을 삭제합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "카테고리별 예산 삭제 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CustomApiResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답 예시",
                    value = """
                    {
                        "success": true,
                        "data": null,
                        "message": "카테고리별 예산이 성공적으로 삭제되었습니다.",
                        "timestamp": "2024-01-15T10:30:00"
                    }
                    """
                )
            )
        )
    })
    @DeleteMapping("/{categoryBudgetId}")
    public ResponseEntity<CustomApiResponse<Void>> deleteCategoryBudget(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "카테고리별 예산 ID") @PathVariable Long categoryBudgetId) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        categoryBudgetService.deleteCategoryBudget(categoryBudgetId, coupleId);
        
        return ResponseEntity.ok(CustomApiResponse.success(null, "카테고리별 예산이 성공적으로 삭제되었습니다."));
    }
}
