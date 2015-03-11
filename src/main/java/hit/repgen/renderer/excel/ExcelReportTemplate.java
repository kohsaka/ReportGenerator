package hit.repgen.renderer.excel;

import hit.repgen.config.Resource;
import hit.repgen.renderer.RenderingTemplate;

import java.util.ArrayList;
import java.util.List;

public class ExcelReportTemplate extends RenderingTemplate{

	/** このテンプレートの元となるリソース */
	private Resource resource;
	
	protected List<ExcelPlaceholder> placeholderList;
	
	public ExcelReportTemplate(){
		this.resource = null;
		this.placeholderList = new ArrayList<>();
	}
	
	/**
	 * @return resource
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * @param resource セットする resource
	 */
	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public void setPlaceholderList(List<ExcelPlaceholder> placeholderList){
		this.placeholderList = placeholderList;
	}
	
	public List<ExcelPlaceholder> getPlaceholderList(){
		return this.placeholderList;
	}
	
	public void destory() {
		// Nothing to do
	}
	
}
