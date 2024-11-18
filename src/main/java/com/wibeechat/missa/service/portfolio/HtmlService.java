package com.wibeechat.missa.service.portfolio;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HtmlService {

    private final SpringTemplateEngine templateEngine;

    public HtmlService(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String generateHtmlContent(String userId,
                                       List<String> cardLabels, List<Double> cardAmounts, List<Double> cardInOutValues,
                                       String cardAmountChartPath, String cardInOutChartPath,
                                       List<String> bankLabels, List<Double> bankAmounts,
                                       String bankTransactionChartPath, String bankInOutChartPath,
                                       List<String> loanLabels, List<Double> loanAmounts,
                                       String loanAmountChartPath) {
        Context context = new Context();
        context.setVariable("userId", userId);
        
        // Card 데이터 병렬화
        List<Map<String, Object>> cardTransactionData = new ArrayList<>();
        for (int i = 0; i < cardLabels.size(); i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("label", cardLabels.get(i));
            data.put("amount", cardAmounts.get(i));
            data.put("inOut", cardInOutValues.get(i));
            cardTransactionData.add(data);
        }
        context.setVariable("cardTransactionData", cardTransactionData);
        context.setVariable("cardAmountChart", new File(cardAmountChartPath).toURI().toString());
        context.setVariable("cardInOutChart", new File(cardInOutChartPath).toURI().toString());
        
        // Bank 데이터 병렬화
        List<Map<String, Object>> bankTransactionData = new ArrayList<>();
        for (int i = 0; i < bankLabels.size(); i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("label", bankLabels.get(i));
            data.put("amount", bankAmounts.get(i));
            bankTransactionData.add(data);
        }
        context.setVariable("bankTransactionData", bankTransactionData);
        context.setVariable("bankTransactionChart", new File(bankTransactionChartPath).toURI().toString());
        context.setVariable("bankInOutChart", new File(bankInOutChartPath).toURI().toString());
        
        // Loan 데이터 병렬화
        List<Map<String, Object>> loanTransactionData = new ArrayList<>();
        for (int i = 0; i < loanLabels.size(); i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("label", loanLabels.get(i));
            data.put("amount", loanAmounts.get(i));
            loanTransactionData.add(data);
        }
        context.setVariable("loanTransactionData", loanTransactionData);
        context.setVariable("loanAmountChart", new File(loanAmountChartPath).toURI().toString());
        
        // 현재 날짜와 시간 전달
        context.setVariable("currentDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        return templateEngine.process("portfolio", context);
    }
}
