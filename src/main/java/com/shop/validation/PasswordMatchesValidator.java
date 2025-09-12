package com.shop.validation;

import com.shop.dto.MemberFormDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, MemberFormDto> {

    @Override
    public boolean isValid(MemberFormDto dto, ConstraintValidatorContext context) {
        if (dto.getPassword() == null || dto.getCheckPassword() == null) {
            return false; // 둘 중 하나라도 null이면 false
        }
        return dto.getPassword().equals(dto.getCheckPassword());
    }
}
