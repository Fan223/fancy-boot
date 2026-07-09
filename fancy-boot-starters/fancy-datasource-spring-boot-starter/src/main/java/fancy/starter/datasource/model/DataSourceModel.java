package fancy.starter.datasource.model;

import java.util.Map;

/**
 * 数据源模型.
 *
 * @author Fan
 */
public record DataSourceModel(

        // 数据源唯一标识码
        String code,

        String driverClassName,

        String url,

        String username,

        String password,

        // 数据源属性
        Map<String, Object> properties
) {
}
