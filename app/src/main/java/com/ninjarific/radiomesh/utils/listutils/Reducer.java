package com.ninjarific.radiomesh.utils.listutils;

public interface Reducer<T> {
    T reduce(T current, T next);
}
