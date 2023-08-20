package NestNet.NestNetWebSite.controller.manager;

import NestNet.NestNetWebSite.api.ApiResult;
import NestNet.NestNetWebSite.domain.member.MemberAuthority;
import NestNet.NestNetWebSite.dto.request.MemberChangeAuthorityRequestDto;
import NestNet.NestNetWebSite.dto.request.MemberSignUpManagementRequestDto;
import NestNet.NestNetWebSite.dto.response.MemberInfoDto;
import NestNet.NestNetWebSite.service.manager.ManagerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
//@RequestMapping("/manager")
public class ManagerController {

    private final ManagerService managerService;

    /*
    회원가입 요청 조회
     */
    @GetMapping("/manager/singup-request")
    public ApiResult<?> showSignUpRequests(){

        return managerService.findAllRequests();
    }

    /*
    회원가입 승인
     */
    @PostMapping("/manager/approve-signup")
    public ApiResult<?> approveSignUpMember(@Valid @RequestBody MemberSignUpManagementRequestDto dto){
        return managerService.approveSignUp(dto);
    }

    /*
    회원 권한 변경
     */
    @PostMapping("/manager/change-authority")
    public ApiResult<?> changeMemberAuthority(@Valid @RequestBody MemberChangeAuthorityRequestDto dto){

        return managerService.changeAuthority(dto.getId(), dto.getMemberAuthority());
    }

    /*
    회원 정보 조회
     */
    @GetMapping("/manager/member-info")
    public ApiResult<?> showMemberInfo(){

        return managerService.findAllMemberInfo();
    }
}
