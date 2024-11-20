package com.wibeechat.missa.dto.portfolio;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioResponse {

    private List<String> portfolioFiles;  // 포트폴리오 파일 리스트 (파일 리스트 조회 시 사용)
    private String portfolioUrl;          // 포트폴리오가 업로드된 S3 URL (포트폴리오 생성 시 사용)
    private String message;               // 응답 메시지 (성공/오류 메시지)

    // 포트폴리오 생성 성공 시 URL과 메시지를 설정하는 생성자
    public PortfolioResponse(String portfolioUrl, String message) {
        this.portfolioUrl = portfolioUrl;
        this.message = message;
    }

    // 오류 발생 시 메시지만 설정하는 생성자
    public PortfolioResponse(String message) {
        this.message = message;
    }
}
