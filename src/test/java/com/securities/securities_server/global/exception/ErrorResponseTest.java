package com.securities.securities_server.global.exception;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static com.securities.securities_server.global.exception.ErrorCode.INTERNAL_SERVER_ERROR;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ErrorResponseTest {

    @Test
    void ErrorCode를_전달하면_ErrorResponse_형식에_맞도록_값이_변환된다() {
        // when
        ErrorResponse errorResponse = ErrorResponse.from(INTERNAL_SERVER_ERROR);

        // then
        assertThat(errorResponse.status()).isEqualTo(INTERNAL_SERVER_ERROR.getStatus().value());
        assertThat(errorResponse.code()).isEqualTo(INTERNAL_SERVER_ERROR.getCode());
        assertThat(errorResponse.message()).isEqualTo(INTERNAL_SERVER_ERROR.getMessage());
    }
}