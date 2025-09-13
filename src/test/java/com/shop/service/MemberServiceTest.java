package com.shop.service;

import com.shop.dto.MemberFormDto;
import com.shop.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Member createMember(){
        MemberFormDto memberFormDto = new MemberFormDto();
        memberFormDto.setEmail("test@email.com");
        memberFormDto.setName("홍길동");
        memberFormDto.setAddress("서울시 마포구 합정동");
        memberFormDto.setPassword("1234");
        return Member.createMember(memberFormDto, passwordEncoder);
    }

    @Test
    @DisplayName("회원가입 테스트")
    public void saveMemberTest(){
        Member member = createMember();
        Member savedMember = memberService.saveMember(member);

        assertEquals(savedMember.getEmail(), member.getEmail());
        assertEquals(savedMember.getName(), member.getName());
        assertEquals(savedMember.getAddress(), member.getAddress());
        assertEquals(savedMember.getPassword(), member.getPassword());
        assertEquals(savedMember.getRole(), member.getRole());
    }

    @Test
    @DisplayName("중복 회원 가입 테스트")
    public void saveDuplicateMemberTest() {
        Member firstMember = createMember();
        Member secondMember = createMember();

        memberService.saveMember(firstMember);

        Exception e = assertThrows(IllegalStateException.class, () -> memberService.saveMember(secondMember));

        assertEquals("이미 가입된 회원입니다.", e.getMessage());
    }
}
