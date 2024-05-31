package com.example.loginapp.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public void 회원가입(String username, String password, String email) {
        User user = User.builder()
                .username(username)
                .password(password)
                .email(email)
                .build();
        userRepository.save(user);
    }

    public User 로그인(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("아이디가 없습니다");
        } else {
            if (user.getPassword().equals(password)) {
                return user;
            } else {
                throw new RuntimeException("비밀번호가 틀렸습니다");
            }
        }
    }

    public User 카카오로그인(String code) {
        String clientId = "e8c7ee8b96bccb1d6806c8946fbbb1c9";
        String redirectUrl = "http://localhost:8080/oauth/callback";

        ResponseEntity<KakaoResponse.TokenDTO> response = requestToken(code, clientId, redirectUrl);
        ResponseEntity<KakaoResponse.KakaoUserDTO> kakaoUserInfo = requestUserInfo(response.getBody().getAccessToken());


        // 3. 해당정보로 DB조회 (있을수, 없을수)
        String username = "kakao_" + kakaoUserInfo.getBody().getId();
        User userPS = userRepository.findByUsername(username);

        // 4. 있으면? - 조회된 유저정보 리턴
        if (userPS != null) {

            return userPS;

        } else {

            User user = User.builder()
                    .username(username)
                    .password(UUID.randomUUID().toString())
                    .email(kakaoUserInfo.getBody().getProperties().getNickname() + "@gmail.com")
                    .provider("kakao")
                    .build();
            User returnUser = userRepository.save(user);

            return returnUser;
        }
    }


    private ResponseEntity<KakaoResponse.TokenDTO> requestToken(String code, String cliendId, String redirectUrl) {
        // 1. code로 카카오에서 토큰 받기 (위임완료) - oauth2.0

        // 1.1 RestTemplate 설정
        // 1.2 http header 설정
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // 1.3 http body 설정
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", cliendId);
        body.add("redirect_uri", redirectUrl);
        body.add("code", code);

        // 1.4 body+header 객체 만들기
        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        // 1.5 api 요청하기 (토큰 받기)
        ResponseEntity<KakaoResponse.TokenDTO> response = restTemplate.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                request,
                KakaoResponse.TokenDTO.class);

        return response;
    }

    private ResponseEntity<KakaoResponse.KakaoUserDTO> requestUserInfo(String accessToken) {
        // 2. 토큰으로 사용자 정보 받기 (PK, Email)
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        headers.add("Authorization", "Bearer " + accessToken);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(headers);

        ResponseEntity<KakaoResponse.KakaoUserDTO> kakaoUserInfo = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                request,
                KakaoResponse.KakaoUserDTO.class);

        System.out.println("response2 : " + kakaoUserInfo.getBody().toString());

        return kakaoUserInfo;
    }
}