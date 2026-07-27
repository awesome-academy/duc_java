package com.tripgoapi.application.port.in;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TourSearchQueryTest {

    private TourSearchQuery query(int page, int size) {
        return new TourSearchQuery(null, null, null, null, null, null, null, null, null, page, size);
    }

    @Test
    void clampsNonPositivePageTo1() {
        assertThat(query(0, 12).page()).isEqualTo(1);
        assertThat(query(-5, 12).page()).isEqualTo(1);
    }

    @Test
    void defaultsNonPositiveSizeTo12() {
        assertThat(query(1, 0).size()).isEqualTo(12);
        assertThat(query(1, -100).size()).isEqualTo(12);
    }

    @Test
    void clampsOversizedSizeTo50() {
        // Regression guard: search endpoint documents "tối đa 50" — the compact constructor
        // is the single source of truth enforcing this, so a client sending limit=100000
        // must never reach the repository unclamped.
        assertThat(query(1, 50).size()).isEqualTo(50);
        assertThat(query(1, 51).size()).isEqualTo(50);
        assertThat(query(1, 100_000).size()).isEqualTo(50);
    }

    @Test
    void keepsInRangeSizeUnchanged() {
        assertThat(query(1, 25).size()).isEqualTo(25);
    }

    @Test
    void defaultsNullSortToNewest() {
        TourSearchQuery query = new TourSearchQuery(null, null, null, null, null, null, null, null, null, 1, 12);

        assertThat(query.sort()).isEqualTo(TourSortOption.NEWEST);
    }

    @Test
    void pageAndSizeAreAlwaysValidForZeroBasedPageRequest() {
        // Regression guard: PageRequest.of(query.page() - 1, query.size()) must never receive
        // a negative page index or an unbounded size, however extreme the raw client input is.
        for (int rawPage : new int[]{Integer.MIN_VALUE, -1, 0, 1}) {
            for (int rawSize : new int[]{Integer.MIN_VALUE, -1, 0, 1, 50, 51, Integer.MAX_VALUE}) {
                TourSearchQuery q = query(rawPage, rawSize);
                assertThat(q.page()).isGreaterThanOrEqualTo(1);
                assertThat(q.size()).isBetween(1, 50);
            }
        }
    }
}
