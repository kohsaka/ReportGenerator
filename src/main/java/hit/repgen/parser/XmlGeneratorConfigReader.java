/**
 * 
 */
package hit.repgen.parser;

import hit.repgen.config.ComponentConfig;
import hit.repgen.config.ComponentSet;
import hit.repgen.config.GeneratorConfig;
import hit.repgen.config.ComponentSet.ComponentType;

import java.util.Map;
import java.util.Properties;

/**
 * @author kohsaka.N0003
 * 
 */
public class XmlGeneratorConfigReader extends XmlFileParser<GeneratorConfig> {

	/** このクラスで使用する要素名の定義 */
	enum EName implements NameDef{
		GeneratorConfig("generator-config"),
		ComponentSet("component-set"),
		Component("component"),
		Id("id"),
		Name("name"),
		Description("description"),
		ClassName("class-name"),
		Properties("properties"),
		Property("property");

		private String eName;
		private EName(String eName){ this.eName = eName; }
		@Override public String getName(){ return this.eName; }
	};
	
	/** このクラスで使用する属性名の定義 */
	enum AName implements NameDef{
		Default("default"),
		Scope("scope"),
		Type("type"),
		Name("name");

		private String eName;
		private AName(String eName){ this.eName = eName; }
		@Override public String getName(){ return this.eName; }
	}
	
	@Override
	protected XmlElementHandler<GeneratorConfig> getTopElementHandler() {
		return new GeneratorConfigHandler();
	}
	
	//
	// XML要素のハンドラを定義します。
	// 解析処理はXmlFileParser側に実装されています。
	//

	class GeneratorConfigHandler extends XmlElementHandler<GeneratorConfig> {

		public GeneratorConfigHandler() {
			addHandler(new PropertiesHandler());
			addHandler(new ComponentSetHandler());
		}

		@Override
		public String getEName() {
			return EName.GeneratorConfig.getName();
		}

		@Override
		protected void initMyResult() {
			this.result = new GeneratorConfig();
		}
		
		@Override
		protected void putChildElementValue(String eName, Map<String, String> attrMap, Object value) {
			if( isSameName(EName.Properties, eName)){
				this.result.setProperties((Properties)value);
			}else if( isSameName(EName.ComponentSet, eName)){
				this.result.getComponentSetList().add((ComponentSet)value);
			}else{
				handleUnsupportedChildElement(eName);
			}
			handleUnsupportedAttribute(eName, attrMap);
		}
	}

	class PropertiesHandler extends XmlElementHandler<Properties>{

		public PropertiesHandler(){
			// 子要素(ネストあり)はないのでハンドラは追加しない。
		}
		
		@Override
		public String getEName() {
			return EName.Properties.getName();
		}

		@Override
		protected void initMyResult() {
			this.result = new Properties();
		}

		@Override
		protected void putChildElementValue(String eName, Map<String, String> attrMap, Object value) {
			if( isSameName(EName.Property, eName) ){
				final String nameAttrName = AName.Name.getName();
				String nameValue;
				if( attrMap.containsKey(nameAttrName)){
					nameValue = attrMap.get(nameAttrName);
					this.result.put(nameValue, value);
					attrMap.remove(nameAttrName);
				}else{
					handleRequiredAttribute(eName, nameAttrName);
				}
			}else{
				handleUnsupportedChildElement(eName);
			}
			handleUnsupportedAttribute(eName, attrMap);
		}
	}
	
	class ComponentSetHandler extends XmlElementHandler<ComponentSet> {

		public ComponentSetHandler(){
			addHandler(new ComponentHandler());
		}
		
		@Override
		public String getEName() {
			return EName.ComponentSet.getName();
		}
		
		@Override
		protected void initMyResult() {
			this.result = new ComponentSet();
		}
		
		@Override
		protected void putMyAttributes(Map<String,String> attrMap) {

			String name, value;
			
			// TODO やっぱりアノテーションの方が良いか...
			
			name = AName.Type.getName();
			value = attrMap.get(name);
			if( value != null ){
				ComponentType type = ComponentType.parse(value, ComponentType.UNKOWN);
				if( ComponentType.UNKOWN != type ){
					this.result.setType(type);
				}else{
					handleUnsupportedValue(name, value);
				}
				attrMap.remove(name);
			}else{
				handleRequiredAttribute(null, name);
			}

			handleUnsupportedAttribute(null, attrMap);
		}

		@Override
		protected void putChildElementValue(String eName, Map<String, String> attrMap, Object value) {
			if( isSameName(EName.Component, eName)){
				this.result.getComponentConfigList().add((ComponentConfig)value);
			}else{
				handleUnsupportedChildElement(eName);
			}
			handleUnsupportedAttribute(eName, attrMap);
		}
	}

	class ComponentHandler extends XmlElementHandler<ComponentConfig> {

		public ComponentHandler(){
			addHandler(new PropertiesHandler());
		}
		
		@Override
		public String getEName() {
			return EName.Component.getName();
		}

		@Override
		protected void initMyResult() {
			this.result = new ComponentConfig();
		}

		@Override
		protected void putChildElementValue(String eName, Map<String, String> attrMap, Object value) {
			if( isSameName(EName.Id, eName)){
				this.result.setId((String)value);
			}else if( isSameName(EName.Name, eName)){
				this.result.setName((String)value);
			}else if( isSameName(EName.Description, eName)){
				this.result.setDescription((String)value);
			}else if( isSameName(EName.ClassName, eName)){
				this.result.setClassName((String)value);
			}else if( isSameName(EName.Properties, eName)){
				this.result.setProperties((Properties)value);
			}else{
				handleUnsupportedChildElement(eName);
			}
			handleUnsupportedAttribute(eName, attrMap);
		}
	}
	

}
