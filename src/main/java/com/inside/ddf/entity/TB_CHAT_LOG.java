package com.inside.ddf.entity;

import jakarta.persistence.*;
import javax.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "tb_chat_log",
    indexes = {
        @Index(name = "idx_chat_log_user", columnList = "user_id"),
        @Index(name = "idx_chat_log_dt",   columnList = "chat_dt")
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TB_CHAT_LOG {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id")
    private Integer chatId;

    // FK → TB_USER.USER_ID
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_chat_log_user")
    )
    @NotNull
    private TB_USER user;

    // 입력 텍스트
    @Column(name = "input_txt", columnDefinition = "text")
    private String inputTxt;

    // 출력 텍스트
    @Column(name = "output_txt", columnDefinition = "text")
    private String outputTxt;

    // 생성 시각 (DB now 또는 애플리케이션에서 채움)
    @CreationTimestamp
    @Column(name = "chat_dt", nullable = false)
    private LocalDateTime chatDt;
    
    @PrePersist
    protected void onCreate() {
       this.chatDt = LocalDateTime.now();
    }
    
}