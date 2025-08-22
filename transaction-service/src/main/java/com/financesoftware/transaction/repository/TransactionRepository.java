package com.financesoftware.transaction.repository;

import com.financesoftware.common.enums.TransactionStatus;
import com.financesoftware.transaction.entity.Transaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByMerchantIdAndStatus(UUID merchantId, TransactionStatus status);
    List<Transaction> findByStatus(TransactionStatus status);
    Page<Transaction> findByStatus(TransactionStatus status, Pageable pageable);
}
