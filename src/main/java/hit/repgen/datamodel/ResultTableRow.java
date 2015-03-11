/**
 * 
 */
package hit.repgen.datamodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author kohsaka.N0003
 * 
 */
public class ResultTableRow extends ResultBase {

	private int columnCount;

	private List<Object> values;

	public ResultTableRow(int columnCount) {
		this.columnCount = columnCount;
		List<Object> values = new ArrayList<>(columnCount);
		for(int i=0; i<columnCount; i++) values.add(null);
		this.values = values;
	}

	public void setValue(int column, Object value) {
		this.values.set(column, value);
	}
	
	public Object getValue(int column){
		return this.values.get(column);
	}

	public String toString() {
		return getClass().getSimpleName() + "(" + this.values + ")";
	}
}
