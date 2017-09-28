package com.ninjarific.radiomesh.utils.listutils;

import android.annotation.SuppressLint;
import android.os.Build;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper utilities for performing functional operations on lists.
 * List manipulations all return new lists and can be chained together for complex operations,
 * eg combinations of mapping and filtering.
 * This class makes use of streams on Android api 24. Tests need to be kept up to date to ensure that
 * there aren't behavioural differences.
 */

public class ListUtils {

    public interface BuildVersionProvider {
        int getBuildVersion();
    }

    private static BuildVersionProvider buildVersionProvider = () -> Build.VERSION.SDK_INT;

    public static void setBuildVersionProvider(@Nullable BuildVersionProvider provider) {
        if (provider == null) {
            buildVersionProvider = () -> Build.VERSION.SDK_INT;
        } else {
            buildVersionProvider = provider;
        }
    }

    @SuppressLint("NewApi")
    public static <T, S> List<S> map(List<T> mapList, Mapper<T, S> mapper) {
        if (buildVersionProvider.getBuildVersion() >= Build.VERSION_CODES.N) {
            return mapList.stream().map(mapper::map).filter(value -> value != null).collect(Collectors.toList());
        } else {
            List<S> returnList = new ArrayList<>(mapList.size());
            //noinspection Convert2streamapi
            for (T t : mapList) {
                if (t != null) {
                    returnList.add(mapper.map(t));
                }
            }
            return returnList;
        }
    }

    public static <T, S> List<S> flatMap(List<T> mapList, Mapper<T, List<S>> mapper) {
        List<S> returnList = new ArrayList<>(mapList.size());
        for (T t : mapList) {
            if (t != null) {
                returnList.addAll(mapper.map(t));
            }
        }
        return returnList;
    }

    @SuppressLint("NewApi")
    public static <T, S> List<S> mapFilter(List<T> mapList, Mapper<T, S> mapper, Condition<S> condition) {
        if (buildVersionProvider.getBuildVersion() >= Build.VERSION_CODES.N) {
            return mapList.stream().map(mapper::map).filter(condition::isTrue).collect(Collectors.toList());
        } else {
            List<S> returnList = new ArrayList<>();
            //noinspection Convert2streamapi
            for (T t : mapList) {
                if (t == null) {
                    continue;
                }
                S mappedVal = mapper.map(t);
                if (condition.isTrue(mappedVal)) {
                    returnList.add(mappedVal);
                }
            }
            return returnList;
        }
    }

    @SuppressLint("NewApi")
    public static <T> List<T> filter(List<T> mapList, Condition<T> condition) {
        if (buildVersionProvider.getBuildVersion() >= Build.VERSION_CODES.N) {
            return mapList.stream().filter(condition::isTrue).collect(Collectors.toList());
        } else {
            List<T> returnList = new ArrayList<>();
            //noinspection Convert2streamapi
            for (T t : mapList) {
                if (t != null && condition.isTrue(t)) {
                    returnList.add(t);
                }
            }
            return returnList;
        }
    }

    @SuppressLint("NewApi")
    public static <T> void foreach(Collection<T> collection, Job<T> job) {
        if (buildVersionProvider.getBuildVersion() >= Build.VERSION_CODES.N) {
            collection.forEach(job::perform);
        } else {
            //noinspection Convert2streamapi
            for (T t : collection) {
                job.perform(t);
            }
        }
    }

    /**
     * Find the first object in the list matching the condition.
     * @return -1 if no match found, otherwise index within list length
     */
    public static <T> int indexOfObjectMatchingCondition(List<T> list, Condition<T> condition) {
        for (int i = 0; i < list.size(); i++) {
            if (condition.isTrue(list.get(i))) {
                return i;
            }
        }
        return -1;
    }

    public static <T> T reduce(List<T> list, T initialValue, Reducer<T> reducer) {
        T current = initialValue;
        for (T item : list) {
            current = reducer.reduce(current, item);
        }
        return current;
    }

    public static <T, S> S mapReduce(List<T> list, S initialValue, Mapper<T, S> mapper, Reducer<S> reducer) {
        S current = initialValue;
        for (T item : list) {
            current = reducer.reduce(current, mapper.map(item));
        }
        return current;
    }
}
