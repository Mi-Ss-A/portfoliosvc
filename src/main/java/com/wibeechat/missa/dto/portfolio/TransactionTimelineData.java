package com.wibeechat.missa.dto.portfolio;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionTimelineData {

    private String date;         // 거래 날짜
    private Double amount;       // 거래 금액
    private String type;         // 거래 유형 (e.g., Fund, Bank, Loan)

    @Override
    public String toString() {
        return "TransactionTimelineData{" +
                "date='" + date + '\'' +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                '}';
    }
}
