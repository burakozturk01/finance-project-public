package com.financesoftware.transaction.service;

import com.financesoftware.common.enums.TransactionStatus;
import com.financesoftware.transaction.entity.Transaction;
import com.financesoftware.transaction.repository.TransactionRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private RabbitMQSender rabbitMQSender;

    public Transaction processTransaction(Transaction transaction) {
        Transaction savedTransaction = transactionRepository.save(transaction);
        rabbitMQSender.sendTransactionCreatedEvent(savedTransaction);
        return savedTransaction;
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Page<Transaction> getTransactionsByStatus(String status, Pageable pageable) {
        TransactionStatus transactionStatus = TransactionStatus.valueOf(status);
        return transactionRepository.findByStatus(transactionStatus, pageable);
    }

    public Optional<Transaction> getTransactionById(String id) {
        return transactionRepository.findById(UUID.fromString(id));
    }

    public void deleteTransaction(Transaction transaction) {
        transactionRepository.delete(transaction);
    }

    public Optional<UUID> updateTransactionStatus(String id, String status) {
        Optional<Transaction> transaction = transactionRepository.findById(UUID.fromString(id));
        if (transaction.isPresent()) {
            Transaction existingTransaction = transaction.get();
            existingTransaction.setStatus(TransactionStatus.valueOf(status));
            Transaction updatedTransaction = transactionRepository.save(existingTransaction);
            return Optional.of(updatedTransaction.getId());
        }
        return Optional.empty();
    }

    public void setTransactionToPending(String id) {
        Optional<Transaction> transaction = transactionRepository.findById(UUID.fromString(id));
        if (transaction.isPresent() && transaction.get().getStatus() == TransactionStatus.VALIDATED) {
            Transaction existingTransaction = transaction.get();
            existingTransaction.setStatus(TransactionStatus.PENDING);
            transactionRepository.save(existingTransaction);
        }
    }

    public void setAllTransactionsToPendingForMerchant(String merchantId) {
        List<Transaction> transactions = transactionRepository.findByMerchantIdAndStatus(UUID.fromString(merchantId), TransactionStatus.VALIDATED);
        for (Transaction transaction : transactions) {
            transaction.setStatus(TransactionStatus.PENDING);
        }
        transactionRepository.saveAll(transactions);
    }

    public void setAllTransactionsToPending() {
        List<Transaction> transactions = transactionRepository.findByStatus(TransactionStatus.VALIDATED);
        for (Transaction transaction : transactions) {
            transaction.setStatus(TransactionStatus.PENDING);
        }
        transactionRepository.saveAll(transactions);
    }
}
