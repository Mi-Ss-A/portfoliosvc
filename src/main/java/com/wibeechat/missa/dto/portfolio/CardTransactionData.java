package com.wibeechat.missa.dto.portfolio;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CardTransactionData {
    private String date;
    private Double amount;
    private String type;
}
