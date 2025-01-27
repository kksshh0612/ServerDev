package NestNet.NestNetWebSite.config.security;

import NestNet.NestNetWebSite.config.auth.Authenticator;
import NestNet.NestNetWebSite.config.auth.CustomAuthorizationFilter;
import NestNet.NestNetWebSite.config.jwt.TokenProvider;
import NestNet.NestNetWebSite.config.jwt.errorHandler.JwtAccessDeniedHandler;
import NestNet.NestNetWebSite.config.jwt.errorHandler.JwtAuthenticationEntryPoint;
import NestNet.NestNetWebSite.config.redis.RedisUtil;
import NestNet.NestNetWebSite.repository.member.MemberRepository;
import NestNet.NestNetWebSite.service.auth.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenProvider tokenProvider;
    private final Authenticator authenticator;
    private final CorsFilter corsFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final MemberRepository memberRepository;
    private final RedisUtil redisUtil;

    /*
    비밀번호 암호화를 담당할 인코더 설정
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    /*
    loadUserByUsername 을 사용하여 UserDetails 객체를 가져올 수 있도록 하는 설정
    UserDetails는 시큐리티 컨텍스트에 사용자 정보를 담는 인터페이스
     */
    @Bean
    public UserDetailsService userDetailsService(){
        return new CustomUserDetailsService(memberRepository);
    }

    /*
    커스텀 필터
     */
    @Bean
    public CustomAuthorizationFilter customAuthorizationFilter(){
        return new CustomAuthorizationFilter(tokenProvider, authenticator, redisUtil);
    }


    @Bean
    public DaoAuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){
        return web -> web
                .ignoring()
                .requestMatchers("/swagger-ui/index.html", "/swagger-ui/**", "/v3/api-docs/**")
                .requestMatchers("/auth/signup", "/auth/login", "/auth/mail-auth", "/auth/mail-auth-answer")
                .requestMatchers("/member/find-id", "/member/get-temp-pw", "/member/find")
                .requestMatchers("/attendance/statistics")
                .requestMatchers("/life4cut/**")
                .requestMatchers("/post/recent-posts")
                .requestMatchers("/manager/**")
                .requestMatchers("/file/**")
                .requestMatchers("/forbidden", "/unauthorized")
                .requestMatchers("/photo-post/**")
                .requestMatchers("/image/**");
    }

    /*
    스프링 시큐리티 구성을 정의하는 필터체인 구성
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                //API 통신을 하는 애플리케이션의 경우 csrf 공격을 받을 가능성이 없기 때문에 @EnableWebSecurity의 csrf 보호 기능을 해제
                .csrf(csrf -> csrf.disable())

                //jwt를 사용하기 때문에 세션 사용하지 않음
                .sessionManagement((sessionManagement) -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                //각 예외 인터페이스를 커스텀한 두 예외 등록. 401, 403 에러
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )

                //http 요청에 대한 접근 권한을 설정
                //로그인, 회원가입 api는 토큰이 없는 상태로 요청이 들어오기 때문에 permitAll()로 열어줌
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()        //html, css같은 정적 리소스에 대해 접근 허용
                        .requestMatchers(HttpMethod.GET, "/image/**").permitAll()
                        .requestMatchers("/auth/signup", "auth/login").permitAll()                      //로그인, 회원가입 접근 허용
                        // 관리자 패이지는 관리자만 접근 가능
                        .requestMatchers("/manager/**").hasAnyAuthority("MANAGER", "ADMIN")                  //manager하위 리소스는 MANAGER 권한으로 허용
                        // 게시판 통합 기능은 모든 회원 접근 가능
                        .requestMatchers("/post/**").hasAnyAuthority("ADMIN", "PRESIDENT", "VICE_PRESIDENT", "MANAGER", "GENERAL_MEMBER", "ON_LEAVE_MEMBER", "GRADUATED_MEMBER",  "WITHDRAWN_MEMBER")
                        // 통합 게시판은 모든 회원 접근 가능
                        .requestMatchers("/unified-post/**").hasAnyAuthority("ADMIN", "PRESIDENT", "VICE_PRESIDENT", "MANAGER", "GENERAL_MEMBER", "ON_LEAVE_MEMBER", "GRADUATED_MEMBER", "WITHDRAWN_MEMBER")
                        // 족보 게시판은 졸업생을 제외한 모든 회원 접근 가능
                        .requestMatchers("/exam-collection-post/**").hasAnyAuthority("ADMIN", "PRESIDENT", "VICE_PRESIDENT", "MANAGER", "GENERAL_MEMBER", "ON_LEAVE_MEMBER", "WITHDRAWN_MEMBER")
                        // 사진 게시판 리스트 조회는 권한 필요없음 / 사진 게시판 작성은 회장, 부회장, 관리자 접근 가능 / 그 외 조회는 모든 회원 접근 가능
                        .requestMatchers("/photo-post/post").hasAnyAuthority("PRESIDENT", "VICE_PRESIDENT", "MANAGER", "ADMIN")
                        .requestMatchers("/photo-post/**").hasAnyAuthority("ADMIN", "PRESIDENT", "VICE_PRESIDENT", "MANAGER", "GENERAL_MEMBER", "ON_LEAVE_MEMBER", "GRADUATED_MEMBER", "WITHDRAWN_MEMBER")
                        // 인생네컷
                        .requestMatchers("/life4cut/save").hasAnyAuthority("PRESIDENT", "VICE_PRESIDENT", "MANAGER")
//                        .requestMatchers("/life4cut/**").permitAll()
                        // 멤버 프로필
                        .requestMatchers("/member-profile/member-info/**").hasAnyAuthority("ADMIN", "PRESIDENT", "VICE_PRESIDENT", "MANAGER", "GENERAL_MEMBER", "ON_LEAVE_MEMBER", "GRADUATED_MEMBER", "WITHDRAWN_MEMBER")
                        .requestMatchers("/member/**").hasAnyAuthority("ADMIN", "PRESIDENT", "VICE_PRESIDENT", "MANAGER", "GENERAL_MEMBER", "ON_LEAVE_MEMBER", "GRADUATED_MEMBER", "WITHDRAWN_MEMBER")
                        // 자기소개 게시판
                        .requestMatchers("/introduction-post/**").hasAnyAuthority("ADMIN", "PRESIDENT", "VICE_PRESIDENT", "MANAGER", "GENERAL_MEMBER", "ON_LEAVE_MEMBER", "GRADUATED_MEMBER", "WITHDRAWN_MEMBER")
                        .anyRequest().authenticated()       //나머지 요청은 모두 권한 필요함.

                )

                // 헤더 관련 설정
                .headers(headers ->
                        headers.frameOptions(options ->
                                options.sameOrigin()            //x-frame-option 설정

                        )
                );

        http
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(customAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


}
