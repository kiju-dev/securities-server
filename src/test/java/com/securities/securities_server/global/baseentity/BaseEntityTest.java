package com.securities.securities_server.global.baseentity;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class BaseEntityTest {

    @Test
    void delete_메서드_호출_시_deletedAt이_저장된다() {
        // given
        TestEntity testEntity = new TestEntity();

        // when
        testEntity.delete();

        // then
        assertThat(testEntity.getDeletedAt()).isNotNull();
        assertThat(testEntity.isDeleted()).isTrue();
    }

    @Test
    void 삭제되지_않은_엔티티는_isDeleted_결과가_false다() {
        // given
        TestEntity testEntity = new TestEntity();

        // when
        boolean result = testEntity.isDeleted();

        // then
        assertThat(result).isFalse();
    }

    static class TestEntity extends BaseEntity {
    }
}