package fancy.boot.core.http;

/**
 * HTTP 状态系列.
 *
 * @author Fan
 */
public enum Series {

    /**
     * {@code 1xx}, 信息性状态.
     */
    INFORMATIONAL(1),

    /**
     * {@code 2xx}, 成功状态.
     */
    SUCCESSFUL(2),

    /**
     * {@code 3xx}, 重定向状态.
     */
    REDIRECTION(3),

    /**
     * {@code 4xx}, 客户端错误状态.
     */
    CLIENT_ERROR(4),

    /**
     * {@code 5xx}, 服务器错误状态.
     */
    SERVER_ERROR(5);

    /**
     * 状态系列数值.
     */
    private final int value;

    Series(int value) {
        this.value = value;
    }

    /**
     * 根据状态码获取对应的 {@link Series}.
     *
     * @param statusCode 状态码
     * @return {@link Series}
     */
    public static Series valueOf(int statusCode) {
        Series series = resolve(statusCode);
        if (series == null) {
            throw new IllegalArgumentException("No matching constant for [" + statusCode + "].");
        }
        return series;
    }

    /**
     * 根据状态码解析对应的 {@link Series}, 找不到则返回 {@code null}.
     *
     * @param statusCode 状态码
     * @return {@link Series}
     */
    public static Series resolve(int statusCode) {
        int seriesCode = statusCode / 100;
        for (Series series : values()) {
            if (series.value == seriesCode) {
                return series;
            }
        }
        return null;
    }

    /**
     * 返回状态系列数值.
     *
     * @return {@code int}
     */
    public int value() {
        return this.value;
    }
}
