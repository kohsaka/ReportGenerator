package hit.repgen.parser;

import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XML文書上の単一対象要素を解析するための抽象クラス<br>
 * 対象要素の直下にある要素(子要素なし)の場合、putElementValue()が実行されます。<br>
 * 対象要素の直下にあるネストされた要素名に対応する要素ハンドラ(このクラスで再帰的に)で処理します。<br>
 * 対象要素の解析結果として返却するクラスを仮型引数Tに指定してください。
 * <p>
 * 同一階層に並ぶ複数の同一要素を解析する場合、その要素の一つ上のレベルでリスト化してください。<br>
 * このクラスはあくまでも単一要素を解析する目的で設計されています。
 * 
 * @author kohsaka.N0003
 * 
 */
public abstract class XmlElementHandler<T> {

		/** 解析結果のマップ先クラス(解析結果インスタンス) */
		protected T result;
		
		/**
		 * 処理対象の要素において、さらに解析が必要な要素のハンドラ<br>
		 * 子要素に対応するハンドラであるため、このクラスの仮型引数Tとは関係なく、<br>
		 * 推測はできないので仮型引数Tの指定は行わず、@SuppressWarningsしています。
		 */
		@SuppressWarnings("rawtypes")
		private Map<String, XmlElementHandler> handlerMap = new HashMap<>();

		/** ロガー(サブクラスで使用する想定) */
		protected Logger logger = LoggerFactory.getLogger(this.getClass().getName());

		/**
		 * 要素ハンドラを追加する。
		 * 
		 * @param handler
		 *            要素ハンドラ
		 */
		@SuppressWarnings("rawtypes")
		protected void addHandler(XmlElementHandler handler) {
			this.handlerMap.put(handler.getEName(), handler);
		}

		/**
		 * 処理対象要素名(物理名)。
		 * 
		 * @return 要素名(物理名)
		 */
		public abstract String getEName();

		/**
		 * 処理対象要素を解析します。<br>
		 * 処理対象要素に対する属性値、子要素の属性値／値が指定された場合、所定のメソッドを実行します。
		 * 
		 * @param reader
		 *            パーサー
		 * @param attributes
		 *            処理対象要素の属性値。未指定の場合はnullではなく空。
		 * @return 解析結果を表現するオブジェクト
		 * @throws XMLStreamException
		 *             解析時例外
		 */
		@SuppressWarnings("rawtypes")
		public T parse(XMLStreamReader reader) throws XMLStreamException {

			logger.debug("Start parsing...");
			
			initMyResult();

			// この要素自体の属性を取得し、具象クラスに処理をさせる
			// 属性未定義であっても検証のために具象クラスを実行する。
			Map<String, String> myAttributes = parseAttributes(reader);
			putMyAttributes(myAttributes); // 具象クラスで処理

			String variableName = null;
			Map<String, String> attrs = null;
			while (reader.hasNext()) {

				reader.next();
				int eType = reader.getEventType();
				switch (eType) {

				case XMLStreamConstants.START_ELEMENT:
					String eName = reader.getLocalName();
					XmlElementHandler handler = handlerMap.get(eName);
					if (handler != null) {
						Object result = handler.parse(reader);
						putChildElementValue(eName, createEmptyAttributes(), result); // 具象クラスで処理
						variableName = null;
						attrs = null;
					} else {
						variableName = eName;
						attrs = parseAttributes(reader);
					}
					break;

				case XMLStreamConstants.CHARACTERS:

					if (reader.isWhiteSpace() || variableName == null) {
						continue;
					}
					String value = reader.getText().trim();
					putChildElementValue(variableName, attrs, value); // 具象クラスで処理
					variableName = null;
					attrs = null;
					break;

				case XMLStreamConstants.END_ELEMENT:

					if (getEName().equals(reader.getLocalName())) {
						logger.debug("Exit parsing...");
						return this.result;
					}

				default:
					// それ以外は無視
				}
			}

			logger.debug("End parsing...");
			return result;
		}
		
		
		/**
		 * 要素に付与された属性値を取得する。<br>
		 * @param reader
		 *            パーサー
		 * @return 属性がない場合はnull
		 */
		protected Map<String, String> parseAttributes(XMLStreamReader reader) {
			Map<String, String> map = createEmptyAttributes();

			// 読み込み状態がが要素開始ではない場合、属性に関する操作はできないので終了
			if( !reader.isStartElement() ){
				return map;
			}
			
			for (int i = 0; i < reader.getAttributeCount(); i++) {
				String attrName = reader.getAttributeLocalName(i);
				String attrValue = reader.getAttributeValue(i);
				map.put(attrName, attrValue);
			}
			return map;
		}

		/**
		 * 空の属性値を生成します。
		 * @return
		 */
		protected Map<String, String> createEmptyAttributes(){
			return new HashMap<>();
		}

		/**
		 * この要素の解析結果を格納するオブジェクトを初期化します。<br>
		 * 複数回実行されることを考慮し、コール毎にインスタンスを作り直してください。
		 */
		protected abstract void initMyResult();

		/**
		 * この要素に付加された属性値を設定します。<br>
		 * 既定では全ての属性値を未対応属性として処理します。
		 * 属性値を利用したい場合はオーバーライドしたメソッドにてその処理を実装してください。
		 * 
		 * @param attributeMap
		 */
		protected void putMyAttributes(Map<String, String> attrMap) {
			handleUnsupportedAttribute(null, attrMap);
		}

		/**
		 * 子要素の値を保持するための処理を定義します。<br>
		 * 
		 * 値を保持したい場合はオーバーライドしてください。
		 * 
		 * @param eName
		 * @param value
		 */
		protected void putChildElementValue(String eName, Map<String, String> attrMap, Object value) {
			// 既定では全ての子要素とその属性を未対応とする
			handleUnsupportedChildElement(eName);
			handleUnsupportedAttribute(eName, attrMap);
		}

		/**
		 * 未対応の属性を検出した場合の処理を行います。
		 * @param childEName
		 * @param attrMap
		 */
		protected void handleUnsupportedAttribute(String childEName, Map<String, String> attrMap){
			if( attrMap == null ) return;
			for(String aName: attrMap.keySet()){
				String value = attrMap.get(aName);
				handleUnsupportedAttribute(childEName, aName, value);
			}
		}

		/**
		 * 未対応の属性を検出した場合の処理を行います。
		 * @param childEName
		 * @param value
		 */
		protected void handleUnsupportedAttribute(String childEName, String aName, String value){
			if( childEName == null ){
				logger.warn("Unsupported attribute: {}={}", aName, value);
			}else{
				logger.warn("Unsupported attribute in {}: {}={}", childEName, aName, value);
			}
		}
		
		/**
		 * 必須属性の未指定を検出した場合の処理を行います。
		 * @param childEName
		 * @param aName
		 */
		protected void handleRequiredAttribute(String childEName, String aName){
			if( childEName == null ){
				logger.warn("Attribute is required: {}", aName);
			}else{
				logger.warn("Attribute is required in {}: {}", childEName, aName);
			}
		}

		/**
		 * 未対応の子要素を検出した場合の処理を行います。
		 * @param eName
		 */
		protected void handleUnsupportedChildElement(String eName){
			logger.debug("Unsupported child element: {}", eName);
		}

		/**
		 * 未対応の値(要素又は属性値)を検出した場合の処理を行います。<br>
		 * @param target 要素名又は属性名
		 * @param value 値
		 */
		protected void handleUnsupportedValue(String target, Object value){
			logger.warn("Unsupported value: {}={}", target, (value != null) ? value.toString(): null);
		}
		
}
