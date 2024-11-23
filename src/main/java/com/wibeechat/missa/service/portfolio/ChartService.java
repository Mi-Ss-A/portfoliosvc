package com.wibeechat.missa.service.portfolio;

import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Service;

import com.wibeechat.missa.dto.portfolio.FundTransactionTypeData;
import com.wibeechat.missa.dto.portfolio.MonthlyBankInOutData;
import com.wibeechat.missa.dto.portfolio.TransactionTimelineData;

@Service
public class ChartService {

    // Updated Colors
    private static final Color BACKGROUND_COLOR = Color.WHITE;
    private static final Color PLOT_BACKGROUND_COLOR = new Color(240, 240, 240);
    private static final Color TITLE_COLOR = new Color(50, 50, 50);
    private static final Color GRIDLINE_COLOR = new Color(200, 200, 200);
    private static final Color SERIES_COLOR_1 = new Color(66, 133, 244); // Google Blue
    private static final Color SERIES_COLOR_2 = new Color(219, 68, 55); // Google Red
    private static final Color SERIES_COLOR_3 = new Color(244, 180, 0); // Google Yellow
    private static final Color SERIES_COLOR_4 = new Color(15, 157, 88); // Google Green

    // Apply Updated Style to a Chart
    @SuppressWarnings("rawtypes")
    private void applyCIStyle(JFreeChart chart) {
        chart.setBackgroundPaint(BACKGROUND_COLOR);
        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 16));
        chart.getTitle().setPaint(TITLE_COLOR);

        Plot plot = chart.getPlot();
        if (plot instanceof CategoryPlot) {
            CategoryPlot categoryPlot = (CategoryPlot) plot;
            categoryPlot.setBackgroundPaint(PLOT_BACKGROUND_COLOR);
            categoryPlot.setRangeGridlinePaint(GRIDLINE_COLOR);
            categoryPlot.setOutlineVisible(false);
            categoryPlot.getRenderer().setSeriesPaint(0, SERIES_COLOR_1);
            categoryPlot.getRenderer().setSeriesPaint(1, SERIES_COLOR_2);
            categoryPlot.getRenderer().setSeriesPaint(2, SERIES_COLOR_3);
            categoryPlot.getRenderer().setSeriesPaint(3, SERIES_COLOR_4);
        } else if (plot instanceof PiePlot) {
            PiePlot piePlot = (PiePlot) plot;
            piePlot.setBackgroundPaint(PLOT_BACKGROUND_COLOR);
            piePlot.setLabelFont(new Font("Arial", Font.PLAIN, 12));
            piePlot.setSectionPaint(0, SERIES_COLOR_1);
            piePlot.setSectionPaint(1, SERIES_COLOR_2);
            piePlot.setSectionPaint(2, SERIES_COLOR_3);
            piePlot.setSectionPaint(3, SERIES_COLOR_4);
        }
    }

    // Save chart as Base64
    private String saveChartAsBase64(JFreeChart chart, int width, int height) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ChartUtils.writeChartAsPNG(out, chart, width, height);
            byte[] chartBytes = out.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(chartBytes);
        }
    }

    // Generate a pie chart
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public String generatePieChart(String title, Map<String, Double> data) throws IOException {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data for pie chart must not be null or empty.");
        }

        DefaultPieDataset dataset = new DefaultPieDataset();
        data.forEach((key, value) -> {
            if (key == null || value == null) {
                throw new IllegalArgumentException("Pie chart data contains null values.");
            }
            dataset.setValue(key, value);
        });

        JFreeChart chart = ChartFactory.createPieChart(title, dataset, true, true, false);
        applyCIStyle(chart);

        return saveChartAsBase64(chart, 600, 400);
    }

    // Generate a timeline chart
    public String generateTimelineChart(List<TransactionTimelineData> timelineData) throws IOException {
        if (timelineData == null || timelineData.isEmpty()) {
            throw new IllegalArgumentException("Timeline data must not be null or empty.");
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        timelineData.forEach(data -> {
            if (data.getDate() == null || data.getAmount() == null || data.getType() == null) {
                throw new IllegalArgumentException("Timeline data contains null values.");
            }
            dataset.addValue(data.getAmount(), data.getType(), data.getDate());
        });

        JFreeChart chart = ChartFactory.createLineChart("Transaction Timeline", "Date", "Amount", dataset);
        applyCIStyle(chart);

        return saveChartAsBase64(chart, 800, 600);
    }

    // Generate monthly in/out chart
    public String generateMonthlyInOutChart(List<MonthlyBankInOutData> monthlyInOutData) throws IOException {
        if (monthlyInOutData == null || monthlyInOutData.isEmpty()) {
            throw new IllegalArgumentException("Monthly in/out data must not be null or empty.");
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (MonthlyBankInOutData data : monthlyInOutData) {
            if (data.getMonth() == null || data.getInflow() == null || data.getOutflow() == null) {
                throw new IllegalArgumentException("Monthly in/out data contains null values.");
            }
            dataset.addValue(data.getInflow(), "Inflow", data.getMonth());
            dataset.addValue(data.getOutflow(), "Outflow", data.getMonth());
        }

        JFreeChart chart = ChartFactory.createStackedBarChart("Monthly In/Out", "Month", "Amount", dataset);
        applyCIStyle(chart);

        return saveChartAsBase64(chart, 800, 600);
    }

    // Generate loan repayment summary chart
    public String generateRepaymentSummaryChart(Map<String, Double> repaymentSummary) throws IOException {
        if (repaymentSummary == null || repaymentSummary.isEmpty()) {
            throw new IllegalArgumentException("Repayment summary data must not be null or empty.");
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        repaymentSummary.forEach((key, value) -> {
            if (key == null || value == null) {
                throw new IllegalArgumentException("Repayment summary data contains null values.");
            }
            dataset.addValue(value, "Remaining Amount", key);
        });

        JFreeChart chart = ChartFactory.createBarChart("Loan Repayment Summary", "Repayment Code", "Amount", dataset);
        applyCIStyle(chart);

        return saveChartAsBase64(chart, 800, 600);
    }

    // Generate loan amount chart
    public String generateLoanAmountChart(List<String> dates, List<Double> loanAmounts) throws IOException {
        if (dates == null || loanAmounts == null || dates.size() != loanAmounts.size()) {
            throw new IllegalArgumentException("Dates and loan amounts must not be null and must have the same size.");
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < dates.size(); i++) {
            dataset.addValue(loanAmounts.get(i), "Loan Amount", dates.get(i));
        }

        JFreeChart chart = ChartFactory.createBarChart("Loan Amounts Over Time", "Date", "Loan Amount", dataset);
        applyCIStyle(chart);

        return saveChartAsBase64(chart, 800, 600);
    }

    // Generate generic amount chart
    public String generateAmountChart(List<String> dates, List<Double> amounts) throws IOException {
        if (dates == null || amounts == null || dates.size() != amounts.size()) {
            throw new IllegalArgumentException("Dates and amounts must not be null and must have the same size.");
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < dates.size(); i++) {
            dataset.addValue(amounts.get(i), "Amount", dates.get(i));
        }

        JFreeChart chart = ChartFactory.createLineChart("Transaction Amount Over Time", "Date", "Amount", dataset);
        applyCIStyle(chart);

        return saveChartAsBase64(chart, 800, 600);
    }

    // Generate asset trend chart
    public String generateAssetTrendChart(List<TransactionTimelineData> assetTrend) throws IOException {
        if (assetTrend == null || assetTrend.isEmpty()) {
            throw new IllegalArgumentException("Asset trend data must not be null or empty.");
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        assetTrend.forEach(data -> {
            if (data.getDate() == null || data.getAmount() == null) {
                throw new IllegalArgumentException("Asset trend data contains null values.");
            }
            dataset.addValue(data.getAmount(), "Total Assets", data.getDate());
        });

        JFreeChart chart = ChartFactory.createLineChart("Asset Trend", "Date", "Total Assets", dataset);
        applyCIStyle(chart);

        return saveChartAsBase64(chart, 800, 600);
    }

    // Generate fund transaction type breakdown chart
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public String generateFundTransactionTypeChart(List<FundTransactionTypeData> fundTransactionTypeData)
            throws IOException {
        if (fundTransactionTypeData == null || fundTransactionTypeData.isEmpty()) {
            throw new IllegalArgumentException("Fund transaction type data must not be null or empty.");
        }

        DefaultPieDataset dataset = new DefaultPieDataset();
        for (FundTransactionTypeData data : fundTransactionTypeData) {
            if (data.getTransactionType() == null || data.getTotalAmount() == null) {
                throw new IllegalArgumentException("Fund transaction type data contains null values.");
            }
            dataset.setValue(data.getTransactionType(), data.getTotalAmount());
        }

        JFreeChart chart = ChartFactory.createPieChart("Fund Transaction Type Breakdown", dataset, true, true, false);
        applyCIStyle(chart);

        return saveChartAsBase64(chart, 600, 400);
    }
}
