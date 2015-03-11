/**
 * 
 */
package hit.repgen.datamodel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kohsaka.N0003
 *
 */
public class ResultTable extends ResultBase{

	private int columnCount;
	
	private String[] columnNames;
	
	private List<ResultTableRow> rowList;
	
	public ResultTable(String[] columnNames){
		this.columnCount = columnNames.length;
		this.columnNames = columnNames;
		this.rowList = new ArrayList<>();
	}
	
	public ResultTableRow newRow(){
		return new ResultTableRow(this.columnCount);
	}

	public void addRow(ResultTableRow row){
		this.rowList.add(row);
	}
	
	public ResultTableRow getRow(int index){
		return this.rowList.get(index);
	}
	
	
//	public List<ResultTableRow> getRowList(){
//		return this.rowList;
//	}
	
	public int getColumnCount(){
		return this.columnCount;
	}
	
	public int getRowCount(){
		return this.rowList.size();
	}
	
	public String toString(){
		return getClass().getSimpleName() + "(columns=" + this.columnCount + ", rows=" + this.rowList.size() + ")";
	}

	

}
