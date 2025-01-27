package NestNet.NestNetWebSite.controller.member;

import NestNet.NestNetWebSite.api.ApiResult;
import NestNet.NestNetWebSite.config.cookie.CookieManager;
import NestNet.NestNetWebSite.dto.response.MemberIdDto;
import NestNet.NestNetWebSite.dto.request.*;
import NestNet.NestNetWebSite.dto.response.TokenDto;
import NestNet.NestNetWebSite.dto.response.member.TemporaryInfoDto;
import NestNet.NestNetWebSite.service.auth.AuthService;
import NestNet.NestNetWebSite.service.mail.MailService;
import NestNet.NestNetWebSite.service.member.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "회원 API")
public class MemberController {

    private final MailService mailService;
    private final MemberService memberService;
    private final AuthService authService;

    private final CookieManager cookieManager;

    /*
    회원 정보 수정
     */
    @PostMapping("/member/modify-info")
    @Operation(summary = "회원 단건 정보 수정", description = "로그인한 회원이 자신의 정보를 수정한다.")
    public ApiResult<?> modifyMemberInfo(@Valid @RequestBody MemberModifyInfoRequest memberModifyInfoRequest,
                                         @AuthenticationPrincipal UserDetails userDetails,
                                         HttpServletRequest request, HttpServletResponse response){

        MemberIdDto dto = memberService.modifyMemberInfo(memberModifyInfoRequest, userDetails.getUsername());

        // 새로운 인증 정보 발급
        TokenDto tokenDto = authService.setAuthenticationSecurityContext(dto.getLoginId(), request);

        cookieManager.setCookie("Authorization", tokenDto.getAccessToken(), false, response);
        cookieManager.setCookie("refresh-token", tokenDto.getRefreshToken(), false, response);

        return ApiResult.success("회원 정보가 수정되었습니다.");
    }

    /*
    회원 이름 + 이메일로 아이디 찾기 -> 이메일로 아이디 전송
     */
    @PostMapping("/member/find-id")
    @Operation(summary = "회원 아이디 찾기", description = "회원 이름 + 이메일(이미 등록된 이메일)로 아이디를 찾아서 이메일로 전송한다.", responses = {
            @ApiResponse(responseCode = "200", description = "회원 이메일 주소 + 에게 아이디를 전송하였습니다."),
            @ApiResponse(responseCode = "404", description = "일치하는 회원이 없습니다."),
            @ApiResponse(responseCode = "500", description = "서버에서 이메일 전송을 실패하였습니다. 관리자에게 문의하세요")
    })
    public ApiResult<?> findMemberId(@Valid @RequestBody MemberFindIdRequest memberFindIdRequest){

        String memberLoginId = memberService.findMemberId(memberFindIdRequest.getName(), memberFindIdRequest.getEmailAddress());

        return mailService.sendEmailLoginId(memberFindIdRequest.getEmailAddress(), memberLoginId);
    }

    /*
    회원 아이디로 임시비밀번호 발급받기 -> 이메일로 임시 비밀번호 전송
    */
    @PostMapping("/member/get-temp-pw")
    @Operation(summary = "임시 비밀번호 발급", description = "회원 아이디로 임시 비밀번호를 발급하고 이메일을 전송한다.", responses = {
            @ApiResponse(responseCode = "200", description = "회원 이메일 주소 + 에게 임시 비밀번호를 전송하였습니다."),
            @ApiResponse(responseCode = "404", description = "회원 아이디가 틀렸습니다."),
            @ApiResponse(responseCode = "500", description = "서버에서 이메일 전송을 실패하였습니다. 관리자에게 문의하세요")
    })
    public ApiResult<?> getTemporaryPassword(@Valid @RequestBody MemberGetTemporaryPwRequest dto){

        TemporaryInfoDto emailAndPw = memberService.createTemporaryPassword(dto.getLoginId());

        return mailService.sendEmailTemporaryPassword(emailAndPw.getEmail(), emailAndPw.getPassword());
    }

    /*
    회원 비밀번호 인증
     */
    @PostMapping("/member/check-pw")
    @Operation(summary = "회원 비밀번호 인증", description = "로그인한 회원이 자신의 비밀번호를 인증한다.(비밀번호 변경 전)")
    public ApiResult<?> checkPassword(@Valid @RequestBody MemberPasswordChangeRequest dto,
                                       @AuthenticationPrincipal UserDetails userDetails){

        return memberService.checkMemberPassword(userDetails.getUsername(), dto.getPassword());
    }

    /*
    회원 비밀번호 변경
     */
    @PostMapping("/member/change-pw")
    @Operation(summary = "회원 비밀번호 변경", description = "로그인한 회원이 자신의 비밀번호를 변경한다.")
    public ApiResult<?> changePassword(@Valid @RequestBody MemberPasswordChangeRequest dto,
                                       @AuthenticationPrincipal UserDetails userDetails){

        return memberService.changeMemberPassword(userDetails.getUsername(), dto.getPassword());
    }

    /*
    회원 탈퇴
     */
    @GetMapping("/member/withdraw")
    @Operation(summary = "회원 탈퇴", description = "로그인한 회원 본인이 직접 탈퇴한다.")
    public ApiResult<?> withdrawMember(@AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request){

        return memberService.withDrawMember(userDetails.getUsername(), request);
    }

}
