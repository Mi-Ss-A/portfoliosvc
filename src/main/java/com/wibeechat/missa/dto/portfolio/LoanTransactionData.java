package com.wibeechat.missa.dto.portfolio;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoanTransactionData {
    private String startDate;
    private String endDate;
    private Double amount;
    private String status;
}
