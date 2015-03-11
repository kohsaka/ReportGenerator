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
	 * @param dataSetList �Z�b�g���� dataSetList
	 */
	public void setDataSetList(List<DataSet> dataSetList) {
		this.dataSetList = dataSetList;
	}
	
	/**
	 * �ݒ����Ԃ��B
	 * @return �ݒ���
	 */
	public String toString(){
		return MessageFormat.format("{0}: {1}", getClass().getSimpleName(), this.dataSetList);
	}

}
