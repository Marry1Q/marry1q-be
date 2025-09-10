package com.marry1q.marry1qbe.domain.finance.controller;

import com.marry1q.marry1qbe.domain.finance.dto.response.BudgetOverviewResponse;
import com.marry1q.marry1qbe.domain.finance.service.CategoryBudgetService;
import com.marry1q.marry1qbe.domain.couple.service.CoupleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
@Tag(name = "Finance Overview", description = "가계부 대시보드 API")
@SecurityRequirement(name = "Bearer Authentication")
public class FinanceController {
    
    private final CategoryBudgetService categoryBudgetService;
    private final CoupleService coupleService;
    
    @Operation(
        summary = "예산 대시보드 정보 조회",
        description = "전체 예산 현황과 카테고리별 예산 정보를 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "예산 대시보드 정보 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BudgetOverviewResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답 예시",
                    value = """
                    {
                        "totalBudget": 50000000,
                        "totalSpent": 25000000,
                        "totalRemaining": 25000000,
                        "totalUsageRate": 50.0,
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
                                "usageRate": 50.0
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
                                "usageRate": 50.0
                            },
                            {
                                "categoryBudgetId": 3,
                                "categoryId": 3,
                                "categoryName": "스튜디오",
                                "iconName": "Camera",
                                "colorName": "blue",
                                "budgetAmount": 2000000,
                                "spentAmount": 1000000,
                                "remainingAmount": 1000000,
                                "usageRate": 50.0
                            }
                        ]
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/overview")
    public ResponseEntity<BudgetOverviewResponse> getBudgetOverview(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String currentUserSeqNo = userDetails.getUsername();
        Long coupleId = coupleService.getCurrentCoupleId();
        
        BudgetOverviewResponse overview = categoryBudgetService.getBudgetOverview(coupleId);
        
        return ResponseEntity.ok(overview);
    }
}
