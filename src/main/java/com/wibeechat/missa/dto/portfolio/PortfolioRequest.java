package com.wibeechat.missa.dto.portfolio;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioRequest {

    private String userId;  // 포트폴리오 생성 요청을 보낸 사용자 ID
    private String portfolioData;  // 포트폴리오 생성에 필요한 데이터 (예: 포트폴리오 내용)
}