package com.wibeechat.missa.dto.portfolio;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@AllArgsConstructor
@Getter
@Setter
public class MonthlyBankInOutData {
    private String month;
    private Double inflow;
    private Double outflow;
}
