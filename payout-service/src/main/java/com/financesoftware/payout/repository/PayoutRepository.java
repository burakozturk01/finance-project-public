package com.financesoftware.payout.repository;

import com.financesoftware.payout.entity.Payout;
import com.financesoftware.common.enums.PayoutStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PayoutRepository extends JpaRepository<Payout, UUID> {
    List<Payout> findByStatus(PayoutStatus status);
    List<Payout> findByMerchantId(UUID merchantId);
    List<Payout> findByMerchantIdAndStatus(UUID merchantId, PayoutStatus status);
}
