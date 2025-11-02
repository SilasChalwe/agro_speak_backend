package com.nextinnomind.agro_speak_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private final RestTemplate restTemplate;

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${twilio.from-number:}")
    private String fromNumber;

    private boolean configured() {
        return accountSid != null && !accountSid.isEmpty()
                && authToken != null && !authToken.isEmpty()
                && fromNumber != null && !fromNumber.isEmpty();
    }

    public boolean sendSms(String to, String body) {
        if (!configured()) {
            log.warn("Twilio not configured - skipping SMS to {}: {}", to, body);
            return false;
        }

        try {
            String url = String.format("https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json", accountSid);

            HttpHeaders headers = new HttpHeaders();
            // Suppress null warnings - credentials already validated in configured() check
            if (accountSid != null && authToken != null) {
                headers.setBasicAuth(accountSid, authToken);
            }
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("From", fromNumber);
            form.add("To", to);
            form.add("Body", body);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

            @SuppressWarnings("null")
            ResponseEntity<String> resp = restTemplate.postForEntity(url, request, String.class);
            if (resp.getStatusCode().is2xxSuccessful()) {
                log.info("SMS sent to {}", to);
                return true;
            } else {
                log.warn("Failed to send SMS to {}: {}", to, resp.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("Error sending SMS to {}: {}", to, e.getMessage(), e);
            return false;
        }
    }
}
