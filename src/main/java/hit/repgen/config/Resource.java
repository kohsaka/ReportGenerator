package hit.repgen.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * 主にテンプレートとなるファイルやURLを表現します。
 * 
 * @author kohsaka.N0003
 * 
 */
public class Resource {

	/**
	 * リソースタイプ
	 * 
	 * @author kohsaka.N0003
	 * 
	 */
	public enum Type {
		/** ローカルファイル */
		LocalFile,
		/** URL */
		Url
	};

	/** リソースタイプ */
	private Type type;

	/** ファイル名 */
	private String filename;

	/** URL */
	private URL url;

	/** コンストラクタ */
	public Resource(Type type, String resource) {
		setResource(type, resource);
	}

	/**
	 * リソースを設定します。
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
	 * リソースタイプを取得します。
	 * @return
	 */
	public Type getType() {
		return this.type;
	}

	/**
	 * リソースの入力ストリームを取得します。
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
