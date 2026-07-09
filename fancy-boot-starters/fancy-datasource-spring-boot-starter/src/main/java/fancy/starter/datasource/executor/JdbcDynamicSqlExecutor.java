package fancy.starter.datasource.executor;

import fancy.starter.datasource.core.DynamicDataSourceManager;
import fancy.starter.datasource.mapper.EncodingColumnMapRowMapper;
import fancy.starter.datasource.model.DataSourceModel;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * {@link JdbcTemplate} 动态 SQL 执行器, 通过 {@link DynamicDataSourceManager} 获取数据源并执行 SQL 语句.
 *
 * @author Fan
 */
@RequiredArgsConstructor
public class JdbcDynamicSqlExecutor implements DynamicSqlExecutor {

    private static final RowMapper<Map<String, Object>> DEFAULT_ROW_MAPPER = new ColumnMapRowMapper();

    private final DynamicDataSourceManager manager;

    /**
     * 根据数据源唯一标识码获取对应的 {@link NamedParameterJdbcTemplate} 实例.
     *
     * @param code 数据源唯一标识码
     * @return {@link NamedParameterJdbcTemplate}
     */
    private NamedParameterJdbcTemplate getNamedParameterJdbcTemplate(String code) {
        return new NamedParameterJdbcTemplate(manager.get(code));
    }

    @Override
    public List<Map<String, Object>> query(String code, String sql, Object... args) {
        return getNamedParameterJdbcTemplate(code).getJdbcTemplate().query(sql, buildRowMapper(code), args);
    }

    @Override
    public List<Map<String, Object>> query(String code, String sql, Map<String, Object> params) {
        return getNamedParameterJdbcTemplate(code).query(sql, params, buildRowMapper(code));
    }

    @Override
    public int executeUpdate(String code, String sql, Object... args) {
        return getNamedParameterJdbcTemplate(code).getJdbcTemplate().update(sql, args);
    }

    @Override
    public int executeUpdate(String code, String sql, Map<String, Object> params) {
        return getNamedParameterJdbcTemplate(code).update(sql, params);
    }

    /**
     * 构造数据源对应的 {@link RowMapper}.
     *
     * @param code 数据源唯一标识码
     * @return {@link RowMapper}
     */
    private RowMapper<Map<String, Object>> buildRowMapper(String code) {
        DataSourceModel model = manager.getModel(code);
        if (model == null) {
            return DEFAULT_ROW_MAPPER;
        }

        String charset = getCharset(model);
        if (charset == null) {
            return DEFAULT_ROW_MAPPER;
        }

        try {
            return new EncodingColumnMapRowMapper(Charset.forName(charset));
        } catch (Exception _) {
            return DEFAULT_ROW_MAPPER;
        }
    }

    public String getCharset(DataSourceModel model) {
        Map<String, Object> properties = model.properties();
        if (properties == null) {
            return null;
        }

        Object charset = properties.get("charset");
        if (charset == null) {
            return null;
        }
        String charsetStr = charset.toString().trim();
        return charsetStr.isEmpty() ? null : charsetStr;
    }
}
