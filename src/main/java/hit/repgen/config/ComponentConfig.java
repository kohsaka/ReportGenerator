/**
 * 
 */
package hit.repgen.config;

import java.util.Properties;

/**
 * �����_���\��f�[�^�v���o�C�_�[���̃R���|�[�l���g�̐ݒ����ێ����܂��B
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
	 * @param id �Z�b�g���� id
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
	 * @param name �Z�b�g���� name
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
	 * @param description �Z�b�g���� description
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
	 * @param className �Z�b�g���� className
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
	 * @param properties �Z�b�g���� properties
	 */
	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	/**
	 * �ݒ���e�𕶎��񉻂��܂��B
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
