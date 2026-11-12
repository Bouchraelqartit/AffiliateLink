package com.affiliateLink.offer.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class TagTest {

    @Test
    void shouldNormalizeTagName() {
        Tag tag = new Tag("  TeCH  ");

        assertThat(tag.getName()).isEqualTo("tech");
    }

    @Test
    void shouldNormalizeUppercaseName() {
        assertThat(Tag.normalize("GAMING"))
                .isEqualTo("gaming");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void shouldRejectBlankTagNames(String name) {
        assertThatThrownBy(() -> new Tag(name))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectTagNameLongerThan50Characters() {
        String name = "a".repeat(51);

        assertThatThrownBy(() -> new Tag(name))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldAcceptTagNameWith50Characters() {
        String name = "a".repeat(50);

        assertThat(new Tag(name).getName()).isEqualTo(name);
    }
}
