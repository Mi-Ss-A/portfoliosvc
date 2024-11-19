package com.wibeechat.missa.service.portfolio;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.wibeechat.missa.dto.portfolio.*;

@Service
public class HtmlService {

    private final SpringTemplateEngine templateEngine;

    public HtmlService(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String generateHtmlContent(
            String userId,
            String cardAmountChart,
            List<CardTransactionData> cardTransactionData,
            String monthlyBankInOutChart,
            List<MonthlyBankInOutData> monthlyBankInOutData,
            String loanAmountChart,
            List<LoanTransactionData> loanTransactionData,
            String assetDistributionChart,
            List<AssetDistributionData> assetDistributionData,
            String transactionTimelineChart,
            List<TransactionTimelineData> transactionTimelineData,
            String repaymentSummaryChart,
            List<RepaymentSummaryData> repaymentSummaryData,
            String loanStatusChart,
            List<LoanStatusData> loanStatusData,
            String fundTransactionTypeChart,
            List<FundTransactionTypeData> fundTransactionTypeData
    ) {
        // Create Thymeleaf context and add variables
        Context context = new Context();
        context.setVariable("userId", userId);
        context.setVariable("currentDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // Chart paths or Base64 strings
        context.setVariable("cardAmountChart", cardAmountChart);
        context.setVariable("monthlyBankInOutChart", monthlyBankInOutChart);
        context.setVariable("loanAmountChart", loanAmountChart);
        context.setVariable("assetDistributionChart", assetDistributionChart);
        context.setVariable("transactionTimelineChart", transactionTimelineChart);
        context.setVariable("repaymentSummaryChart", repaymentSummaryChart);
        context.setVariable("loanStatusChart", loanStatusChart);
        context.setVariable("fundTransactionTypeChart", fundTransactionTypeChart);

        // Data objects
        context.setVariable("cardTransactionData", cardTransactionData);
        context.setVariable("monthlyBankInOutData", monthlyBankInOutData);
        context.setVariable("loanTransactionData", loanTransactionData);
        context.setVariable("assetDistributionData", assetDistributionData);
        context.setVariable("transactionTimelineData", transactionTimelineData);
        context.setVariable("repaymentSummaryData", repaymentSummaryData);
        context.setVariable("loanStatusData", loanStatusData);
        context.setVariable("fundTransactionTypeData", fundTransactionTypeData);

        // Generate HTML using Thymeleaf template
        return templateEngine.process("portfolio", context);
    }
}
