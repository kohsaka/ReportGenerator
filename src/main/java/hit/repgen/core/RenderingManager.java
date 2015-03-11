package hit.repgen.core;

import hit.repgen.config.ComponentConfig;
import hit.repgen.config.ComponentSet;
import hit.repgen.config.ComponentSet.ComponentType;
import hit.repgen.config.Resource;
import hit.repgen.datamodel.DataResult;
import hit.repgen.renderer.Renderer;
import hit.repgen.renderer.RenderingTemplate;
import hit.repgen.util.ExceptionUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RenderingManager {
	
	private Properties properties;

	private Map<String, Renderer> rendererMap;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public RenderingManager(){
		this.rendererMap = new HashMap<>();
	}
	
	public int addRenderers(ComponentSet componentSet){
		
		int count = 0;
		
		ComponentType type = componentSet.getType();
		if( !ComponentType.Renderer.equals(type) ){
			return count;
		}
		
		List<ComponentConfig> configList = componentSet.getComponentConfigList();
		for(ComponentConfig config: configList){
			
			String id = config.getId();

			Renderer renderer = createRender(config);
			if( renderer == null){
				continue;
			}
			
			try{
				renderer.init(id, config.getProperties());
			}catch(Exception e){
				logger.warn("initialization failed({}): {}", id, ExceptionUtils.getStackTrace(e));
				continue;
			}
			
			try{
				if( !renderer.validate() ){
					logger.warn("validation failed({})", id);
				}
			}catch(Exception e){
				logger.warn("validation failed({}): {}", id, ExceptionUtils.getStackTrace(e));
			}
			
			// IDÇ≈ÉåÉìÉ_ÉâÅ\ï€éù
			this.rendererMap.put(id, renderer);
			
			count++;
		}
		
		return count;
	}
	
	protected Renderer createRender(ComponentConfig config){

		String className = config.getClassName();
		Class<?> clazz = null;
		try{
			clazz = Class.forName(className);
		}catch(ClassNotFoundException e){
			clazz = null;
		}
		if( clazz == null ){
			logger.warn("could not found class: {}", className);
			return null;
		}
		
		Object instance = null;
		try{
			instance = clazz.newInstance();
		}catch(Exception e){
			instance = null;
		}
		if( !(instance instanceof Renderer)){
			logger.warn("could not instanciate class: {}", className);
			return null;
		}
		
		return (Renderer)instance;
	}

	public RenderingTemplate generateTemplate(String rendererId, Resource resource) throws Exception{

		logger.debug("template generating start: rendererId={}, resource={}", rendererId, resource);

		Renderer renderer = this.rendererMap.get(rendererId);
		if( renderer == null ){
			throw new IllegalArgumentException("renderer not found: " + rendererId);
		}

		RenderingTemplate template = renderer.generateTemplate(resource);

		logger.debug("template generating end");
		
		return template;
	}

	public InputStream render(RenderingTemplate template, DataResult result) throws Exception{
		
		logger.debug("rendering start: template={}", template);

		String rendererId = template.getRendererId();
		Renderer renderer = this.rendererMap.get(rendererId);
		InputStream is =renderer.render(template, result);
		
		logger.debug("rendering end");

		return is;
	}
	
}
