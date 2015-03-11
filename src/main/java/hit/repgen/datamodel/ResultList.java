/**
 * 
 */
package hit.repgen.datamodel;

import java.util.List;

/**
 * @author kohsaka.N0003
 *
 */
public class ResultList extends ResultBase{

	private List<Object> valueList;
	
	public ResultList(List<Object> valueList){
		this.valueList = valueList;
	}
	
	public String toString(){
		return getClass().getSimpleName() + "(" + this.valueList + ")";
	}
}
