package hit.repgen.config;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class DataSet {
	
	private List<DataDefinition> dataDefinitionList;
	
	private String defaultDataProviderId;
	
	private DataScope defaultScope;

	public DataSet(){
		this.dataDefinitionList = new ArrayList<>();
	}
	
	/**
	 * @return dataDefinitionList
	 */
	public List<DataDefinition> getDataDefinitionList() {
		return dataDefinitionList;
	}

	/**
	 * @param dataDefinitionList セットする dataDefinitionList
	 */
	public void setDataDefinitionList(List<DataDefinition> dataDefinitionList) {
		this.dataDefinitionList = dataDefinitionList;
	}

	/**
	 * @return defaultDataProviderId
	 */
	public String getDefaultDataProviderId() {
		return defaultDataProviderId;
	}

	/**
	 * @param defaultDataProviderId セットする defaultDataProviderId
	 */
	public void setDefaultDataProviderId(String defaultDataProviderId) {
		this.defaultDataProviderId = defaultDataProviderId;
	}

	/**
	 * @return defaultScope
	 */
	public DataScope getDefaultScope() {
		return defaultScope;
	}

	/**
	 * @param defaultScope セットする defaultScope
	 */
	public void setDefaultScope(DataScope defaultScope) {
		this.defaultScope = defaultScope;
	}
	
	/**
	 * 設定情報を返します。
	 * @return 設定情報
	 */
	public String toString(){
		String className = getClass().getSimpleName();
		String defaultScope = (this.defaultScope != null) ? this.defaultScope.name(): null;
		return MessageFormat.format("{0}(defaultDataProviderId: {1}, defaultScope: {2}): [{3}]", className, this.defaultDataProviderId, defaultScope, this.dataDefinitionList);
	}

}
