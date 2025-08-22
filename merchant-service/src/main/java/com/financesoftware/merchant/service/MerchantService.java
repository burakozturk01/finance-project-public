package com.financesoftware.merchant.service;

import com.financesoftware.merchant.entity.Merchant;
import com.financesoftware.merchant.repository.MerchantRepository;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;

@Service
public class MerchantService {

    @Autowired
    private MerchantRepository merchantRepository;

    public Merchant createMerchant(Merchant merchant) {
        merchant.setCreatedAt(LocalDateTime.now());
        return merchantRepository.save(merchant);
    }

    public Optional<Merchant> getMerchantById(UUID id) {
        return merchantRepository.findById(id);
    }

    public List<Merchant> getAllMerchants() {
        return merchantRepository.findAll();
    }

    public Optional<Merchant> getMerchantByNameEmailIban(String name, String email, String iban) {
        return merchantRepository.findByNameAndEmailAndIban(name, email, iban);
    }

    public Merchant updateMerchant(UUID id, Merchant merchantDetails) {
        Optional<Merchant> merchantOptional = merchantRepository.findById(id);
        if (merchantOptional.isPresent()) {
            merchantRepository.deleteById(id);
            Merchant newMerchant = new Merchant(
                merchantDetails.getName(),
                merchantDetails.getEmail(),
                merchantDetails.getIban(),
                merchantDetails.getCommissionPercentage() // Include commissionPercentage
            );
            newMerchant.setCreatedAt(merchantOptional.get().getCreatedAt());
            return merchantRepository.save(newMerchant);
        }
        return null;
    }

    public void deleteMerchant(UUID id) {
        merchantRepository.deleteById(id);
    }

    // New: Update merchant debt
    public boolean updateMerchantDebt(UUID id, BigDecimal debt) {
        Optional<Merchant> merchantOptional = merchantRepository.findById(id);
        if (merchantOptional.isPresent()) {
            Merchant merchant = merchantOptional.get();
            merchant.setDebt(debt);
            merchantRepository.save(merchant);
            return true;
        }
        return false;
    }
}
