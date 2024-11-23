package com.wibeechat.missa.service.portfolio;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.wibeechat.missa.dto.portfolio.AssetDistributionData;
import com.wibeechat.missa.dto.portfolio.CardTransactionData;
import com.wibeechat.missa.dto.portfolio.FundTransactionTypeData;
import com.wibeechat.missa.dto.portfolio.LoanStatusData;
import com.wibeechat.missa.dto.portfolio.LoanTransactionData;
import com.wibeechat.missa.dto.portfolio.MonthlyBankInOutData;
import com.wibeechat.missa.dto.portfolio.PortfolioResponse;
import com.wibeechat.missa.dto.portfolio.RepaymentSummaryData;
import com.wibeechat.missa.dto.portfolio.TransactionTimelineData;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PortfolioService {

        private final KibanaService kibanaService;
        private final ChartService chartService;
        private final HtmlService htmlService;
        private final PdfService pdfService;
        private final ExtractTransactionService extractTransactionService;
        private final S3Service s3Service;

        public PortfolioService(KibanaService kibanaService, ChartService chartService, HtmlService htmlService,
                        PdfService pdfService, ExtractTransactionService extractTransactionService,
                        S3Service s3Service) {
                this.kibanaService = kibanaService;
                this.chartService = chartService;
                this.htmlService = htmlService;
                this.pdfService = pdfService;
                this.extractTransactionService = extractTransactionService;
                this.s3Service = s3Service;
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
                        List<CardTransactionData> cardTransactionData = extractTransactionService
                                        .extractCardTransactionData(cardData);

                        List<MonthlyBankInOutData> monthlyBankInOutData = extractTransactionService
                                        .extractMonthlyBankInOutData(bankData);
                        List<LoanTransactionData> loanTransactionData = extractTransactionService
                                        .extractLoanTransactionData(loanData);
                        Map<String, Double> fundTransactionTypeMap = extractTransactionService
                                        .extractFundTransactionTypeData(fundData);
                        // Map<String, Double>를 List<FundTransactionTypeData>로 변환
                        List<FundTransactionTypeData> fundTransactionTypeData = fundTransactionTypeMap.entrySet()
                                        .stream()
                                        .map(entry -> new FundTransactionTypeData(entry.getKey(), entry.getValue()))
                                        .collect(Collectors.toList());

                        List<TransactionTimelineData> transactionTimelineData = extractTransactionService
                                        .extractAssetTimeline(fundData, bankData);

                        Map<String, Double> assetDistributionMap = Map.of(
                                        "Fund", extractTransactionService.extractTotalFundAmount(fundData),
                                        "Bank", extractTransactionService.extractLatestBankBalance(bankData));
                        List<AssetDistributionData> assetDistributionData = assetDistributionMap.entrySet()
                                        .stream()
                                        .map(entry -> new AssetDistributionData(entry.getKey(), entry.getValue()))
                                        .collect(Collectors.toList());

                        Map<String, Double> repaymentSummaryMap = extractTransactionService
                                        .extractLoanRepaymentSummary(loanData);
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
                                        extractTransactionService.extractLoanAmounts(loanData));
                        String assetDistributionChart = chartService.generatePieChart("Asset Distribution",
                                        assetDistributionMap);
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
                                        fundTransactionTypeData);

                        // 5. PDF 변환
                        File pdfFile = pdfService.convertHtmlToPdf(htmlContent, userId, portfolioData);

                        // 6. S3에 업로드
                        String keyName = "portfolios/" + userId + "/" + portfolioData + ".pdf";
                        String s3Url = s3Service.uploadFile(pdfFile, keyName);

                        // 업로드 후 로컬 파일 삭제 (선택 사항)
                        boolean deleted = pdfFile.delete();
                        if (!deleted) {
                                log.warn("임시 PDF 파일을 삭제하지 못했습니다: " + pdfFile.getAbsolutePath());
                        }
                        List<String> s3Urls = Collections.singletonList(s3Url);
                        return new PortfolioResponse(s3Urls, "포트폴리오가 성공적으로 생성되었습니다");
                } catch (Exception e) {
                        log.error("Error generating portfolio", e);
                        return new PortfolioResponse("error", "Portfolio generation failed");
                }
        }

        // 특정 userId를 기반으로 모든 포트폴리오 urls를 반환하는 기능
        public PortfolioResponse getPortfolioList(String userId) {
                try {
                        List<String> s3Urls = s3Service.getPortfolioUrlsForUser(userId);
                        return new PortfolioResponse(s3Urls, "포트폴리오가 성공적으로 조회되었습니다");
                } catch (Exception e) {
                        log.error("Error get portfolios", e);
                        return new PortfolioResponse("error", "Get Portfolios failed");
                }
        }
}
