package ru.misis.gamification.dto.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO информации о группе с признаком членства пользователя")
public class GroupWithMembershipDto {
    @Schema(description = "Идентификатор группы из LMS", example = "M-21-2")
    private String groupId;

    @Schema(description = "Отображаемое название группы")
    private String displayName;

    @Schema(description = "Состоит ли пользователь в этой группе")
    private boolean member;
}