package hit.repgen.datamodel;

import java.util.HashMap;
import java.util.Map;

public class DataResult {
	
	private Map<String, ResultBase> resultMap;
	
	public DataResult(){
		this.resultMap = new HashMap<>();
	}
	
	public ResultBase getResult(String queryId){
		return this.resultMap.get(queryId);
	}
	
	public void addResult(String queryId, ResultBase result){
		this.resultMap.put(queryId, result);
	}
	

}
