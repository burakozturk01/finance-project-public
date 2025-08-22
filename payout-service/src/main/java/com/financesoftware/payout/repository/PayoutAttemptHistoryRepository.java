package com.financesoftware.payout.repository;

import com.financesoftware.payout.entity.PayoutAttemptHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for PayoutAttemptHistory entity
 */
@Repository
public interface PayoutAttemptHistoryRepository extends JpaRepository<PayoutAttemptHistory, UUID> {

    /**
     * Find all attempt histories for a specific payout
     * @param payoutId the payout ID
     * @return list of attempt histories
     */
    List<PayoutAttemptHistory> findByPayoutIdOrderByAttemptTimestampDesc(UUID payoutId);
}
