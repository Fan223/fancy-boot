package fancy.starter.mybatis.plus.properties;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 代码生成器配置.
 *
 * @author Fan
 */
@Getter
@Setter
@Builder
public class CodeProperties {

    private String url;

    @Builder.Default
    private String username = "root";

    private String password;

    @Builder.Default
    private String author = "Fan";

    private String outputDir;

    @Builder.Default
    private String parent = "";

    private String moduleName;

    @Builder.Default
    private String tablePrefix = "";

    @Builder.Default
    private String tableSuffix = "";
}
