package com.shop.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


class MemberFormDtoTest {
    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void 이름이_비어있으면_에러발생() {
        // given
        MemberFormDto dto = new MemberFormDto();
        dto.setName("");
        dto.setEmail("test@test.com");
        dto.setPassword("12345678");
        dto.setCheckPassword("12345678");
        dto.setAddress("Seoul");

        // when
        Set<ConstraintViolation<MemberFormDto>> violations = validator.validate(dto);

        // then
        assertThat(violations).extracting("message")
                .contains("이름은 필수 입력 값입니다.");
    }

    @Test
    void 이메일_비어있으면_에러발생() {
        // given
        MemberFormDto dto = new MemberFormDto();
        dto.setName("홍길동");
        dto.setEmail("");
        dto.setPassword("12345678");
        dto.setCheckPassword("12345678");
        dto.setAddress("Seoul");

        // when
        Set<ConstraintViolation<MemberFormDto>> violations = validator.validate(dto);

        // then
        assertThat(violations).extracting("message")
                .contains("이메일은 필수 입력 값입니다.");
    }

    @Test
    void 이메일_형식안맞으면_에러발생() {
        // given
        MemberFormDto dto = new MemberFormDto();
        dto.setName("홍길동");
        dto.setEmail("testtest.com");
        dto.setPassword("12345678");
        dto.setCheckPassword("12345678");
        dto.setAddress("Seoul");

        // when
        Set<ConstraintViolation<MemberFormDto>> violations = validator.validate(dto);

        // then
        assertThat(violations).extracting("message")
                .contains("이메일 형식으로 입력해주세요");
    }

    @Test
    void 비밀번호_비어있으면_에러발생() {
        // given
        MemberFormDto dto = new MemberFormDto();
        dto.setEmail("test@test.com");
        dto.setPassword("");
        dto.setCheckPassword("2");
        dto.setAddress("Seoul");

        // when
        Set<ConstraintViolation<MemberFormDto>> violations = validator.validate(dto);

        // then
        assertThat(violations).extracting("message")
                .contains("비밀번호는 필수 입력 값입니다.");
    }

    @Test
    void 비밀번호와_확인비밀번호가_다르면_에러발생() {
        // given
        MemberFormDto dto = new MemberFormDto();
        dto.setName("홍길동");
        dto.setEmail("test@test.com");
        dto.setPassword("12345678");
        dto.setCheckPassword("87654321"); // 불일치
        dto.setAddress("Seoul");

        // when
        Set<ConstraintViolation<MemberFormDto>> violations = validator.validate(dto);

        // then
        assertThat(violations).extracting("message")
                .contains("비밀번호가 일치하지 않습니다.");
    }

    @Test
    void 주소_비었으면_오류() {
        // given
        MemberFormDto dto = new MemberFormDto();
        dto.setName("홍길동");
        dto.setEmail("test@test.com");
        dto.setPassword("12345678");
        dto.setCheckPassword("12345678");
        dto.setAddress("");

        // when
        Set<ConstraintViolation<MemberFormDto>> violations = validator.validate(dto);

        // then
        assertThat(violations).extracting("message")
                .contains("주소는 필수 입력 값입니다.");
    }


    @Test
    void 모든값이_정상입력되면_검증통과() {
        // given
        MemberFormDto dto = new MemberFormDto();
        dto.setName("홍길동");
        dto.setEmail("test@test.com");
        dto.setPassword("12345678");
        dto.setCheckPassword("12345678");
        dto.setAddress("Seoul");

        // when
        Set<ConstraintViolation<MemberFormDto>> violations = validator.validate(dto);

        // then
        assertThat(violations).isEmpty();
    }

}