package com.project.main.service.component;

import com.project.main.enums.Difficulty;
import com.project.main.enums.GenderCode;
import com.project.main.enums.UserStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeParseException;

@Component
public class TypeMapperComponent {

    private static final Logger logger = LoggerFactory.getLogger(TypeMapperComponent.class);

    public String escapeLikeWildcards(String value) {
        if (value == null) {
            return null;
        }

        return value
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_");
    }

    public Boolean toBoolean(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Boolean b) {
            return b;
        }

        if (value instanceof Number n) {
            return n.intValue() != 0;
        }

        String s = value.toString().trim().toLowerCase();

        return switch (s) {
            case "true", "t", "1", "yes", "y" -> true;
            case "false", "f", "0", "no", "n" -> false;
            default -> null;
        };
    }

    public Instant parseTimeToInstant(String timeStr) {
        if (timeStr == null || timeStr.isBlank()) {
            return null;
        }

        try {
            return Instant.parse(timeStr);
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalDateTime.parse(timeStr).atZone(ZoneOffset.UTC).toInstant();
        } catch (Exception e) {
            logger.error("Error when parsing Instant: {}", timeStr, e);
            return null;
        }
    }

    public LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof LocalDateTime ldt) {
            return ldt;
        }

        if (value instanceof java.sql.Timestamp ts) {
            return ts.toLocalDateTime();
        }

        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate().atStartOfDay();
        }

        if (value instanceof OffsetDateTime odt) {
            return odt.toLocalDateTime();
        }

        if (value instanceof Instant instant) {
            return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        }

        if (value instanceof java.util.Date utilDate) {
            return utilDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        }

        logger.warn(
                "Unsupported datetime value: value='{}', class='{}'",
                value,
                value.getClass().getName()
        );

        return null;
    }

    public LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof LocalDate localDate) {
            return localDate;
        }

        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }

        if (value instanceof java.util.Date utilDate) {
            return utilDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        logger.warn(
                "Unsupported date value: value='{}', class='{}'",
                value,
                value.getClass().getName()
        );

        return null;
    }


    public UserStatus parseStatus(Object value) {
        if (value == null) return null;
        try {
            return UserStatus.valueOf(value.toString());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public GenderCode parseGender(Object value) {
        if (value == null) return GenderCode.NOT_STATED;
        try {
            return GenderCode.valueOf(value.toString());
        } catch (IllegalArgumentException e) {
            return GenderCode.NOT_STATED;
        }
    }

    public Difficulty parseDifficulty(Object value) {
        if (value == null) {
            return null;
        }

        try {
            return Difficulty.valueOf(value.toString().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Unknown difficulty value: '{}'", value);
            return null;
        }
    }
}