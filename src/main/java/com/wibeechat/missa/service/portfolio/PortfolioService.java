package com.wibeechat.missa.service.portfolio;

import com.wibeechat.missa.dto.portfolio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PortfolioService {

    private final KibanaService kibanaService;
    private final ChartService chartService;
    private final HtmlService htmlService;
    private final PdfService pdfService;
    private final ExtractTransactionService extractTransactionService;

    public PortfolioService(KibanaService kibanaService, ChartService chartService, HtmlService htmlService,
                            PdfService pdfService, ExtractTransactionService extractTransactionService) {
        this.kibanaService = kibanaService;
        this.chartService = chartService;
        this.htmlService = htmlService;
        this.pdfService = pdfService;
        this.extractTransactionService = extractTransactionService;
    }

    public PortfolioResponse generateAndSavePortfolio(String userId, String portfolioData) {
        try {
            // 1. Kibana에서 데이터 가져오기
            Map<String, Object> cardData = kibanaService.getTransactionData(userId, portfolioData, "card");
            Map<String, Object> bankData = kibanaService.getTransactionData(userId, portfolioData, "bank");
            Map<String, Object> loanData = kibanaService.getTransactionData(userId, portfolioData, "loan");
            Map<String, Object> fundData = kibanaService.getTransactionData(userId, portfolioData, "fund");

            // 2. 데이터 처리 및 변환
            List<String> cardDates = extractTransactionService.extractTransactionDates(cardData);
            List<Double> cardAmounts = extractTransactionService.extractTransactionAmounts(cardData);
            List<CardTransactionData> cardTransactionData = extractTransactionService.extractCardTransactionData(cardData);

            List<MonthlyBankInOutData> monthlyBankInOutData = extractTransactionService.extractMonthlyBankInOutData(bankData);
            List<LoanTransactionData> loanTransactionData = extractTransactionService.extractLoanTransactionData(loanData);
            Map<String, Double> fundTransactionTypeMap = extractTransactionService.extractFundTransactionTypeData(fundData);
            // Map<String, Double>를 List<FundTransactionTypeData>로 변환
            List<FundTransactionTypeData> fundTransactionTypeData = fundTransactionTypeMap.entrySet()
            .stream()
            .map(entry -> new FundTransactionTypeData(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());

            List<TransactionTimelineData> transactionTimelineData = extractTransactionService.extractAssetTimeline(fundData, bankData);

            Map<String, Double> assetDistributionMap = Map.of(
                "Fund", extractTransactionService.extractTotalFundAmount(fundData),
                "Bank", extractTransactionService.extractLatestBankBalance(bankData)
            );
            List<AssetDistributionData> assetDistributionData = assetDistributionMap.entrySet()
                .stream()
                .map(entry -> new AssetDistributionData(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

            Map<String, Double> repaymentSummaryMap = extractTransactionService.extractLoanRepaymentSummary(loanData);
            List<RepaymentSummaryData> repaymentSummaryData = repaymentSummaryMap.entrySet()
                .stream()
                .map(entry -> new RepaymentSummaryData(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

            Map<String, Double> loanStatusMap = extractTransactionService.extractLoanStatusData(loanData);
            List<LoanStatusData> loanStatusData = loanStatusMap.entrySet()
                .stream()
                .map(entry -> new LoanStatusData(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

            // 3. 차트 생성
            String cardChart = chartService.generateAmountChart(cardDates, cardAmounts);
            String monthlyInOutChart = chartService.generateMonthlyInOutChart(monthlyBankInOutData);
            String loanChart = chartService.generateLoanAmountChart(
                extractTransactionService.extractLoanDates(loanData),
                extractTransactionService.extractLoanAmounts(loanData)
            );
            String assetDistributionChart = chartService.generatePieChart("Asset Distribution", assetDistributionMap);
            String timelineChart = chartService.generateTimelineChart(transactionTimelineData);
            String fundTypeChart = chartService.generateFundTransactionTypeChart(fundTransactionTypeData);
            String repaymentSummaryChart = chartService.generateRepaymentSummaryChart(repaymentSummaryMap);
            String loanStatusChart = chartService.generatePieChart("Loan Status Breakdown", loanStatusMap);

            String htmlContent = htmlService.generateHtmlContent(
                userId,
                cardChart,
                cardTransactionData,
                monthlyInOutChart,
                monthlyBankInOutData,
                loanChart,
                loanTransactionData,
                assetDistributionChart,
                assetDistributionData,
                timelineChart,
                transactionTimelineData,
                repaymentSummaryChart,
                repaymentSummaryData,
                loanStatusChart,
                loanStatusData,
                fundTypeChart,
                fundTransactionTypeData
            );

            // 5. PDF 변환
            File pdfFile = pdfService.convertHtmlToPdf(htmlContent, userId, portfolioData);

            return new PortfolioResponse(pdfFile.getAbsolutePath(), "Portfolio generated successfully");

        } catch (Exception e) {
            log.error("Error generating portfolio", e);
            return new PortfolioResponse("error", "Portfolio generation failed");
        }
    }
}
