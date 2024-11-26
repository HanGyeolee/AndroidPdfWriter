package com.hangyeolee.pdf.core.listener;

/**
 * java.util.function.Function 이 API 레벨 24에 추가되었습니다.<br>
 * 최소 API 레벨 14 까지 지원하기 위해 Function 대신 해당 인터페이스를 사용합니다.<br>
 * java.util.function.Function was added in API level 24.<br>
 * Use that interface instead of Function to support up to a minimum API level 14
 * @see java.util.function.Function
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 */
public interface Action<T, R> {
    R invoke(T t);
}
