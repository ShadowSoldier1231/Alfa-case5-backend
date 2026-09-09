package com.project.main.service.component;

import com.project.main.enums.ValidPasswordStatus;
import com.project.main.enums.ValidUsernameStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class UserValidationServiceTest {

    private final UserValidationService service = new UserValidationService();

    @ParameterizedTest(name = "password \"{0}\" -> {1}")
    @MethodSource("passwordCases")
    void checkPasswordReturnsExpectedStatus(String password, ValidPasswordStatus expected) {
        assertThat(service.checkPassword(password)).isEqualTo(expected);
    }

    static Stream<Arguments> passwordCases() {
        return Stream.of(
                // класс "пустые"
                Arguments.of(null, ValidPasswordStatus.EMPTY),
                Arguments.of("   ", ValidPasswordStatus.EMPTY),
                // границы длины: 7/8 и 30/31
                Arguments.of("a1!aaaa", ValidPasswordStatus.TOO_SHORT),            // 7
                Arguments.of("tea_tea1", ValidPasswordStatus.OK),                  // 8
                Arguments.of("a1!" + "a".repeat(27), ValidPasswordStatus.OK),      // 30
                Arguments.of("a1!" + "a".repeat(28), ValidPasswordStatus.TOO_LONG),// 31
                // содержание
                Arguments.of("password1", ValidPasswordStatus.NO_SPECIAL_SYMBOL),
                Arguments.of("password!", ValidPasswordStatus.NO_DIGITS)
        );
    }

    @ParameterizedTest(name = "username \"{0}\" -> {1}")
    @MethodSource("usernameCases")
    void checkUsernameReturnsExpectedStatus(String username, ValidUsernameStatus expected) {
        assertThat(service.checkUsername(username)).isEqualTo(expected);
    }

    static Stream<Arguments> usernameCases() {
        return Stream.of(
                Arguments.of(null, ValidUsernameStatus.EMPTY),
                Arguments.of("ab", ValidUsernameStatus.TOO_SHORT),           // 2
                Arguments.of("abc", ValidUsernameStatus.OK),                 // 3 (граница)
                Arguments.of("a".repeat(20), ValidUsernameStatus.OK),        // 20 (граница)
                Arguments.of("a".repeat(21), ValidUsernameStatus.TOO_LONG),  // 21
                Arguments.of("user name", ValidUsernameStatus.SPACE)
        );
    }
}