package com.ninjarific.radiomesh.utils.listutils;

import android.support.annotation.Nullable;

public interface Condition<T> {
    boolean isTrue(@Nullable T t);
}
