package com.wibeechat.missa.controller.portfolio;

import com.wibeechat.missa.annotation.CurrentUser;
import com.wibeechat.missa.annotation.LoginRequired;
import com.wibeechat.missa.dto.portfolio.PortfolioRequest;
import com.wibeechat.missa.dto.portfolio.PortfolioResponse;
import com.wibeechat.missa.service.portfolio.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @Operation(summary = "포트폴리오 생성", description = "포트폴리오를 생성하고 로컬에 저장된 경로를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "포트폴리오 생성 성공"),
            @ApiResponse(responseCode = "400", description = "포트폴리오 생성 실패")
    })
    @LoginRequired
    @PostMapping
    public ResponseEntity<PortfolioResponse> createPortfolio(@CurrentUser String userNo, @RequestBody PortfolioRequest portfolioRequest) {
        try {
            log.info("포트폴리오 생성 요청: {}", portfolioRequest);

            // 포트폴리오 생성 후 로컬 파일 경로를 응답으로 반환
            PortfolioResponse response = portfolioService.generateAndSavePortfolio(
                    userNo,
                    portfolioRequest.getPortfolioData());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("포트폴리오 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new PortfolioResponse("Error", "포트폴리오 생성에 실패했습니다."));
        }
    }

    @GetMapping
    @LoginRequired
    public ResponseEntity<PortfolioResponse> getPortfolios(@CurrentUser String userNo) {
        try {
            log.info("포트폴리오 생성 요청");
            String userId = new String("5c06e46c5e47cfacad16ce1e37f17c09fdbc7072c567613e0b8112173f688a65");

            // 포트폴리오 생성 후 로컬 파일 경로를 응답으로 반환
            PortfolioResponse response = portfolioService.getPortfolioList(userNo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("포트폴리오 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new PortfolioResponse("Error", "포트폴리오 생성에 실패했습니다."));
        }
    }

}
