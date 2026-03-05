package ru.misis.gamification.service.simple.transaction;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.misis.gamification.entity.Transaction;
import ru.misis.gamification.exception.DuplicateEventException;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Сервис управления транзакциями
 */
public interface TransactionService {

    /**
     * Проверить, было ли событие с указанным идентификатором уже обработано
     *
     * @param eventId Идентификатор события из LMS
     * @return Да / Нет
     * @throws DuplicateEventException         если событие с таким eventId уже обработано
     * @throws DataIntegrityViolationException при нарушении уникальности на уровне БД
     * @throws ConstraintViolationException    если transaction == null
     */
    boolean isExistsByEventId(@NotBlank(message = "{event.id.required}") String eventId);

    /**
     * Сохранить транзакцию, если событие с таким {@link Transaction#getEventId()} ещё не обрабатывалось
     *
     * @param transaction Транзакция
     * @return Транзакция
     * @throws DuplicateEventException         если событие уже существует
     * @throws DataIntegrityViolationException при нарушении уникальности
     */
    Transaction saveIfNotExists(@NotNull(message = "{transaction.required}") Transaction transaction);

    /**
     * Получить страницу транзакции по идентификатору пользователя из LMS
     *
     * @param userId   Идентификатор пользователя из LMS
     * @param pageable Параметры пагинации и сортировки
     * @return Страница транзакций
     * @throws ConstraintViolationException если userId — null или пустая строка, или pageable == null
     */
    Page<Transaction> getTransactionsByUserId(@NotBlank(message = "{user.id.required}") String userId,
                                              @NotNull(message = "{pageable.required}") Pageable pageable);

    /**
     * Получить сумму очков, начисленных пользователю по конкретному типу события за указанный день
     *
     * @param userUuid      UUID пользователя
     * @param eventTypeUuid UUID типа события
     * @param date          Дата, за которую считается сумма (не может быть null)
     * @return Сумма начисленных очков за день
     * @throws ConstraintViolationException если любой из параметров null
     */
    long sumPointsByUserAndEventTypeAndDate(@NotNull(message = "{user.uuid.required}") UUID userUuid,
                                            @NotNull(message = "{eventType.uuid.required}") UUID eventTypeUuid,
                                            @NotNull(message = "{date.required}") LocalDate date);
}
