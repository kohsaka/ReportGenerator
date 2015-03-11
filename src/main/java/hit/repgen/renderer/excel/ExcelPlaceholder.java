package hit.repgen.renderer.excel;

import hit.repgen.datamodel.DataQuery;

public class ExcelPlaceholder {

	private static volatile long serialId = 1; 

	private String id;
	
	private String tagName;
	
	private String sheetName;
	
	private int columnIndex;
	
	private int rowIndex;
	
	private String dataQueryId;
	
	public ExcelPlaceholder(String sheetName, String tagName, int columnIndex, int rowIndex){
		this.id = Long.toHexString(serialId++);
		this.sheetName = sheetName;
		this.tagName = tagName;
		this.columnIndex = columnIndex;
		this.rowIndex = rowIndex;
	}

	/**
	 * @return id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id セットする id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return tagName
	 */
	public String getTagName() {
		return tagName;
	}

	/**
	 * @param tagName セットする tagName
	 */
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	/**
	 * @return sheetName
	 */
	public String getSheetName() {
		return sheetName;
	}

	/**
	 * @param sheetName セットする sheetName
	 */
	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}

	/**
	 * @return column
	 */
	public int getColumnIndex() {
		return columnIndex;
	}

	/**
	 * @param columnIndex セットする column
	 */
	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}

	/**
	 * @return row
	 */
	public int getRowIndex() {
		return rowIndex;
	}

	/**
	 * @param rowIndex セットする row
	 */
	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}

	/**
	 * @return dataQueryId
	 */
	public String getDataQueryId() {
		return dataQueryId;
	}

	/**
	 * @param dataQueryId セットする dataQueryId
	 */
	public void setDataQueryId(String dataQueryId) {
		this.dataQueryId = dataQueryId;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append("(");
		sb.append("id=" + this.id);
		sb.append(", sheetName=" + this.sheetName);
		sb.append(", tagName=" + this.tagName);
		sb.append(", colIndex=" + this.columnIndex);
		sb.append(", rowIndex=" + this.rowIndex);
		sb.append(", dataQueryId=" + this.dataQueryId);
		sb.append(")");
		return sb.toString();
	}

}
