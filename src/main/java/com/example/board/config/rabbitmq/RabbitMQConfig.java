package com.example.board.config.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue queue() {
        return new Queue("board-notification", true);
    }

    @Bean
    public Queue sendNotificationEmail() {
        return new Queue("send_notification.email", true);
    }

    @Bean
    public Queue sendNotificationSms() {
        return new Queue("send_notification.sms", true);
    }

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange("send_notification_exchange");
    }

    @Bean
    public Binding bindingEmailQueue() {
        return BindingBuilder.bind(this.sendNotificationEmail()).to(this.fanoutExchange());
    }

    @Bean
    public Binding bindingSmsQueue() {
        return BindingBuilder.bind(this.sendNotificationSms()).to(this.fanoutExchange());
    }

}
