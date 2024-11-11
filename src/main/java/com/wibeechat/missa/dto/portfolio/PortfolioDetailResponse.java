package com.wibeechat.missa.dto.portfolio;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioDetailResponse {

    private String portfolioUrl;  // 생성된 포트폴리오 파일의 URL

    private String message;  // 오류 메시지 (예: 조회 실패 시)

    // 기본 생성자는 오류 메시지 사용을 위한 용도로 설정
    public PortfolioDetailResponse(String message) {
        this.message = message;
    }
}
