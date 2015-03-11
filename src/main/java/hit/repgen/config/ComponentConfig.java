/**
 * 
 */
package hit.repgen.config;

import java.util.Properties;

/**
 * レンダラ―やデータプロバイダー等のコンポーネントの設定情報を保持します。
 * @author kohsaka.N0003
 *
 */
public class ComponentConfig {

	private String id;
	
	private String name;
	
	private String description;
	
	private String className;
	
	private Properties properties;

	public ComponentConfig(){
		this.properties =new Properties();
	}
	
	/**
	 * @return id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id セットする id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name セットする name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description セットする description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return className
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @param className セットする className
	 */
	public void setClassName(String className) {
		this.className = className;
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
	 * 設定内容を文字列化します。
	 */
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("id=" + id);
		sb.append(", name=" + name);
		sb.append(", description=" + description);
		sb.append(", className=" + className);
		sb.append(", properties=" + properties);
		return sb.toString();
	}
}
