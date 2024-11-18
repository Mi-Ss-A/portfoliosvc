package com.wibeechat.missa.service.portfolio;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class ChartService {

    private String createChart(String title, String xAxis, String yAxis, List<String> dates, List<Double> values, String fileName, boolean isBarChart) throws IOException {
        if (dates == null || dates.isEmpty() || values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Dates and values must not be null or empty");
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < dates.size(); i++) {
            dataset.addValue(values.get(i), title, dates.get(i));
        }

        JFreeChart chart = isBarChart ?
                ChartFactory.createBarChart(title, xAxis, yAxis, dataset) :
                ChartFactory.createLineChart(title, xAxis, yAxis, dataset);

        File chartDir = new File("charts");
        if (!chartDir.exists()) {
            chartDir.mkdirs();
        }
        File chartFile = new File(chartDir, fileName + "_" + System.currentTimeMillis() + ".png");
        ChartUtils.saveChartAsPNG(chartFile, chart, 600, 400);
        return chartFile.getAbsolutePath();
    }

    public String generateAmountChart(List<String> dates, List<Double> amounts) throws IOException {
        return createChart("Transaction Amount Over Time", "Date", "Amount", dates, amounts, "transaction_amount_chart", false);
    }

    public String generateInOutChart(List<String> dates, List<Double> inOutValues) throws IOException {
        return createChart("Transaction In/Out Over Time", "Date", "In/Out", dates, inOutValues, "transaction_inout_chart", true);
    }
}
