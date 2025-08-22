package com.financesoftware.ledger.feign;

import com.financesoftware.ledger.dto.MerchantDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "merchant-service", url = "${merchant.service.url}")
public interface MerchantServiceClient {

    @GetMapping("/api/merchants/{id}")
    MerchantDTO getMerchantById(@PathVariable("id") UUID id);

    @PutMapping("/api/merchants/{id}/debt")
    void updateMerchantDebt(@PathVariable("id") UUID id, @RequestParam("debt") BigDecimal debt);

    @PutMapping("/api/merchants/{id}")
    MerchantDTO updateMerchant(@PathVariable("id") UUID id, @RequestBody MerchantDTO merchant);
}
