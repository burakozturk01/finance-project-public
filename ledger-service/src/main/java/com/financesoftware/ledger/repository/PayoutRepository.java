package com.financesoftware.ledger.repository;

import com.financesoftware.ledger.entity.Payout;
import com.financesoftware.common.enums.PayoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PayoutRepository extends JpaRepository<Payout, UUID> {

    List<Payout> findByMerchantId(UUID merchantId);

    List<Payout> findByStatus(PayoutStatus status);

    Page<Payout> findByStatus(PayoutStatus status, Pageable pageable);

    List<Payout> findByMerchantIdAndStatus(UUID merchantId, PayoutStatus status);

    @Query("SELECT p FROM Payout p WHERE p.status = :status AND p.merchantId = :merchantId")
    List<Payout> findByStatusAndMerchantId(@Param("status") PayoutStatus status,
                                          @Param("merchantId") UUID merchantId);

    @Query("SELECT p FROM Payout p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<Payout> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM Payout p WHERE p.merchantId = :merchantId AND p.createdAt BETWEEN :startDate AND :endDate")
    List<Payout> findByMerchantIdAndCreatedAtBetween(@Param("merchantId") UUID merchantId,
                                                    @Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(p) FROM Payout p WHERE p.status = :status")
    long countByStatus(@Param("status") PayoutStatus status);
}
