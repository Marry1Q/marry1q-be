package com.marry1q.marry1qbe.grobal.commonCode.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "common_code")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonCode {
    @Id
    @Column(name = "code_id", length = 50)
    private String codeId;
    
    @Column(name = "code_name", length = 100, nullable = false)
    private String codeName;
    
    @Column(name = "code_value", length = 100, nullable = false)
    private String codeValue;
    
    @Column(name = "code_group", length = 50, nullable = false)
    private String codeGroup;
    
    @Column(name = "sort_order")
    private Integer sortOrder;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
