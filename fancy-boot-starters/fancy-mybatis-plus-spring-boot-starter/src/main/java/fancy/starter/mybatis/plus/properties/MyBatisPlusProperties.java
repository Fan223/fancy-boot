package fancy.starter.mybatis.plus.properties;

import com.baomidou.mybatisplus.annotation.DbType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MyBatis-Plus 配置.
 *
 * @author Fan
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "fancy.mybatis-plus")
public class MyBatisPlusProperties {

    private boolean optimisticLocker = true;

    private boolean blockAttack = true;

    private boolean pagination = true;

    /**
     * 数据库类型字符串, 例如 "MySQL" / "PostgreSQL", 启动时由 AutoConfiguration 转为 {@link DbType}.
     */
    private String dbType = "MySQL";
}
