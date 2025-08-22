package com.financesoftware.transaction.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "merchant-service", url = "${merchant.service.url}")
public interface MerchantServiceClient {

    @GetMapping("/merchants/{id}/validate")
    boolean validateMerchant(@PathVariable("id") UUID id);
}
