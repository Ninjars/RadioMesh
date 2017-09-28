package com.ninjarific.radiomesh.utils.listutils;

import android.support.annotation.NonNull;

public interface Mapper<T, S> {
    @NonNull S map(@NonNull T t);
}
