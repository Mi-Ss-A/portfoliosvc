package com.wibeechat.missa.service.portfolio;

import com.wibeechat.missa.dto.portfolio.PortfolioResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PortfolioService {

    private final KibanaService kibanaService;
    private final ChartService chartService;
    private final HtmlService htmlService;
    private final PdfService pdfService;

    public PortfolioService(KibanaService kibanaService, ChartService chartService, HtmlService htmlService, PdfService pdfService) {
        this.kibanaService = kibanaService;
        this.chartService = chartService;
        this.htmlService = htmlService;
        this.pdfService = pdfService;
    }

    public PortfolioResponse generateAndSavePortfolio(String userId, String portfolioData) {
        try {
            // Kibana에서 데이터 가져오기
            Map<String, Object> cardData = kibanaService.getTransactionData(userId, portfolioData, "card");
            Map<String, Object> bankData = kibanaService.getTransactionData(userId, portfolioData, "bank");
            Map<String, Object> loanData = kibanaService.getTransactionData(userId, portfolioData, "loan");

            // card 데이터 추출
            List<String> cardDates = extractTransactionDates(cardData);
            List<Double> cardAmounts = extractTransactionAmounts(cardData);
            List<Double> cardInOutValues = extractTransactionInOut(cardData);

            // bank 데이터 추출
            List<String> bankDates = extractTransactionDates(bankData);
            List<Double> bankAmounts = extractTransactionAmounts(bankData);
            List<Double> bankInOutValues = extractTransactionInOut(bankData);

            // loan 데이터 추출
            List<String> loanDates = extractTransactionDates(loanData);
            List<Double> loanAmounts = extractTransactionAmounts(loanData);
            List<Double> loanInOutValues = extractTransactionInOut(loanData);

            // card 차트 생성
            String amountChartFilePath = chartService.generateAmountChart(cardDates, cardAmounts);
            String inOutChartFilePath = chartService.generateInOutChart(cardDates, cardInOutValues);

            // HTML 생성
            String htmlContent = htmlService.generateHtmlContent(userId, cardDates, cardAmounts, cardInOutValues, amountChartFilePath, inOutChartFilePath);

            // PDF 변환
            File pdfFile = pdfService.convertHtmlToPdf(htmlContent, userId, portfolioData);

            return new PortfolioResponse(pdfFile.getAbsolutePath(), "포트폴리오 생성 완료");

        } catch (Exception e) {
            log.error("포트폴리오 생성 중 오류 발생", e);
            return new PortfolioResponse("error", "포트폴리오 생성 실패");
        }
    }

    private List<String> extractTransactionDates(Map<String, Object> cardData) {
        List<String> dates = new ArrayList<>();
        try {
            // hits.hits 추출
            Map<String, Object> hits = (Map<String, Object>) cardData.get("hits");
            List<Map<String, Object>> hitList = (List<Map<String, Object>>) hits.get("hits");

            // 날짜 데이터 추출
            for (Map<String, Object> hit : hitList) {
                try {
                    Map<String, Object> source = (Map<String, Object>) hit.get("_source");
                    String transactionDate = (String) source.get("transaction_date");
                    if (transactionDate != null && transactionDate.length() >= 7) {
                        dates.add(transactionDate.substring(0, 7)); // "YYYY-MM"
                    } else {
                        log.warn("유효하지 않은 transaction_date: {}", source);
                    }
                } catch (Exception e) {
                    log.error("날짜 처리 중 오류 발생: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Transaction dates 추출 중 오류 발생: {}", e.getMessage());
        }
        return dates;
    }

    private List<Double> extractTransactionAmounts(Map<String, Object> cardData) {
        List<Double> amounts = new ArrayList<>();
        try {
            Map<String, Object> hits = (Map<String, Object>) cardData.get("hits");
            List<Map<String, Object>> hitList = (List<Map<String, Object>>) hits.get("hits");

            for (Map<String, Object> hit : hitList) {
                Map<String, Object> source = (Map<String, Object>) hit.get("_source");
                Number transactionAmount = (Number) source.get("transaction_amount");
                if (transactionAmount != null) {
                    amounts.add(transactionAmount.doubleValue());
                } else {
                    log.warn("transaction_amount가 누락된 항목이 있습니다: {}", source);
                }
            }
        } catch (Exception e) {
            log.error("Transaction amounts 추출 중 오류 발생: {}", e.getMessage());
        }
        return amounts;
    }

    private List<Double> extractTransactionInOut(Map<String, Object> cardData) {
        List<Double> inOutValues = new ArrayList<>();
        try {
            Map<String, Object> hits = (Map<String, Object>) cardData.get("hits");
            List<Map<String, Object>> hitList = (List<Map<String, Object>>) hits.get("hits");

            for (Map<String, Object> hit : hitList) {
                Map<String, Object> source = (Map<String, Object>) hit.get("_source");
                String transactionType = (String) source.get("transaction_type");
                Number transactionAmount = (Number) source.get("transaction_amount");

                if (transactionAmount != null) {
                    if ("C".equals(transactionType)) { // 입금
                        inOutValues.add(transactionAmount.doubleValue());
                    } else if ("D".equals(transactionType)) { // 출금
                        inOutValues.add(-transactionAmount.doubleValue());
                    } else {
                        log.warn("알 수 없는 transaction_type: {}", source);
                    }
                } else {
                    log.warn("transaction_amount가 누락된 항목이 있습니다: {}", source);
                }
            }
        } catch (Exception e) {
            log.error("Transaction in/out 추출 중 오류 발생: {}", e.getMessage());
        }
        return inOutValues;
    }
}
