package hit.repgen.parser;

import hit.repgen.config.DataConfig;
import hit.repgen.config.DataDefinition;
import hit.repgen.config.DataScope;
import hit.repgen.config.DataSet;
import hit.repgen.config.DataType;

import java.util.Map;

import org.apache.commons.lang.UnhandledException;

public class XmlDataDefinitionReader extends XmlFileParser<DataConfig>{

	/** このクラスで使用する要素名の定義 */
	enum EName implements NameDef{
		Data("data"),
		DataSet("data-set"),
		DataDefinition("definition"),
		Id("id"),
		Scope("scope"),
		Type("type"),
		Desc("desc"),
		Define("define");

		private String eName;
		private EName(String eName){ this.eName = eName; }
		@Override public String getName(){ return this.eName; }
	};
	
	/** このクラスで使用する属性名の定義 */
	enum AName implements NameDef{
		DefaultDataProviderId("default-data-provider-id"),
		DataProviderId("data-provider-id"),
		DefaultScope("default-scope"),
		Scope("scope");

		private String eName;
		private AName(String eName){ this.eName = eName; }
		@Override public String getName(){ return this.eName; }
	}
	
	@Override
	protected XmlElementHandler<DataConfig> getTopElementHandler() {
		return new DataHandler();
	}
	
	//
	// XML要素のハンドラを定義します。
	// 解析処理はXmlFileParser側に実装されています。
	//
	
	class DataHandler extends XmlElementHandler<DataConfig>{

		@Override
		public String getEName() {
			return EName.Data.getName();
		}
		
		public DataHandler(){
			addHandler(new DataSetHandler());
		}

		@Override
		protected void initMyResult() {
			this.result = new DataConfig();
		}

		@Override
		protected void putChildElementValue(String eName, Map<String, String> attrMap, Object value) {
			if(isSameName(EName.DataSet, eName)){
				this.result.getDataSetList().add((DataSet)value);
			}else{
				handleUnsupportedChildElement(eName);
			}
			handleUnsupportedAttribute(eName, attrMap);
		}
	}
	
	class DataSetHandler extends XmlElementHandler<DataSet>{

		@Override
		public String getEName() {
			return EName.DataSet.getName();
		}

		public DataSetHandler(){
			addHandler(new DataDefinitionHandler());
		}
		
		@Override
		protected void initMyResult() {
			this.result = new DataSet();
		}

		@Override
		protected void putMyAttributes(Map<String,String> attrMap) {
			
			String name, value;
			
			// TODO やっぱりアノテーションの方が良いか...
			
			name = AName.DefaultDataProviderId.getName();
			value = attrMap.get(name);
			if( value != null ){
				this.result.setDefaultDataProviderId(value);
				attrMap.remove(name);
			}
			name = AName.DefaultScope.getName();

			value = attrMap.get(name);
			if( value != null ){
				DataScope scope = DataScope.parse(value);
				if( scope != null ){
					this.result.setDefaultScope(scope);
				}else{
					handleUnsupportedValue(name, value);
				}
				attrMap.remove(name);
			}
			
			handleUnsupportedAttribute(null, attrMap);
		}
		
		@Override
		protected void putChildElementValue(String eName, Map<String, String> attrMap, Object value) {
			if(isSameName(EName.DataDefinition, eName)){
				this.result.getDataDefinitionList().add((DataDefinition)value);
			}else{
				handleUnsupportedChildElement(eName);
			}
			handleUnsupportedAttribute(eName, attrMap);
		}
	}

	class DataDefinitionHandler extends XmlElementHandler<DataDefinition>{

		@Override
		public String getEName() {
			return EName.DataDefinition.getName();
		}

		@Override
		protected void initMyResult() {
			this.result = new DataDefinition();
		}

		@Override
		protected void putMyAttributes(Map<String,String> attrMap) {
			for(String key: attrMap.keySet()){
				String value = attrMap.get(key);
				if( isSameName(AName.DataProviderId, key)){
					this.result.setDataProviderId(value);
					attrMap.remove(key);
				}else if( isSameName(AName.Scope, key)){
					DataScope scope = DataScope.parse(value);
					if( scope != null ){
						this.result.setScope(scope);
					}else{
						handleUnsupportedValue(key, value);
					}
					attrMap.remove(key);
				}else{
					handleUnsupportedAttribute(null, key, value);
				}
			}
		}
		
		@Override
		protected void putChildElementValue(String eName, Map<String, String> attrMap, Object value) {

			if( isSameName(EName.Id, eName)){
				this.result.setId((String)value);
			}else if( isSameName(EName.Desc, eName)){
				this.result.setDescription((String)value);
			}else if( isSameName(EName.Define, eName)){
				this.result.setDefine((String)value);
			}else if( isSameName(EName.Scope, eName)){
				DataScope scope = DataScope.parse(value.toString());
				if( scope != null ){
					this.result.setScope(scope);
				}else{
					handleUnsupportedValue(eName, value);
				}
			}else if( isSameName(EName.Type, eName)){
				DataType type = DataType.parse((String)value);
				if( type != null ){
					this.result.setType(type);
				}else{
					handleUnsupportedValue(eName, value);
				}
				this.result.setType(type);
			}else{
				handleUnsupportedChildElement(eName);
			}
			handleUnsupportedAttribute(eName, attrMap);
		}
	}

}
