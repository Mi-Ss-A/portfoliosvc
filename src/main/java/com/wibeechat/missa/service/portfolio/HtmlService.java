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

    public String generateHtmlContent(String userId, List<String> labels, List<Double> amounts, List<Double> inOutValues, String amountChartFilePath, String inOutChartFilePath) {
        Context context = new Context();
        context.setVariable("userId", userId);

        // 데이터를 병렬화
        List<Map<String, Object>> transactionData = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++) {
            Map<String, Object> data = new HashMap<>();
            data.put("label", labels.get(i));
            data.put("amount", amounts.get(i));
            data.put("inOut", inOutValues.get(i));
            transactionData.add(data);
        }
        context.setVariable("transactionData", transactionData);

        // 이미지 파일 경로 설정
        context.setVariable("amountChart", new File(amountChartFilePath).toURI().toString());
        context.setVariable("inOutChart", new File(inOutChartFilePath).toURI().toString());

        // 현재 날짜와 시간 전달
        context.setVariable("currentDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return templateEngine.process("portfolio", context);
    }
}