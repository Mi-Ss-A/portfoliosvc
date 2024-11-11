package com.wibeechat.missa.dto.portfolio;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioResponse {

    private String portfolioUrl;  // 포트폴리오가 업로드된 S3 URL
    private String message;       // 오류 메시지 (예: 생성 실패 시)

    // 기본 생성자 오류 메시지 제공을 위해 설정
    public PortfolioResponse(String message) {
        this.message = message;
    }
}
