/**
 * 
 */
package hit.repgen.config;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * レポートジェネレータの設定情報を保持します。
 * @author kohsaka.N0003
 *
 */
public class GeneratorConfig {

	/** アプリケーションプロパティ */
	private Properties properties;
	
	/** コンポーネントセットのリスト */
	private List<ComponentSet> componentSetList;
	
	/** コンストラクタ */
	public GeneratorConfig(){
		this.properties = new Properties();
		this.componentSetList = new ArrayList<>();
	}
	
	/**
	 * @return properties
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * @param properties セットする properties
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	/**
	 * @return componentSetList
	 */
	public List<ComponentSet> getComponentSetList() {
		return componentSetList;
	}

	/**
	 * @param componentSetList セットする componentSetList
	 */
	public void setComponentSetList(List<ComponentSet> componentSetList) {
		this.componentSetList = componentSetList;
	}

	/**
	 * 設定内容を文字列化します。
	 */
	public String toString(){
		return MessageFormat.format("{0}({1}): [{2}], ", getClass().getSimpleName(), properties, this.componentSetList);
	}

	
}
