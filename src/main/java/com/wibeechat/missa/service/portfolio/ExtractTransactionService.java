package com.wibeechat.missa.service.portfolio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.wibeechat.missa.dto.portfolio.CardTransactionData;
import com.wibeechat.missa.dto.portfolio.LoanTransactionData;
import com.wibeechat.missa.dto.portfolio.MonthlyBankInOutData;
import com.wibeechat.missa.dto.portfolio.TransactionTimelineData;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class ExtractTransactionService {
    public List<String> extractTransactionDates(Map<String, Object> cardData) {
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

    public List<Double> extractTransactionAmounts(Map<String, Object> cardData) {
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

    public List<Double> extractTransactionInOut(Map<String, Object> cardData) {
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

    // 추가 메서드: Loan 데이터를 추출하는 메서드
    public List<String> extractLoanDates(Map<String, Object> loanData) {
        List<String> dates = new ArrayList<>();
        try {
            Map<String, Object> hits = (Map<String, Object>) loanData.get("hits");
            List<Map<String, Object>> hitList = (List<Map<String, Object>>) hits.get("hits");

            for (Map<String, Object> hit : hitList) {
                Map<String, Object> source = (Map<String, Object>) hit.get("_source");
                String startDate = (String) source.get("contract_start_date");
                if (startDate != null) {
                    dates.add(startDate.substring(0, 7)); // "YYYY-MM"
                } else {
                    log.warn("누락된 loan contract_start_date: {}", source);
                }
            }
        } catch (Exception e) {
            log.error("Loan dates 추출 중 오류 발생: {}", e.getMessage());
        }
        return dates;
    }

    public List<Double> extractLoanAmounts(Map<String, Object> loanData) {
        List<Double> amounts = new ArrayList<>();
        try {
            Map<String, Object> hits = (Map<String, Object>) loanData.get("hits");
            List<Map<String, Object>> hitList = (List<Map<String, Object>>) hits.get("hits");

            for (Map<String, Object> hit : hitList) {
                Map<String, Object> source = (Map<String, Object>) hit.get("_source");
                Number loanAmount = (Number) source.get("loan_contract_amount");
                if (loanAmount != null) {
                    amounts.add(loanAmount.doubleValue());
                } else {
                    log.warn("누락된 loan_contract_amount: {}", source);
                }
            }
        } catch (Exception e) {
            log.error("Loan amounts 추출 중 오류 발생: {}", e.getMessage());
        }
        return amounts;
    }

    // 추가 메서드: Bank 데이터를 추출하는 메서드
    public List<String> extractBankDates(Map<String, Object> bankData) {
        List<String> dates = new ArrayList<>();
        try {
            Map<String, Object> hits = (Map<String, Object>) bankData.get("hits");
            List<Map<String, Object>> hitList = (List<Map<String, Object>>) hits.get("hits");

            for (Map<String, Object> hit : hitList) {
                Map<String, Object> source = (Map<String, Object>) hit.get("_source");
                String transactionDate = (String) source.get("transaction_date");
                if (transactionDate != null) {
                    dates.add(transactionDate.substring(0, 10)); // "YYYY-MM-DD"
                } else {
                    log.warn("누락된 transaction_date: {}", source);
                }
            }
        } catch (Exception e) {
            log.error("Bank dates 추출 중 오류 발생: {}", e.getMessage());
        }
        return dates;
    }

    public List<Double> extractBankAmounts(Map<String, Object> bankData) {
        List<Double> amounts = new ArrayList<>();
        try {
            Map<String, Object> hits = (Map<String, Object>) bankData.get("hits");
            List<Map<String, Object>> hitList = (List<Map<String, Object>>) hits.get("hits");

            for (Map<String, Object> hit : hitList) {
                Map<String, Object> source = (Map<String, Object>) hit.get("_source");
                Number transactionAmount = (Number) source.get("transaction_amount");
                if (transactionAmount != null) {
                    amounts.add(transactionAmount.doubleValue());
                } else {
                    log.warn("누락된 transaction_amount: {}", source);
                }
            }
        } catch (Exception e) {
            log.error("Bank amounts 추출 중 오류 발생: {}", e.getMessage());
        }
        return amounts;
    }

    public List<Double> extractBankInOut(Map<String, Object> bankData) {
        List<Double> inOutValues = new ArrayList<>();
        try {
            Map<String, Object> hits = (Map<String, Object>) bankData.get("hits");
            List<Map<String, Object>> hitList = (List<Map<String, Object>>) hits.get("hits");

            for (Map<String, Object> hit : hitList) {
                Map<String, Object> source = (Map<String, Object>) hit.get("_source");
                Number transactionAmount = (Number) source.get("transaction_amount");
                Integer transactionInAndOut = (Integer) source.get("transaction_in_and_out");

                if (transactionAmount != null && transactionInAndOut != null) {
                    if (transactionInAndOut == 1) { // 입금
                        inOutValues.add(transactionAmount.doubleValue());
                    } else if (transactionInAndOut == 0) { // 출금
                        inOutValues.add(-transactionAmount.doubleValue());
                    } else {
                        log.warn("알 수 없는 transaction_in_and_out 값: {}", source);
                    }
                } else {
                    log.warn("transaction_amount 또는 transaction_in_and_out 누락: {}", source);
                }
            }
        } catch (Exception e) {
            log.error("Bank in/out 추출 중 오류 발생: {}", e.getMessage());
        }
        return inOutValues;
    }

    public List<String> extractFundTransactionTypes(Map<String, Object> fundData) {
        List<String> transactionTypes = new ArrayList<>();
        try {
            Map<String, Object> hits = (Map<String, Object>) fundData.get("hits");
            List<Map<String, Object>> hitList = (List<Map<String, Object>>) hits.get("hits");
    
            for (Map<String, Object> hit : hitList) {
                Map<String, Object> source = (Map<String, Object>) hit.get("_source");
                String transactionType = (String) source.get("fund_transaction_type");
                if (transactionType != null) {
                    transactionTypes.add(transactionType);
                }
            }
        } catch (Exception e) {
            log.error("Fund transaction types 추출 중 오류 발생: {}", e.getMessage());
        }
        return transactionTypes;
    }
    
    public List<Double> extractFundAmounts(Map<String, Object> fundData) {
        List<Double> amounts = new ArrayList<>();
        try {
            Map<String, Object> hits = (Map<String, Object>) fundData.get("hits");
            List<Map<String, Object>> hitList = (List<Map<String, Object>>) hits.get("hits");
    
            for (Map<String, Object> hit : hitList) {
                Map<String, Object> source = (Map<String, Object>) hit.get("_source");
                Number fundAmount = (Number) source.get("fund_amount");
                if (fundAmount != null) {
                    amounts.add(fundAmount.doubleValue());
                }
            }
        } catch (Exception e) {
            log.error("Fund amounts 추출 중 오류 발생: {}", e.getMessage());
        }
        return amounts;
    }

    public Map<String, double[]> extractMonthlyBankInOut(Map<String, Object> bankData) {
        Map<String, double[]> monthlyInOut = new HashMap<>();
        try {
            Map<String, Object> hits = (Map<String, Object>) bankData.get("hits");
            List<Map<String, Object>> hitList = (List<Map<String, Object>>) hits.get("hits");

            for (Map<String, Object> hit : hitList) {
                Map<String, Object> source = (Map<String, Object>) hit.get("_source");
                String transactionDate = (String) source.get("transaction_date");
                Integer inOut = (Integer) source.get("transaction_in_and_out");
                Number amount = (Number) source.get("transaction_amount");

                if (transactionDate != null && inOut != null && amount != null) {
                    String month = transactionDate.substring(0, 7); // "YYYY-MM"
                    double[] values = monthlyInOut.getOrDefault(month, new double[]{0, 0});
                    if (inOut == 1) {
                        values[0] += amount.doubleValue(); // 입금
                    } else {
                        values[1] += amount.doubleValue(); // 출금
                    }
                    monthlyInOut.put(month, values);
                }
            }
        } catch (Exception e) {
            log.error("Bank monthly in/out 추출 중 오류 발생: {}", e.getMessage());
        }
        return monthlyInOut;
    }

    public List<TransactionTimelineData> extractAssetTimeline(Map<String, Object> fundData, Map<String, Object> bankData) {
        List<TransactionTimelineData> timelineData = new ArrayList<>();
        try {
            // Fund 데이터 처리
            Map<String, Object> fundHits = (Map<String, Object>) fundData.get("hits");
            List<Map<String, Object>> fundHitList = (List<Map<String, Object>>) fundHits.get("hits");
            for (Map<String, Object> hit : fundHitList) {
                Map<String, Object> source = (Map<String, Object>) hit.get("_source");
                String transactionDate = (String) source.get("transaction_date");
                Number amount = (Number) source.get("fund_amount");
                if (transactionDate != null && amount != null) {
                    timelineData.add(new TransactionTimelineData(transactionDate, amount.doubleValue(), "Fund"));
                }
            }
    
            // Bank 데이터 처리
            Map<String, Object> bankHits = (Map<String, Object>) bankData.get("hits");
            List<Map<String, Object>> bankHitList = (List<Map<String, Object>>) bankHits.get("hits");
            for (Map<String, Object> hit : bankHitList) {
                Map<String, Object> source = (Map<String, Object>) hit.get("_source");
                String transactionDate = (String) source.get("transaction_date");
                Number amount = (Number) source.get("transaction_amount");
                Integer inOut = (Integer) source.get("transaction_in_and_out");
                if (transactionDate != null && amount != null && inOut != null) {
                    double adjustedAmount = inOut == 1 ? amount.doubleValue() : -amount.doubleValue();
                    timelineData.add(new TransactionTimelineData(transactionDate, adjustedAmount, "Bank"));
                }
            }
    
            // 날짜순 정렬
            timelineData.sort(Comparator.comparing(TransactionTimelineData::getDate));
    
        } catch (Exception e) {
            log.error("Asset timeline data 추출 중 오류 발생: {}", e.getMessage());
        }
        return timelineData;
    }
    
    public double extractLatestBankBalance(Map<String, Object> bankData) {
        double latestBalance = 0;
        try {
            Map<String, Object> hits = (Map<String, Object>) bankData.get("hits");
            List<Map<String, Object>> hitList = (List<Map<String, Object>>) hits.get("hits");
    
            for (Map<String, Object> hit : hitList) {
                Map<String, Object> source = (Map<String, Object>) hit.get("_source");
                Number balance = (Number) source.get("transaction_after_balance_amount");
                if (balance != null) {
                    latestBalance = Math.max(latestBalance, balance.doubleValue());
                }
            }
        } catch (Exception e) {
            log.error("Latest bank balance 추출 중 오류 발생: {}", e.getMessage());
        }
        return latestBalance;
    }
    
    // Loan Repayment Summary 데이터 추출
    public Map<String, Double> extractLoanRepaymentSummary(Map<String, Object> loanData) {
        Map<String, Double> repaymentSummary = new HashMap<>();
        try {
            Map<String, Object> hits = (Map<String, Object>) loanData.get("hits");
            List<Map<String, Object>> hitList = (List<Map<String, Object>>) hits.get("hits");

            for (Map<String, Object> hit : hitList) {
                Map<String, Object> source = (Map<String, Object>) hit.get("_source");
                String repaymentCode = (String) source.get("repayment_code");
                Number repaymentAmount = (Number) source.get("loan_contract_amount");

                if (repaymentCode != null && repaymentAmount != null) {
                    repaymentSummary.merge(repaymentCode, repaymentAmount.doubleValue(), Double::sum);
                }
            }
        } catch (Exception e) {
            log.error("Loan Repayment Summary 데이터 추출 중 오류 발생: {}", e.getMessage());
        }
        return repaymentSummary;
    }

    // Loan Status 데이터 추출
    public Map<String, Double> extractLoanStatusData(Map<String, Object> loanData) {
        Map<String, Double> loanStatusData = new HashMap<>();
        try {
            Map<String, Object> hits = (Map<String, Object>) loanData.get("hits");
            List<Map<String, Object>> hitList = (List<Map<String, Object>>) hits.get("hits");

            for (Map<String, Object> hit : hitList) {
                Map<String, Object> source = (Map<String, Object>) hit.get("_source");
                String loanStatus = (String) source.get("loan_status");
                Number loanAmount = (Number) source.get("loan_contract_amount");

                if (loanStatus != null && loanAmount != null) {
                    loanStatusData.merge(loanStatus, loanAmount.doubleValue(), Double::sum);
                }
            }
        } catch (Exception e) {
            log.error("Loan Status 데이터 추출 중 오류 발생: {}", e.getMessage());
        }
        return loanStatusData;
    }

    // Fund Transaction Type 데이터 추출
    public Map<String, Double> extractFundTransactionTypeData(Map<String, Object> fundData) {
        Map<String, Double> fundTransactionTypeData = new HashMap<>();
        try {
            Map<String, Object> hits = (Map<String, Object>) fundData.get("hits");
            List<Map<String, Object>> hitList = (List<Map<String, Object>>) hits.get("hits");

            for (Map<String, Object> hit : hitList) {
                Map<String, Object> source = (Map<String, Object>) hit.get("_source");
                String transactionType = (String) source.get("fund_transaction_type");
                Number fundAmount = (Number) source.get("fund_amount");

                if (transactionType != null && fundAmount != null) {
                    fundTransactionTypeData.merge(transactionType, fundAmount.doubleValue(), Double::sum);
                }
            }
        } catch (Exception e) {
            log.error("Fund Transaction Type 데이터 추출 중 오류 발생: {}", e.getMessage());
        }
        return fundTransactionTypeData;
    }

    public List<CardTransactionData> extractCardTransactionData(Map<String, Object> cardData) {
        List<CardTransactionData> transactionDataList = new ArrayList<>();

        List<Map<String, Object>> hits = (List<Map<String, Object>>) ((Map<String, Object>) cardData.get("hits")).get("hits");
        for (Map<String, Object> hit : hits) {
            Map<String, Object> source = (Map<String, Object>) hit.get("_source");
            CardTransactionData transactionData = new CardTransactionData();
            transactionData.setDate((String) source.get("transaction_date"));
            transactionData.setAmount(((Number) source.get("transaction_amount")).doubleValue());
            transactionData.setType((String) source.get("transaction_type"));

            transactionDataList.add(transactionData);
        }

        return transactionDataList;
    }
    
    public List<MonthlyBankInOutData> extractMonthlyBankInOutAsList(Map<String, Object> bankData) {
        List<MonthlyBankInOutData> monthlyBankInOutList = new ArrayList<>();
        try {
            Map<String, double[]> monthlyBankInOut = extractMonthlyBankInOut(bankData);
    
            for (Map.Entry<String, double[]> entry : monthlyBankInOut.entrySet()) {
                String month = entry.getKey();
                double[] values = entry.getValue();
    
                // DTO로 변환하여 리스트에 추가
                MonthlyBankInOutData monthlyData = new MonthlyBankInOutData(month, values[0], values[1]);
                monthlyBankInOutList.add(monthlyData);
            }
        } catch (Exception e) {
            log.error("Monthly bank in/out data 추출 중 오류 발생: {}", e.getMessage());
        }
        return monthlyBankInOutList;
    }
    
    
    public List<LoanTransactionData> extractLoanTransactionData(Map<String, Object> loanData) {
        List<LoanTransactionData> loanTransactionDataList = new ArrayList<>();
        try {
            Map<String, Object> hits = (Map<String, Object>) loanData.get("hits");
            List<Map<String, Object>> hitList = (List<Map<String, Object>>) hits.get("hits");
    
            for (Map<String, Object> hit : hitList) {
                Map<String, Object> source = (Map<String, Object>) hit.get("_source");
                String startDate = (String) source.get("contract_start_date");
                String endDate = (String) source.get("contract_end_date");
                Number contractAmount = (Number) source.get("loan_contract_amount");
                String loanStatus = (String) source.get("loan_status");
    
                if (startDate != null && endDate != null && contractAmount != null && loanStatus != null) {
                    String status = loanStatus.equals("A") ? "Active" : "Inactive";
                    LoanTransactionData loanTransaction = new LoanTransactionData(
                        startDate,
                        endDate,
                        contractAmount.doubleValue(),
                        status
                    );
                    loanTransactionDataList.add(loanTransaction);
                }
            }
        } catch (Exception e) {
            log.error("Loan transaction data 추출 중 오류 발생: {}", e.getMessage());
        }
        return loanTransactionDataList;
    }
    
    /**
     * Extract Monthly Bank Inflow and Outflow Data as DTOs
     *
     * @param bankData Map of bank transaction data
     * @return List of MonthlyBankInOutData
     */
    public List<MonthlyBankInOutData> extractMonthlyBankInOutData(Map<String, Object> bankData) {
        // 데이터를 Map에서 추출
        Map<String, double[]> monthlyBankInOut = extractMonthlyBankInOut(bankData);

        // DTO 리스트 생성
        return monthlyBankInOut.entrySet()
                .stream()
                .map(entry -> new MonthlyBankInOutData(entry.getKey(), entry.getValue()[0], entry.getValue()[1]))
                .collect(Collectors.toList());
    }

    /**
     * Extract Total Fund Amount
     *
     * @param fundData Map of fund transaction data
     * @return Total fund amount as a double
     */
    public double extractTotalFundAmount(Map<String, Object> fundData) {
        // 펀드 금액 리스트 추출 및 총합 계산
        return extractFundAmounts(fundData).stream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }

}
