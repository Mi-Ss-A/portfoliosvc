package com.wibeechat.missa.dto.portfolio;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PortfolioRequest {
    private String period; // 포트폴리오 생성에 필요한 데이터 (예: 포트폴리오 내용)
    private String redisSessionId;
}
