package hit.repgen.parser;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class XmlFileParser<T>{

	/** ���\�[�XID(XML�t�@�C����) */
	private String resourceId;
	
	/** ���\�[�X */
	private InputStream resource;
	
	/** ��͌���(�g�b�v���x���v�f�n���h���̉�͌���) */
	private T result;
	
	/** ���K�[(�T�u�N���X�ł��g�p����z��) */
	protected Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	/**
	 * �v�f�⑮�����`���邽�߂̃C���^�[�t�F�C�X<br>
	 * ���̃N���X���p�������N���X�ɂ�����A�v�f�⑮�����`����񋓑̂Ŏg�p����z��B
	 * @author kohsaka.N0003
	 *
	 */
	interface NameDef{
		public String getName();
	}
	
	/**
	 * ���������܂��B
	 * @param resourceId
	 * @throws IOException
	 */
	public void init(String resourceId) throws IOException{

		// XML�t�@�C���̃X�g���[�����擾(�N���X�p�X��T��)
		InputStream is = XmlGeneratorConfigReader.class.getClassLoader().getResourceAsStream(resourceId);
		if (is == null) {
			throw new IOException("No resouces found: " + resourceId);
		}
		
		// ���\�[�X���Ƃ��ĕێ�
		this.setResourceId(resourceId);
		this.resource = is;
	}
	
	/**
	 * ��͂̋N�_�ƂȂ�XML�����̃g�b�v���x���v�f�n���h�����w�肵�܂��B
	 * @return
	 */
	protected abstract XmlElementHandler<T> getTopElementHandler();
	
	/**
	 * XML���������[�h���ĉ�͂��܂��B
	 * @throws XMLStreamException
	 */
	public void load() throws XMLStreamException{

		if (this.resource == null) {
			throw new RuntimeException("XmlConfigReader is not initialized");
		}

		// XML�t�@�C�����e������
		// TODO �ݒ�t�@�C���̌���

		// XML�p�[�T�[(stax)
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader reader = factory.createXMLStreamReader(this.resource);

		// TODO CDATA[�̓ǂݎ��
		// TODO �O���[�o���̃v���p�e�B�ɂ��l�u��
		
		// �ݒ������͂���B
		XmlElementHandler<T> topElementHandler = getTopElementHandler(); // ��ۃN���X�Œ�`
		T result = (T)topElementHandler.parse(reader);
		this.result = result;
		
	}
	
	/**
	 * �S�Ẵ��\�[�X��j�����܂��B
	 */
	public void dispose(){
		try {
			if (this.resource != null) {
				this.resource.close();
			}
		} catch (Exception e) {
			// ignore
		}
		this.setResourceId(null);
		this.resource = null;
		this.result = null;
	}
	
	/**
	 * ��͌��ʂ��擾���܂��B
	 * @return
	 */
	public T getResult(){
		return this.result;
	}

	/**
	 * @return resourceId
	 */
	public String getResourceId() {
		return resourceId;
	}

	/**
	 * @param resourceId �Z�b�g���� resourceId
	 */
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	
	//
	// �w���p�[���\�b�h
	//
	/**
	 * �v�f���⑮�����������Ɠ��ꂩ�𔻒肵�܂��B
	 * @param def
	 * @param name
	 * @return
	 */
	protected boolean isSameName(NameDef def, String name){
		if( def == null || name == null ){
			return false;
		}
		return def.getName().equals(name);
	}
}
