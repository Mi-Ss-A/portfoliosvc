package com.wibeechat.missa.dto.portfolio;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FundTransactionTypeData {
    private String transactionType;
    private Double totalAmount;
}
