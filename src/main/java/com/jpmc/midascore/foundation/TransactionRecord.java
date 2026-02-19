package com.jpmc.midascore.foundation;

import com.jpmc.midascore.entity.UserRecord;
import jakarta.persistence.*;
import org.h2.engine.User;


@Entity
public class TransactionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double amount;
    private double incentive;

    @ManyToOne
    private UserRecord sender;

    @ManyToOne
    private UserRecord recipient;

    public TransactionRecord() {}

    public TransactionRecord(double amount, double incentive,
                             UserRecord sender, UserRecord recipient) {
        this.amount = amount;
        this.incentive = incentive;
        this.sender = sender;
        this.recipient = recipient;
    }


    public double getAmount() {
        return amount;
    }

    public double getIncentive() {
        return incentive;
    }

    public UserRecord getSender() {
        return sender;
    }

    public UserRecord getRecipient() {
        return recipient;
    }

}
