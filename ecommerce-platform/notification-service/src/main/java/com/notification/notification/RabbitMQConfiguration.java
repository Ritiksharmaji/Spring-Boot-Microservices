//package com.notification.notification;
//
//import org.springframework.amqp.core.*;
//import org.springframework.amqp.rabbit.connection.ConnectionFactory;
//import org.springframework.amqp.rabbit.core.RabbitAdmin;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
//import org.springframework.amqp.support.converter.MessageConverter;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class RabbitMQConfiguration {
//
//    // first read all the configuration from the yml file
//
//    @Value("${rabbitmq.queue.name}")
//    private String queueName;
//    @Value("${rabbitmq.exchange.name}")
//    private String exchangeName;
//    @Value("${rabbitmq.routing.key}")
//    private String routingKey;
//
//    @Bean
//    public Queue queue() {
//        return QueueBuilder.durable(queueName)
//                .build();
//    }
//
//    @Bean
//    public TopicExchange exchange() {
//        return ExchangeBuilder.topicExchange(exchangeName)
//                .durable(true).build();
//    }
//
//    @Bean
//    public Binding binding() {
//        return BindingBuilder.bind(queue())
//                .to(exchange()).with(routingKey);
//    }
//
//    // now add admin realted configuration
//
//    @Bean
//    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
//        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
//        admin.setAutoStartup(true);
//        // be giving setAutoStartUp as true all configuratuon will autometically based on given in code
//        return admin;
//    }
//
//    // now we need to define the message convert because convet java object to JSN format.
//    @Bean
//    public MessageConverter messageConverter() {
//        return new Jackson2JsonMessageConverter();
//    }
//
//    // define the rabbitMQ templat not required becasue we are not sending message we are receving message
////    @Bean
////    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
////        RabbitTemplate template = new RabbitTemplate(connectionFactory);
////        template.setMessageConverter(messageConverter());
////        template.setExchange(exchangeName);
////        return template;
////    }
//
//
//}
