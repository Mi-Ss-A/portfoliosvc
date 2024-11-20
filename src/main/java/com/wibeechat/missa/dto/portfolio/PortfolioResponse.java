package com.wibeechat.missa.dto.portfolio;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PortfolioResponse {

    private List<String> portfolioUrls; // 포트폴리오가 업로드된 S3 URL (포트폴리오 생성 시 사용)
    private String message; // 응답 메시지 (성공/오류 메시지)

    // 포트폴리오 생성 성공 시 URL과 메시지를 설정하는 생성자
    public PortfolioResponse(List<String> portfolioUrls, String message) {
        this.portfolioUrls = portfolioUrls;
        this.message = message;
    }

    // 오류 발생 시 메시지만 설정하는 생성자
    public PortfolioResponse(String Err, String message) {
        this.message = message;
    }
}
