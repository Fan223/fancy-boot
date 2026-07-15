package fancy.starter.datasource.mapper;

import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.ColumnMapRowMapper;

import java.nio.charset.Charset;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * 编码 {@link ColumnMapRowMapper}.
 *
 * @author Fan
 */
@AllArgsConstructor
public class EncodingColumnMapRowMapper extends ColumnMapRowMapper {

    private final Charset charset;

    @Override
    protected @Nullable Object getColumnValue(ResultSet rs, int index) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        JDBCType jdbcType = JDBCType.valueOf(meta.getColumnType(index));

        switch (jdbcType) {
            case CHAR, VARCHAR, LONGVARCHAR:
                byte[] bytes = rs.getBytes(index);
                if (bytes == null) {
                    return null;
                }
                return new String(bytes, charset);
            default:
                return super.getColumnValue(rs, index);
        }
    }
}
