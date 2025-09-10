package com.marry1q.marry1qbe.domain.finance.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "finance_category")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FinanceCategory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "finance_category_id")
    private Long financeCategoryId;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "couple_id", nullable = false)
    private Long coupleId;
    
    @Column(name = "icon_name", length = 50)
    private String iconName;
    
    @Column(name = "color_name", length = 50)
    private String colorName;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
