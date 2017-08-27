package com.ninjarific.radiomesh.utils.listutils;

import android.support.annotation.Nullable;

public final class NonNullCondition<T> implements Condition<T> {
    @Override
    public boolean isTrue(@Nullable T t) {
        return t != null;
    }
}
