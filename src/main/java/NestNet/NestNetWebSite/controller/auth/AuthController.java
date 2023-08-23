package NestNet.NestNetWebSite.controller.auth;

import NestNet.NestNetWebSite.api.ApiResult;
import NestNet.NestNetWebSite.config.auth.CustomAuthorizationFilter;
import NestNet.NestNetWebSite.dto.request.LoginRequest;
import NestNet.NestNetWebSite.dto.request.RefreshtokenRequest;
import NestNet.NestNetWebSite.dto.request.SignUpRequest;
import NestNet.NestNetWebSite.dto.response.TokenResponse;
import NestNet.NestNetWebSite.service.auth.AuthService;
import NestNet.NestNetWebSite.service.token.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@RestController
@RequiredArgsConstructor
@Slf4j
//@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @Value("#{environment['jwt.refresh-exp-time']}")
    private long refreshTokenExpTime;              //리프레쉬 토큰 유효기간

    /*
    회원가입 post 요청
     */
    @Operation(summary = "회원가입 요청", description = "파라미터로 회원가입 폼 받음")
    @PostMapping("/auth/signup")
    public ApiResult<?> signUp(@Valid @RequestBody SignUpRequest signUpRequest, HttpServletResponse response){

        ApiResult<?> apiResult = authService.sendSignUpRequest(signUpRequest, response);

        return apiResult;
    }

    /*
    로그인 post 요청
     */
    @Operation(summary = "로그인 요청", description = "access 토큰은 헤더에 Authorization에, refresh 토큰은 헤더에 쿠키로 반환")
    @PostMapping("/auth/login")
    public ApiResult<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response){

        TokenResponse tokenResponse = authService.login(loginRequest);

        if(tokenResponse == null){
            return ApiResult.error(response, HttpStatus.BAD_REQUEST, "아이디 / 비밀번호 불일치");
        }

        // 현재 시간 + 만료 기간 == 만료 시간
        LocalDateTime expTime = Instant.now().plusMillis((long)refreshTokenExpTime).atZone(ZoneId.systemDefault()).toLocalDateTime();

        // 리프레시 토큰 저장
        refreshTokenService.save(new RefreshtokenRequest(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken(), expTime));

        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.set(CustomAuthorizationFilter.AUTHORIZATION_HEADER, "Bearer " + tokenResponse.getAccessToken());
        httpHeaders.set("refresh-token", tokenResponse.getRefreshToken());
        httpHeaders.set("refresh-token-exp-time", Integer.toString((int)refreshTokenExpTime / 1000));

        return ApiResult.success(httpHeaders);
    }

    /*
    로그아웃
     */
    @GetMapping("/auth/logout")
    public ApiResult<?> logout(HttpServletRequest request, HttpServletResponse response){

        return authService.logout(request, response);
    }

}
