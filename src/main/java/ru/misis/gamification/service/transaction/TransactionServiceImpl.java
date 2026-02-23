package ru.misis.gamification.service.transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.misis.gamification.exception.DuplicateEventException;
import ru.misis.gamification.model.admin.Transaction;
import ru.misis.gamification.repository.TransactionRepository;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    /**
     * Репозиторий транзакций
     */
    private final TransactionRepository transactionRepository;

    @Override
    public boolean isExistsByEventId(String eventId) {
        return transactionRepository.existsByEventId(eventId);
    }

    @Override
    public Transaction saveIfNotExists(Transaction transaction) throws DuplicateEventException {
        String eventId = transaction.getEventId();

        log.debug("Попытка сохранить транзакцию: eventId={}, userId={}, points={}",
                eventId, transaction.getUserId(), transaction.getPointsEarned());

        if (transactionRepository.existsByEventId(eventId)) {
            log.info("Попытка повторной обработки события: eventId={}", eventId);
            throw new DuplicateEventException(eventId);
        }

        try {
            Transaction saved = transactionRepository.save(transaction);
            log.info("Транзакция успешно сохранена: id={}, eventId={}", saved.getUuid(), eventId);
            return saved;
        } catch (DataIntegrityViolationException e) {
            log.info("Обнаружен дубликат события при сохранении: eventId={}", eventId);
            throw new DuplicateEventException(eventId);
        } catch (Exception e) {
            log.error("Неожиданная ошибка при сохранении транзакции: eventId={}", eventId, e);
            throw e;
        }
    }

    @Override
    public Page<Transaction> getTransactionsByUserId(String userId, Pageable pageable) {
        log.info("Запрос истории транзакций: userId={}, page={}, size={}, sort={}",
                userId, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        Page<Transaction> page = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        log.debug("Найдено транзакций: count={}, total={}", page.getNumberOfElements(), page.getTotalElements());
        return page;
    }

    @Override
    public long sumPointsByUserIdAndEventTypeAndDate(String userId, String typeCode, LocalDate date) {
        return transactionRepository.sumPointsByUserIdAndEventTypeAndDate(
                userId, typeCode, date);
    }
}
