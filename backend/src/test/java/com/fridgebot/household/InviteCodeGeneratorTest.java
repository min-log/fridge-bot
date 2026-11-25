package com.fridgebot.household;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class InviteCodeGeneratorTest {

	InviteCodeGenerator generator = new InviteCodeGenerator();

	@Test
	void usesOnlyUnambiguousCharacters() {
		for (int i = 0; i < 1_000; i++) {
			String code = generator.generate();
			assertThat(code).hasSize(InviteCodeGenerator.LENGTH)
				.matches("[" + InviteCodeGenerator.ALPHABET + "]+")
				.doesNotContain("0", "O", "1", "I", "L");
		}
	}

	@Test
	void codesAreRandom() {
		Set<String> codes = new HashSet<>();
		for (int i = 0; i < 1_000; i++) {
			codes.add(generator.generate());
		}
		assertThat(codes).hasSize(1_000);
	}

}
