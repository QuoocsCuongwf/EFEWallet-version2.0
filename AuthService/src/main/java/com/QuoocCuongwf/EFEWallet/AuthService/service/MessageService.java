package com.QuoocCuongwf.EFEWallet.AuthService.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final KafkaTemplate<String, Object> kafkaTemplate;


    public void sendMessage(String topic, Object data) {
        kafkaTemplate.send(topic, data);
    }
}