package hit.repgen.renderer;

import hit.repgen.config.Resource;
import hit.repgen.datamodel.DataQuery;
import hit.repgen.datamodel.DataResult;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;

public interface Renderer {

	public void init(String myRendererId, Properties prop);
	
	public boolean validate();
	
	public RenderingTemplate generateTemplate(Resource resource)  throws Exception;
	
	public InputStream render(RenderingTemplate template, DataResult dataResult) throws Exception;
	
	public void close();
}
