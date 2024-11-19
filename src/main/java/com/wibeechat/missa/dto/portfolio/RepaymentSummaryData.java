package com.wibeechat.missa.dto.portfolio;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RepaymentSummaryData {
    private String repaymentCode;
    private Double repaymentAmount;
}
