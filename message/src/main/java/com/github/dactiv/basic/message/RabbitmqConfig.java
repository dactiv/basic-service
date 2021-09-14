package com.github.dactiv.basic.message;

import org.springframework.amqp.core.AsyncAmqpTemplate;
import org.springframework.amqp.rabbit.AsyncRabbitTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * rabbitmq 配置
 *
 * @author maurice.chen
 */
@Configuration
public class RabbitmqConfig {

    public final static String DEFAULT_DELAY_EXCHANGE = "message.delay.exchange";

    @Bean
    public AsyncAmqpTemplate asyncAmqpTemplate(RabbitTemplate rabbitTemplate) {
        return new AsyncRabbitTemplate(rabbitTemplate);
    }
}
