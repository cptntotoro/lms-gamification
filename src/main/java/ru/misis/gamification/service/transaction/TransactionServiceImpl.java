package ru.misis.gamification.service.transaction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.misis.gamification.entity.Transaction;
import ru.misis.gamification.exception.DuplicateEventException;
import ru.misis.gamification.repository.TransactionRepository;
import ru.misis.gamification.service.user.UserService;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class TransactionServiceImpl implements TransactionService {

    /**
     * Репозиторий транзакций
     */
    private final TransactionRepository transactionRepository;

    /**
     * Сервис управления пользователями
     */
    private final UserService userService;

    @Override
    public boolean isExistsByEventId(@NotBlank(message = "{event.id.required}") String eventId) {
        return transactionRepository.existsByEventId(eventId);
    }

    @Transactional
    @Override
    public Transaction saveIfNotExists(@NotNull(message = "{transaction.required}") Transaction transaction) {
        validateTransaction(transaction);

        String eventId = transaction.getEventId();

        log.debug("Сохранение транзакции: eventId={}, userUuid={}, points={}",
                eventId, transaction.getUser().getUuid(), transaction.getPoints());

        if (transactionRepository.existsByEventId(eventId)) {
            log.info("Дубликат события: eventId={}", eventId);
            throw new DuplicateEventException("Событие уже обработано: " + eventId);
        }

        try {
            Transaction saved = transactionRepository.save(transaction);
            log.info("Транзакция сохранена: id={}, eventId={}", saved.getUuid(), eventId);
            return saved;
        } catch (DataIntegrityViolationException e) {
            log.info("Дубликат при сохранении: eventId={}", eventId);
            throw new DuplicateEventException("Событие уже обработано: " + eventId);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Transaction> getTransactionsByUserId(@NotBlank(message = "{user.id.required}") String userId,
                                                     @NotNull(message = "{pageable.required}") Pageable pageable) {
        log.info("Запрос транзакций: userId={}, page={}, size={}, sort={}",
                userId, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        UUID userUuid = userService.getUserUuidByExternalId(userId);

        Page<Transaction> page = transactionRepository.findByUserUuidOrderByCreatedAtDesc(userUuid, pageable);

        log.debug("Результат: элементов={}, всего={}", page.getNumberOfElements(), page.getTotalElements());
        return page;
    }

    @Transactional(readOnly = true)
    @Override
    public long sumPointsByUserAndEventTypeAndDate(@NotNull(message = "{user.uuid.required}") UUID userUuid,
                                                   @NotNull(message = "{eventType.uuid.required}") UUID eventTypeUuid,
                                                   @NotNull(message = "{date.required}") LocalDate date) {
        return transactionRepository.sumPointsByUserUuidAndEventTypeUuidAndDate(
                userUuid, eventTypeUuid, date);
    }

    private void validateTransaction(Transaction t) {
        if (t == null) {
            throw new IllegalArgumentException("Транзакция не может быть null");
        }
        if (t.getEventId() == null || t.getEventId().isBlank()) {
            throw new IllegalArgumentException("eventId не может быть null или пустым");
        }
        if (t.getUser() == null || t.getUser().getUuid() == null) {
            throw new IllegalArgumentException("Пользователь или его UUID не может быть null");
        }
        if (t.getPoints() == null || t.getPoints() < 0) {
            throw new IllegalArgumentException("Количество очков не может быть null или отрицательным");
        }
    }
}