package ru.misis.gamification.dto.admin.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionItemDto {

    private UUID id;
    private String userId;
    private String eventId;
    private Integer pointsEarned;
    private String description;           // если заполнено
    private LocalDateTime createdAt;
}