package hit.repgen.config;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class DataConfig {
	
	private List<DataSet> dataSetList;

	public DataConfig(){
		this.dataSetList = new ArrayList<>();
	}
	
	/**
	 * @return dataSetList
	 */
	public List<DataSet> getDataSetList() {
		return dataSetList;
	}

	/**
	 * @param dataSetList セットする dataSetList
	 */
	public void setDataSetList(List<DataSet> dataSetList) {
		this.dataSetList = dataSetList;
	}
	
	/**
	 * 設定情報を返す。
	 * @return 設定情報
	 */
	public String toString(){
		return MessageFormat.format("{0}: {1}", getClass().getSimpleName(), this.dataSetList);
	}

}
