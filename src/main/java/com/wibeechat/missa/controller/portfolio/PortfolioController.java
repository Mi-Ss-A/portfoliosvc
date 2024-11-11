package com.wibeechat.missa.controller.portfolio;

import com.wibeechat.missa.dto.portfolio.PortfolioRequest;
import com.wibeechat.missa.dto.portfolio.PortfolioResponse;
import com.wibeechat.missa.dto.portfolio.PortfolioDetailResponse;
import com.wibeechat.missa.service.portfolio.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @Operation(summary = "포트폴리오 생성", description = "포트폴리오를 생성하고 S3에 업로드합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "포트폴리오 생성 성공"),
            @ApiResponse(responseCode = "400", description = "포트폴리오 생성 실패")
    })
    @PostMapping
    public ResponseEntity<PortfolioResponse> createPortfolio(@RequestBody PortfolioRequest portfolioRequest) {
        try {
            log.info("포트폴리오 생성 요청: {}", portfolioRequest);

            // 포트폴리오 업로드 성공 시 S3 URL을 응답으로 반환
            PortfolioResponse response = portfolioService.generateAndUploadPortfolio(portfolioRequest.getUserId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("포트폴리오 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new PortfolioResponse("포트폴리오 생성에 실패했습니다."));
        }
    }

    @Operation(summary = "포트폴리오 조회", description = "포트폴리오 ID에 해당하는 포트폴리오를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "포트폴리오 조회 성공"),
            @ApiResponse(responseCode = "404", description = "포트폴리오를 찾을 수 없음")
    })
    @GetMapping("/{portfolioId}")
    public ResponseEntity<PortfolioDetailResponse> getPortfolio(@PathVariable String portfolioId) {
        try {
            log.info("포트폴리오 조회 요청: {}", portfolioId);

            // 포트폴리오 조회
            PortfolioDetailResponse portfolioDetail = portfolioService.getPortfolioDetails(portfolioId);

            // 조회한 포트폴리오 응답
            return ResponseEntity.ok(portfolioDetail);
        } catch (Exception e) {
            log.error("포트폴리오 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new PortfolioDetailResponse("포트폴리오 조회에 실패했습니다."));
        }
    }

    @Operation(summary = "포트폴리오 리스트 조회", description = "사용자가 생성한 모든 포트폴리오 리스트를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "포트폴리오 리스트 조회 성공"),
            @ApiResponse(responseCode = "404", description = "포트폴리오 리스트를 찾을 수 없음")
    })
    @GetMapping("/list")
    public ResponseEntity<PortfolioResponse> getPortfolioList(@RequestParam String userId) {
        try {
            log.info("포트폴리오 리스트 조회 요청: {}", userId);

            // 사용자에 해당하는 포트폴리오 리스트 조회
            String portfolioList = portfolioService.getPortfolioList(userId);

            // 조회한 포트폴리오 리스트 응답
            return ResponseEntity.ok(new PortfolioResponse(portfolioList));
        } catch (Exception e) {
            log.error("포트폴리오 리스트 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new PortfolioResponse("포트폴리오 리스트 조회에 실패했습니다."));
        }
    }
}
