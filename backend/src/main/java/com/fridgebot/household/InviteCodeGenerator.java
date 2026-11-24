package com.fridgebot.household;

import com.fridgebot.common.SecureRandomStrings;
import org.springframework.stereotype.Component;

/** 말로 불러 주기 쉽게 헷갈리는 글자(0/O, 1/I/L)를 뺀 8자리 코드. 32^8 ≈ 1조 가지. */
@Component
public class InviteCodeGenerator {

	static final String ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";

	static final int LENGTH = 8;

	public String generate() {
		return SecureRandomStrings.fromAlphabet(ALPHABET, LENGTH);
	}

}
