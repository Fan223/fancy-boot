package fan.fancy.core.http;

/**
 * HTTP 响应状态码接口.
 *
 * @author Fan
 */
public interface HttpStatusCode {

    /**
     * 返回状态码数值.
     *
     * @return {@code int}
     */
    int value();

    /**
     * 判断状态码是否为信息性状态码({@code 1xx}).
     *
     * @return {@code boolean}
     */
    boolean is1xxInformational();

    /**
     * 判断状态码是否为成功状态码({@code 2xx}).
     *
     * @return {@code boolean}
     */
    boolean is2xxSuccessful();

    /**
     * 判断状态码是否为重定向状态码({@code 3xx}).
     *
     * @return {@code boolean}
     */
    boolean is3xxRedirection();

    /**
     * 判断状态码是否为客户端错误状态码({@code 4xx}).
     *
     * @return {@code boolean}
     */
    boolean is4xxClientError();

    /**
     * 判断状态码是否为服务器错误状态码({@code 5xx}).
     *
     * @return {@code boolean}
     */
    boolean is5xxServerError();

    /**
     * 判断状态码是否为错误状态码({@code 4xx} 或 {@code 5xx}).
     *
     * @return {@code boolean}
     */
    boolean isError();
}
