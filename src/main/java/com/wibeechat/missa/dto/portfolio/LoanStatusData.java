package com.wibeechat.missa.dto.portfolio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
@AllArgsConstructor
public class LoanStatusData {
    private String loanStatus;
    private Double totalAmount;
}
