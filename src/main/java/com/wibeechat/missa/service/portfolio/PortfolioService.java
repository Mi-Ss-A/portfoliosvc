package com.wibeechat.missa.service.portfolio;

import com.wibeechat.missa.dto.portfolio.PortfolioDetailResponse;
import com.wibeechat.missa.dto.portfolio.PortfolioResponse;
import com.wibeechat.missa.dto.portfolio.PortfolioRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Service
public class PortfolioService {

    private static final String S3_BASE_URL = "https://s3.amazonaws.com/portfolio/";

    // 포트폴리오 생성 및 S3 업로드
    public PortfolioResponse generateAndUploadPortfolio(String userId) {
        try {
            // 포트폴리오 생성 날짜를 기반으로 파일명 설정
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String portfolioFileName = userId + "_portfolio_" + timestamp + ".pdf";

            // Kibana 대시보드를 기반으로 포트폴리오 데이터를 가져와 PDF 파일 생성 로직
            // 예시로 포트폴리오 파일을 생성한다고 가정
            String s3FileUrl = S3_BASE_URL + portfolioFileName;
            
            // 파일 업로드 로직 (여기서는 생략하고 URL만 반환)
            // 실제 구현 시 AWS SDK를 사용하여 파일을 S3에 업로드합니다.

            log.info("포트폴리오 파일 생성 및 S3 업로드 완료: {}", s3FileUrl);
            return new PortfolioResponse(s3FileUrl, "포트폴리오 생성 완료");
        } catch (Exception e) {
            log.error("포트폴리오 생성 중 오류 발생", e);
            return new PortfolioResponse("error", "포트폴리오 생성 실패");
        }
    }

    // 포트폴리오 조회
    public PortfolioDetailResponse getPortfolioDetails(String portfolioId) {
        try {
            // 포트폴리오 파일 조회 로직
            String s3FileUrl = S3_BASE_URL + portfolioId + ".pdf";

            // 예시로 URL을 반환
            return new PortfolioDetailResponse(s3FileUrl);
        } catch (Exception e) {
            log.error("포트폴리오 조회 중 오류 발생", e);
            return new PortfolioDetailResponse("포트폴리오 조회 실패");
        }
    }

    // 포트폴리오 리스트 조회
    public String getPortfolioList(String userId) {
        try {
            // 사용자가 생성한 포트폴리오 리스트 조회 로직
            // 예시로 2개의 포트폴리오 URL을 반환
            String portfolio1 = S3_BASE_URL + userId + "_portfolio_20231111_120000.pdf";
            String portfolio2 = S3_BASE_URL + userId + "_portfolio_20231111_130000.pdf";
            return portfolio1 + ", " + portfolio2;
        } catch (Exception e) {
            log.error("포트폴리오 리스트 조회 중 오류 발생", e);
            return "포트폴리오 리스트 조회 실패";
        }
    }
}
