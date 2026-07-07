package fancy.datasource.starter.provider;

import fancy.datasource.starter.model.DataSourceModel;

import java.util.List;

/**
 * 数据源提供者接口.
 *
 * @author Fan
 */
public interface DataSourceProvider {

    List<DataSourceModel> load();
}
