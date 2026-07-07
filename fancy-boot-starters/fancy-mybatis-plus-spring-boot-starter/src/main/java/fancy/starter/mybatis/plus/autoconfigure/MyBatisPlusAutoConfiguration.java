package fancy.starter.mybatis.plus.autoconfigure;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import fancy.starter.mybatis.plus.handler.FancyMetaObjectHandler;
import fancy.starter.mybatis.plus.properties.MyBatisPlusProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * MyBatis-Plus 自动配置类.
 *
 * @author Fan
 */
@AutoConfiguration
@ConditionalOnClass(MybatisPlusInterceptor.class)
@EnableConfigurationProperties(MyBatisPlusProperties.class)
public class MyBatisPlusAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor(MyBatisPlusProperties properties) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 乐观锁插件.
        if (properties.isOptimisticLocker()) {
            interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        }
        // 防全表更新与删除插件.
        if (properties.isBlockAttack()) {
            interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        }
        // 分页插件. 多数据源可以不配具体类型, 存在多个插件时, 应放到插件执行链的最后面.
        if (properties.isPagination()) {
            DbType dbType = resolveDbType(properties.getDbType());
            interceptor.addInnerInterceptor(new PaginationInnerInterceptor(dbType));
        }
        return interceptor;
    }

    private DbType resolveDbType(String dbType) {
        try {
            return DbType.getDbType(dbType);
        } catch (Exception e) {
            throw new IllegalArgumentException("fancy.mybatis-plus.dbType 配置不合法: " + dbType, e);
        }
    }

    @Bean
    @ConditionalOnMissingBean(MetaObjectHandler.class)
    public MetaObjectHandler metaObjectHandler() {
        return new FancyMetaObjectHandler();
    }
}
