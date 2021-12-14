package com.github.dactiv.basic.message.domain.meta;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 短信余额实体
 *
 * @author maurice
 */
public class SmsBalanceMeta implements Serializable {

    private static final long serialVersionUID = 4834851659384448629L;

    private String name;

    private BigDecimal balance;

    public SmsBalanceMeta(String name, BigDecimal balance) {
        this.name = name;
        this.balance = balance;
    }

    public SmsBalanceMeta() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
