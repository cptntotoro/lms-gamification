package ru.misis.gamification.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.misis.gamification.dto.admin.response.TransactionItemDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionPageDto {

    private List<TransactionItemDto> content;
    private int pageNumber;         // номер страницы (0-based)
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;
}