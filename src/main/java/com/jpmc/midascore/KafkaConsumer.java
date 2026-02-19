package com.jpmc.midascore;

import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.foundation.TransactionRecord;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class KafkaConsumer {

    private final UserRepository userRepository;
    private final TransactionRecordRepository transactionRecordRepository;
    private final RestTemplate restTemplate;

    public KafkaConsumer(UserRepository userRepository,
                         TransactionRecordRepository transactionRecordRepository,
                         RestTemplate restTemplate) {

        this.userRepository = userRepository;
        this.transactionRecordRepository = transactionRecordRepository;
        this.restTemplate = restTemplate;
    }


    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-core-group")
    public void listen(Transaction transaction) {

        UserRecord sender = userRepository.findById(transaction.getSenderId()).orElse(null);
        UserRecord recipient = userRepository.findById(transaction.getRecipientId()).orElse(null);

        if (sender == null || recipient == null) {
            return;
        }

        if (sender.getBalance() >= transaction.getAmount()) {

            String url = "http://localhost:8080/incentive";

            Incentive incentive =
                    restTemplate.postForObject(
                            url,
                            transaction,
                            Incentive.class
                    );

            double incentiveAmount = incentive != null ? incentive.getAmount() : 0.0;

            sender.setBalance(sender.getBalance() - transaction.getAmount());
            recipient.setBalance(
                    recipient.getBalance()
                            + transaction.getAmount()
                            + (float)incentiveAmount
            );


            TransactionRecord record =
                    new TransactionRecord(
                            transaction.getAmount(),
                            incentiveAmount,
                            sender,
                            recipient
                    );

            userRepository.save(sender);
            userRepository.save(recipient);
            transactionRecordRepository.save(record);
        }
    }
}
