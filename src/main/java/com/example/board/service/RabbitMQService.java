package com.example.board.service;

import com.example.board.pojo.Notification;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQService {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public RabbitMQService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(Notification notification) {
        this.rabbitTemplate.convertAndSend("board-notification", notification.toJson());
    }

    @RabbitListener(queues = "board-notification")
    public void receive(String message) {
        System.out.println(message);
    }
}
