package com.github.t1.swap;

import javax.lang.model.element.Name;

public class MockName implements Name {
    private final String string;

    public MockName(String string) {
        this.string = string;
    }

    @Override
    public int length() {
        return string.length();
    }

    @Override
    public char charAt(int index) {
        return string.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return string.subSequence(start, end);
    }

    @Override
    public boolean contentEquals(CharSequence cs) {
        return string.equals(cs);
    }
}
