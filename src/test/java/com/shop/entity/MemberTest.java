package com.shop.entity;

import com.shop.repository.MemberRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Slf4j
class MemberTest {

    @Autowired
    private MemberRepository memberRepository;

    @PersistenceContext
    private EntityManager em;

    @Test
    @DisplayName("Auditing 테스트")
    @WithMockUser(username = "gildong", roles = "USER")
    public void auditingTest() {
        Member newMember = new Member();
        memberRepository.save(newMember);

        em.flush();
        em.clear();

        Member member = memberRepository.findById(newMember.getId()).orElseThrow(EntityExistsException::new);

        log.info("register time : {} ", member.getRegTime());
        log.info("update time : {} ", member.getUpdateTime());
        log.info("create member : {} ", member.getCreatedBy());
        log.info("modify member : {} ", member.getModifiedBy());
    }

}