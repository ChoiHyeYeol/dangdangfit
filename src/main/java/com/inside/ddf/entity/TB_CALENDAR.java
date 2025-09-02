package com.inside.ddf.entity;

import jakarta.persistence.*;
import javax.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
    name = "tb_calendar",
    indexes = {
        @Index(name = "idx_calendar_user", columnList = "user_id"),
        @Index(name = "idx_calendar_hos_date", columnList = "hos_date")
    }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TB_CALENDAR {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "calendar_id")
    private Integer calendarId;

    // FK → TB_USER.USER_ID
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_calendar_user")
    )
    private TB_USER user;

    @NotNull
    @Column(name = "hos_date", nullable = false)
    private LocalDate hosDate; // 병원 예약/방문 일시
}
