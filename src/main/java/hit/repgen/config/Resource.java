package hit.repgen.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * ��Ƀe���v���[�g�ƂȂ�t�@�C����URL��\�����܂��B
 * 
 * @author kohsaka.N0003
 * 
 */
public class Resource {

	/**
	 * ���\�[�X�^�C�v
	 * 
	 * @author kohsaka.N0003
	 * 
	 */
	public enum Type {
		/** ���[�J���t�@�C�� */
		LocalFile,
		/** URL */
		Url
	};

	/** ���\�[�X�^�C�v */
	private Type type;

	/** �t�@�C���� */
	private String filename;

	/** URL */
	private URL url;

	/** �R���X�g���N�^ */
	public Resource(Type type, String resource) {
		setResource(type, resource);
	}

	/**
	 * ���\�[�X��ݒ肵�܂��B
	 * 
	 * @param type
	 * @param resource
	 */
	public void setResource(Type type, String resource) {
		if (Type.LocalFile.equals(type)) {
			this.type = Type.LocalFile;
			this.filename = resource;
			this.url = null;
		} else if (Type.Url.equals(type)) {
			this.type = Type.Url;
			this.filename = null;
			try {
				this.url = new URL(resource);
			} catch (Exception e) {
				throw new IllegalArgumentException("invalid resource url: " + resource);
			}
		} else {
			throw new IllegalArgumentException("invalid resource type: " + type);
		}
	}

	/**
	 * ���\�[�X�^�C�v���擾���܂��B
	 * @return
	 */
	public Type getType() {
		return this.type;
	}

	/**
	 * ���\�[�X�̓��̓X�g���[�����擾���܂��B
	 * @return
	 * @throws IOException
	 */
	public InputStream getInputStream() throws IOException {
		if (Type.LocalFile.equals(this.type)) {
			return new FileInputStream(this.filename);
		} else if (Type.Url.equals(this.type)) {
			URLConnection conn = this.url.openConnection();
			return conn.getInputStream();
		} else {
			throw new IllegalArgumentException("invalid resource type: " + type);
		}
	}

}
