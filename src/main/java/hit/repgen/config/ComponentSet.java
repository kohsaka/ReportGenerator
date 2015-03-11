package hit.repgen.config;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class ComponentSet {

	public enum ComponentType{
		UNKOWN("unknown"), Renderer("renderer"), DataProvider("data-provider");
		
		private String name;
		private ComponentType(String name){ this.name = name; }
		public String getName(){ return this.name; }
		/**
		 * 文字列から値を取得します。<br>
		 * @param name 名称
		 * @param defaultName 解析不可時に返却する値
		 * @return コンポーネントタイプ
		 */
		public static ComponentType parse(String name, ComponentType defaultName){
			if( name == null ) return defaultName;
			if( Renderer.getName().toLowerCase().equals(name.toLowerCase())){
				return Renderer;
			}else if(DataProvider.getName().toLowerCase().equals(name.toLowerCase())){
				return DataProvider; 
			}else{
				return defaultName;
			}
		}
	}
	
	private ComponentType type;
	
	private List<ComponentConfig> componentConfigList;

	public ComponentSet(){
		this.componentConfigList = new ArrayList<>();
	}

	/**
	 * @return type
	 */
	public ComponentType getType() {
		return type;
	}

	/**
	 * @param type セットする type
	 */
	public void setType(ComponentType type) {
		this.type = type;
	}

	/**
	 * @return componentConfigList
	 */
	public List<ComponentConfig> getComponentConfigList() {
		return componentConfigList;
	}

	/**
	 * @param componentConfigList セットする componentConfigList
	 */
	public void setComponentConfigList(List<ComponentConfig> componentConfigList) {
		this.componentConfigList = componentConfigList;
	}
	
	/**
	 * 設定情報を返します。
	 * @return 設定情報
	 */
	public String toString(){
		String className = getClass().getSimpleName();
		String typeName = (this.type != null) ? this.type.getName(): null;
		int count = (this.componentConfigList != null ) ? componentConfigList.size(): null;
		return MessageFormat.format("{0}(type:{1}, count:{2})[{3}]", className, typeName, count, this.componentConfigList); 
	}
	
}
