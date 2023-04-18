package me.jaeyeon.blog.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.lang.reflect.Field;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import me.jaeyeon.blog.config.WebConfig;
import me.jaeyeon.blog.dto.MemberRegistrationReq;
import me.jaeyeon.blog.dto.MemberRegistrationRes;
import me.jaeyeon.blog.dto.MemberSignIn;
import me.jaeyeon.blog.exception.BlogApiException;
import me.jaeyeon.blog.exception.EmailAlreadyExistsException;
import me.jaeyeon.blog.exception.ErrorCode;
import me.jaeyeon.blog.model.Member;
import me.jaeyeon.blog.repository.MemberRepository;
import me.jaeyeon.blog.service.MemberService;

@Import(WebConfig.class)
@WebMvcTest(MemberController.class)
@MockBean(JpaMetamodelMappingContext.class)
class MemberControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@MockBean
	private MemberService memberService;

	@Test
	@DisplayName("회원 가입 API 테스트 - 성공")
	void registerMemberTest() throws Exception {
		// given
		MemberRegistrationReq request = new MemberRegistrationReq();
		request.setUserName("test");
		request.setEmail("test@email.com");
		request.setPassword("P@ssw0rd!");

		given(memberService.register(request)).willReturn(null);

		// when
		mockMvc.perform(post("/members/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isCreated());
	}


	// 유효하지 않은 회원 가입 요청에 대한 테스트 코드를 작성하십시오.

	@Test
	@DisplayName("회원 가입 API 테스트 - 실패 (이메일 중복)")
	void registerMemberTest_Fail() throws Exception {
		// given
		MemberRegistrationReq request = new MemberRegistrationReq();
		request.setUserName("test");
		request.setEmail("test@email.com");
		request.setPassword("P@ssw0rd!");

		given(memberService.register(request)).willThrow(new EmailAlreadyExistsException(ErrorCode.EMAIL_ALREADY_EXISTS));

		// when
		mockMvc.perform(post("/members/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errorMessage", containsString(ErrorCode.EMAIL_ALREADY_EXISTS.getMessage())));
	}

	@Test
	@DisplayName("로그인 성공")
	void signInSuccess() throws Exception {
		// given
		Member member = createMember(1L, "test", "test@email.com", "P@ssw0rd!");
		MemberSignIn signIn = new MemberSignIn("test@email.com", "P@ssw0rd!");
		given(memberService.signIn(signIn)).willReturn(member);

		// when
		mockMvc.perform(post("/members/sign-in")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signIn)))
			.andDo(print())
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("로그인 실패")
	void signInFailure() throws Exception {
		// given
		MemberSignIn signIn = new MemberSignIn("nonexistent@email.com", "wrong_password");
		given(memberService.signIn(signIn)).willThrow(new BlogApiException(ErrorCode.MEMBER_NOT_FOUND));

		// when
		mockMvc.perform(post("/members/sign-in")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signIn)))
			.andDo(print())
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errorCode", is("M-002")))
			.andExpect(jsonPath("$.errorMessage", is("회원을 찾을 수 없습니다.")));
	}


	private Member createMember(Long id, String userName, String email, String password) {
		String encodedPassword = passwordEncoder.encode(password);

		Member member = Member.builder()
			.userName(userName)
			.email(email)
			.password(encodedPassword)
			.build();

		// Use reflection to set the id field
		try {
			Field idField = Member.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(member, id);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return member;
	}
}
