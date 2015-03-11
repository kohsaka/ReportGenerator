package hit.repgen.parser;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class XmlFileParser<T>{

	/** リソースID(XMLファイル名) */
	private String resourceId;
	
	/** リソース */
	private InputStream resource;
	
	/** 解析結果(トップレベル要素ハンドラの解析結果) */
	private T result;
	
	/** ロガー(サブクラスでも使用する想定) */
	protected Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	/**
	 * 要素や属性を定義するためのインターフェイス<br>
	 * このクラスを継承したクラスにおける、要素や属性を定義する列挙体で使用する想定。
	 * @author kohsaka.N0003
	 *
	 */
	interface NameDef{
		public String getName();
	}
	
	/**
	 * 初期化します。
	 * @param resourceId
	 * @throws IOException
	 */
	public void init(String resourceId) throws IOException{

		// XMLファイルのストリームを取得(クラスパスを探索)
		InputStream is = XmlGeneratorConfigReader.class.getClassLoader().getResourceAsStream(resourceId);
		if (is == null) {
			throw new IOException("No resouces found: " + resourceId);
		}
		
		// リソース情報として保持
		this.setResourceId(resourceId);
		this.resource = is;
	}
	
	/**
	 * 解析の起点となるXML文書のトップレベル要素ハンドラを指定します。
	 * @return
	 */
	protected abstract XmlElementHandler<T> getTopElementHandler();
	
	/**
	 * XML文書をロードして解析します。
	 * @throws XMLStreamException
	 */
	public void load() throws XMLStreamException{

		if (this.resource == null) {
			throw new RuntimeException("XmlConfigReader is not initialized");
		}

		// XMLファイル内容を検証
		// TODO 設定ファイルの検証

		// XMLパーサー(stax)
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader reader = factory.createXMLStreamReader(this.resource);

		// TODO CDATA[の読み取り
		// TODO グローバルのプロパティによる値置換
		
		// 設定情報を解析する。
		XmlElementHandler<T> topElementHandler = getTopElementHandler(); // 具象クラスで定義
		T result = (T)topElementHandler.parse(reader);
		this.result = result;
		
	}
	
	/**
	 * 全てのリソースを破棄します。
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
	 * 解析結果を取得します。
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
	 * @param resourceId セットする resourceId
	 */
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	
	//
	// ヘルパーメソッド
	//
	/**
	 * 要素名や属性名が引数と同一かを判定します。
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
