package com.neocommercepay.user.event;

import com.neocommercepay.common.constants.KafkaTopics;
import com.neocommercepay.common.event.UserCreatedEvent;
import com.neocommercepay.common.event.UserDeletedEvent;
import com.neocommercepay.common.event.UserUpdatedEvent;
import com.neocommercepay.common.util.CorrelationIdUtil;
import com.neocommercepay.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUserCreated(User user) {
        UserCreatedEvent event = UserCreatedEvent.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdUtil.get())
                .build();

        kafkaTemplate.send(KafkaTopics.USER_CREATED, event);
        log.info("Published UserCreatedEvent for user: {}", user.getEmail());
    }

    public void publishUserUpdated(User user) {
        UserUpdatedEvent event = UserUpdatedEvent.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdUtil.get())
                .build();

        kafkaTemplate.send(KafkaTopics.USER_UPDATED, event);
        log.info("Published UserUpdatedEvent for user: {}", user.getEmail());
    }

    public void publishUserDeleted(Long userId) {
        UserDeletedEvent event = UserDeletedEvent.builder()
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .correlationId(CorrelationIdUtil.get())
                .build();

        kafkaTemplate.send(KafkaTopics.USER_DELETED, event);
        log.info("Published UserDeletedEvent for user: {}", userId);
    }
}
