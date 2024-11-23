package com.wibeechat.missa.service.portfolio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.springframework.stereotype.Service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

@Service
public class PdfService {

    public File convertHtmlToPdf(String htmlContent, String userId, String portfolioData) throws IOException {
        String pdfFileName = "portfolios/" + userId + "_" + portfolioData + "_portfolio.pdf";

        // PDF 저장 디렉토리 확인 및 생성
        File pdfDir = new File("portfolios");
        if (!pdfDir.exists()) {
            pdfDir.mkdirs();
        }

        File pdfFile = new File(pdfFileName);
        try (OutputStream os = new FileOutputStream(pdfFile)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, pdfDir.toURI().toString());
            builder.toStream(os);
            builder.run();
        }
        return pdfFile;
    }
}
