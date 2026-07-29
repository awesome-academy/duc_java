package com.tripgoapi.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlugTest {

    @Test
    void stripsVietnameseDiacritics_andLowercases() {
        assertThat(Slug.from("Đà Nẵng - Hội An 3N2Đ")).isEqualTo("da-nang-hoi-an-3n2d");
    }

    @Test
    void collapsesPunctuationRunsIntoSingleHyphens_andTrimsTheEdges() {
        // "--- Tour: Sa Pa!!! ---" must not produce leading/trailing or doubled hyphens, which
        // would make an ugly (and non-idempotent) public url.
        assertThat(Slug.from("--- Tour: Sa Pa!!! ---")).isEqualTo("tour-sa-pa");
    }

    @Test
    void returnsNullWhenNothingSlugAbleRemains() {
        // Callers need to distinguish "no slug possible" from an empty string so they can apply
        // their own fallback rather than writing "" into a UNIQUE column.
        assertThat(Slug.from("!!! ???")).isNull();
        assertThat(Slug.from("   ")).isNull();
        assertThat(Slug.from(null)).isNull();
    }
}
