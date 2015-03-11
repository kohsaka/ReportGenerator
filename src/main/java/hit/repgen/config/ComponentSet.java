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
		 * �����񂩂�l���擾���܂��B<br>
		 * @param name ����
		 * @param defaultName ��͕s���ɕԋp����l
		 * @return �R���|�[�l���g�^�C�v
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
	 * @param type �Z�b�g���� type
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
	 * @param componentConfigList �Z�b�g���� componentConfigList
	 */
	public void setComponentConfigList(List<ComponentConfig> componentConfigList) {
		this.componentConfigList = componentConfigList;
	}
	
	/**
	 * �ݒ����Ԃ��܂��B
	 * @return �ݒ���
	 */
	public String toString(){
		String className = getClass().getSimpleName();
		String typeName = (this.type != null) ? this.type.getName(): null;
		int count = (this.componentConfigList != null ) ? componentConfigList.size(): null;
		return MessageFormat.format("{0}(type:{1}, count:{2})[{3}]", className, typeName, count, this.componentConfigList); 
	}
	
}
