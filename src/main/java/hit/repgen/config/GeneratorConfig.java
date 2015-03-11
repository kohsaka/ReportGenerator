/**
 * 
 */
package hit.repgen.config;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * ���|�[�g�W�F�l���[�^�̐ݒ����ێ����܂��B
 * @author kohsaka.N0003
 *
 */
public class GeneratorConfig {

	/** �A�v���P�[�V�����v���p�e�B */
	private Properties properties;
	
	/** �R���|�[�l���g�Z�b�g�̃��X�g */
	private List<ComponentSet> componentSetList;
	
	/** �R���X�g���N�^ */
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
	 * @param properties �Z�b�g���� properties
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
	 * @param componentSetList �Z�b�g���� componentSetList
	 */
	public void setComponentSetList(List<ComponentSet> componentSetList) {
		this.componentSetList = componentSetList;
	}

	/**
	 * �ݒ���e�𕶎��񉻂��܂��B
	 */
	public String toString(){
		return MessageFormat.format("{0}({1}): [{2}], ", getClass().getSimpleName(), properties, this.componentSetList);
	}

	
}
