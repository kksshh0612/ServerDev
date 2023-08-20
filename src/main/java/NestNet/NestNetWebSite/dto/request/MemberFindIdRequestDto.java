package NestNet.NestNetWebSite.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberFindIdRequestDto {

    private String name;
    private String emailAddress;
}