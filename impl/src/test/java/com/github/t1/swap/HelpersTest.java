package com.github.t1.swap;

import org.junit.Test;

import static com.github.t1.swap.Helpers.*;
import static org.assertj.core.api.StrictAssertions.*;

public class HelpersTest {
    @Test
    public void shouldGetFirstSentenceOfEmpty() throws Exception {
        assertThat(summary("")).isEqualTo("");
    }

    @Test
    public void shouldGetFirstSentenceWithoutPeriod() throws Exception {
        assertThat(summary("foo bar")).isEqualTo("foo bar");
    }

    @Test
    public void shouldGetFirstSentenceWithTrailingPeriod() throws Exception {
        assertThat(summary("foo bar.")).isEqualTo("foo bar");
    }

    @Test
    public void shouldGetFirstSentenceWithPeriodAndMore() throws Exception {
        assertThat(summary("foo.bar")).isEqualTo("foo");
    }
}
