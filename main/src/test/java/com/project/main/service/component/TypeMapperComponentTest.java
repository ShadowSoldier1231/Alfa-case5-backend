package com.project.main.service.component;

import com.project.main.enums.Difficulty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class TypeMapperComponentTest {

    private final TypeMapperComponent mapper = new TypeMapperComponent();

    @Test
    void escapeLikeWildcardsReturnsNullForNullInput() {
        assertThat(mapper.escapeLikeWildcards(null)).isNull();
    }

    @ParameterizedTest(name = "\"{0}\" -> \"{1}\"")
    @MethodSource("escapeCases")
    void escapeLikeWildcardsEscapesSqlWildcards(String input, String expected) {
        assertThat(mapper.escapeLikeWildcards(input)).isEqualTo(expected);
    }

    static Stream<Arguments> escapeCases() {
        return Stream.of(
                Arguments.of("моск", "моск"),          // обычные символы не трогаем
                Arguments.of("100%", "100!%"),          // SQL wildcard %
                Arguments.of("user_name", "user!_name"),// SQL wildcard _
                Arguments.of("!", "!!"),                // сам экранирующий символ
                Arguments.of("%_!", "!%!_!!")           // всё сразу (порядок замен важен!)
        );
    }

    @Test
    void parseDifficultyIsCaseInsensitiveAndTolerant() {
        assertThat(mapper.parseDifficulty("hard")).isEqualTo(Difficulty.HARD);
        assertThat(mapper.parseDifficulty(null)).isNull();
        assertThat(mapper.parseDifficulty("IMPOSSIBLE")).isNull();
    }
}