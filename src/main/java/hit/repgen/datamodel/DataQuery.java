package hit.repgen.datamodel;

import java.util.HashMap;
import java.util.Map;

public class DataQuery {
	
	private static volatile long serialId = 1; 

	private String id;
	
	private String dataDefinitionId;
	
	private Map<String, Object> paramMap;

	protected static String createId(){
		return Long.toHexString(serialId++);
	}
	
	public DataQuery(){
		this.id = DataQuery.createId();
		this.paramMap = new HashMap<>();
	}
	
	public String getId(){
		return this.id;
	}
	
	/**
	 * @return dataDefinitionId
	 */
	public String getDataDefinitionId() {
		return dataDefinitionId;
	}

	/**
	 * @param dataDefinitionId �Z�b�g���� dataDefinitionId
	 */
	public void setDataDefinitionId(String dataDefinitionId) {
		this.dataDefinitionId = dataDefinitionId;
	}

	/**
	 * @return paramMap
	 */
	public Map<String, Object> getParamMap() {
		return paramMap;
	}

	/**
	 * @param paramMap �Z�b�g���� paramMap
	 */
	public void setParamMap(Map<String, Object> paramMap) {
		this.paramMap = paramMap;
	}
	
	public String toString(){
		return getClass().getSimpleName() + "(" + getId() + ")";
	}

}
