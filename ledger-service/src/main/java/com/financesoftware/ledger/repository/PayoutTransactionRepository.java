package com.financesoftware.ledger.repository;

import com.financesoftware.ledger.entity.PayoutTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayoutTransactionRepository extends JpaRepository<PayoutTransaction, UUID> {

    List<PayoutTransaction> findByPayoutId(UUID payoutId);

    Optional<PayoutTransaction> findByTransactionId(UUID transactionId);

    boolean existsByTransactionId(UUID transactionId);

    void deleteByPayoutId(UUID payoutId);

    @Query("SELECT pt.transactionId FROM PayoutTransaction pt WHERE pt.payoutId = :payoutId")
    List<UUID> findTransactionIdsByPayoutId(@Param("payoutId") UUID payoutId);

    @Query("SELECT pt.payoutId FROM PayoutTransaction pt WHERE pt.transactionId = :transactionId")
    Optional<UUID> findPayoutIdByTransactionId(@Param("transactionId") UUID transactionId);

    @Query("SELECT COUNT(pt) FROM PayoutTransaction pt WHERE pt.payoutId = :payoutId")
    long countByPayoutId(@Param("payoutId") UUID payoutId);
}
