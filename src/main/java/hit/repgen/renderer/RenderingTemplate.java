package hit.repgen.renderer;

import hit.repgen.config.Resource;
import hit.repgen.datamodel.DataQuery;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RenderingTemplate{

	private Resource resource;
	
	private String rendererId;
	
	private List<DataQuery> queryList;
	
	public RenderingTemplate(){
		this.queryList = new ArrayList<>();
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

	/**
	 * @return rendererId
	 */
	public String getRendererId() {
		return rendererId;
	}

	/**
	 * @param rendererId セットする rendererId
	 */
	public void setRendererId(String rendererId) {
		this.rendererId = rendererId;
	}

	public void setDataQueryList(List<DataQuery> queryList){
		this.queryList.addAll(queryList);
	}

	public List<DataQuery> getDataQueryList(){
		return this.queryList;
	}
	
	public void close() {
		// Nothing to do
	}
	
}
