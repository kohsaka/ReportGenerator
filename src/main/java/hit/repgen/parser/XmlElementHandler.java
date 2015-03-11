package hit.repgen.parser;

import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XML������̒P��Ώۗv�f����͂��邽�߂̒��ۃN���X<br>
 * �Ώۗv�f�̒����ɂ���v�f(�q�v�f�Ȃ�)�̏ꍇ�AputElementValue()�����s����܂��B<br>
 * �Ώۗv�f�̒����ɂ���l�X�g���ꂽ�v�f���ɑΉ�����v�f�n���h��(���̃N���X�ōċA�I��)�ŏ������܂��B<br>
 * �Ώۗv�f�̉�͌��ʂƂ��ĕԋp����N���X�����^����T�Ɏw�肵�Ă��������B
 * <p>
 * ����K�w�ɕ��ԕ����̓���v�f����͂���ꍇ�A���̗v�f�̈��̃��x���Ń��X�g�����Ă��������B<br>
 * ���̃N���X�͂����܂ł��P��v�f����͂���ړI�Ő݌v����Ă��܂��B
 * 
 * @author kohsaka.N0003
 * 
 */
public abstract class XmlElementHandler<T> {

		/** ��͌��ʂ̃}�b�v��N���X(��͌��ʃC���X�^���X) */
		protected T result;
		
		/**
		 * �����Ώۂ̗v�f�ɂ����āA����ɉ�͂��K�v�ȗv�f�̃n���h��<br>
		 * �q�v�f�ɑΉ�����n���h���ł��邽�߁A���̃N���X�̉��^����T�Ƃ͊֌W�Ȃ��A<br>
		 * �����͂ł��Ȃ��̂ŉ��^����T�̎w��͍s�킸�A@SuppressWarnings���Ă��܂��B
		 */
		@SuppressWarnings("rawtypes")
		private Map<String, XmlElementHandler> handlerMap = new HashMap<>();

		/** ���K�[(�T�u�N���X�Ŏg�p����z��) */
		protected Logger logger = LoggerFactory.getLogger(this.getClass().getName());

		/**
		 * �v�f�n���h����ǉ�����B
		 * 
		 * @param handler
		 *            �v�f�n���h��
		 */
		@SuppressWarnings("rawtypes")
		protected void addHandler(XmlElementHandler handler) {
			this.handlerMap.put(handler.getEName(), handler);
		}

		/**
		 * �����Ώۗv�f��(������)�B
		 * 
		 * @return �v�f��(������)
		 */
		public abstract String getEName();

		/**
		 * �����Ώۗv�f����͂��܂��B<br>
		 * �����Ώۗv�f�ɑ΂��鑮���l�A�q�v�f�̑����l�^�l���w�肳�ꂽ�ꍇ�A����̃��\�b�h�����s���܂��B
		 * 
		 * @param reader
		 *            �p�[�T�[
		 * @param attributes
		 *            �����Ώۗv�f�̑����l�B���w��̏ꍇ��null�ł͂Ȃ���B
		 * @return ��͌��ʂ�\������I�u�W�F�N�g
		 * @throws XMLStreamException
		 *             ��͎���O
		 */
		@SuppressWarnings("rawtypes")
		public T parse(XMLStreamReader reader) throws XMLStreamException {

			logger.debug("Start parsing...");
			
			initMyResult();

			// ���̗v�f���̂̑������擾���A��ۃN���X�ɏ�����������
			// ��������`�ł����Ă����؂̂��߂ɋ�ۃN���X�����s����B
			Map<String, String> myAttributes = parseAttributes(reader);
			putMyAttributes(myAttributes); // ��ۃN���X�ŏ���

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
						putChildElementValue(eName, createEmptyAttributes(), result); // ��ۃN���X�ŏ���
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
					putChildElementValue(variableName, attrs, value); // ��ۃN���X�ŏ���
					variableName = null;
					attrs = null;
					break;

				case XMLStreamConstants.END_ELEMENT:

					if (getEName().equals(reader.getLocalName())) {
						logger.debug("Exit parsing...");
						return this.result;
					}

				default:
					// ����ȊO�͖���
				}
			}

			logger.debug("End parsing...");
			return result;
		}
		
		
		/**
		 * �v�f�ɕt�^���ꂽ�����l���擾����B<br>
		 * @param reader
		 *            �p�[�T�[
		 * @return �������Ȃ��ꍇ��null
		 */
		protected Map<String, String> parseAttributes(XMLStreamReader reader) {
			Map<String, String> map = createEmptyAttributes();

			// �ǂݍ��ݏ�Ԃ����v�f�J�n�ł͂Ȃ��ꍇ�A�����Ɋւ��鑀��͂ł��Ȃ��̂ŏI��
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
		 * ��̑����l�𐶐����܂��B
		 * @return
		 */
		protected Map<String, String> createEmptyAttributes(){
			return new HashMap<>();
		}

		/**
		 * ���̗v�f�̉�͌��ʂ��i�[����I�u�W�F�N�g�����������܂��B<br>
		 * ��������s����邱�Ƃ��l�����A�R�[�����ɃC���X�^���X����蒼���Ă��������B
		 */
		protected abstract void initMyResult();

		/**
		 * ���̗v�f�ɕt�����ꂽ�����l��ݒ肵�܂��B<br>
		 * ����ł͑S�Ă̑����l�𖢑Ή������Ƃ��ď������܂��B
		 * �����l�𗘗p�������ꍇ�̓I�[�o�[���C�h�������\�b�h�ɂĂ��̏������������Ă��������B
		 * 
		 * @param attributeMap
		 */
		protected void putMyAttributes(Map<String, String> attrMap) {
			handleUnsupportedAttribute(null, attrMap);
		}

		/**
		 * �q�v�f�̒l��ێ����邽�߂̏������`���܂��B<br>
		 * 
		 * �l��ێ��������ꍇ�̓I�[�o�[���C�h���Ă��������B
		 * 
		 * @param eName
		 * @param value
		 */
		protected void putChildElementValue(String eName, Map<String, String> attrMap, Object value) {
			// ����ł͑S�Ă̎q�v�f�Ƃ��̑����𖢑Ή��Ƃ���
			handleUnsupportedChildElement(eName);
			handleUnsupportedAttribute(eName, attrMap);
		}

		/**
		 * ���Ή��̑��������o�����ꍇ�̏������s���܂��B
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
		 * ���Ή��̑��������o�����ꍇ�̏������s���܂��B
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
		 * �K�{�����̖��w������o�����ꍇ�̏������s���܂��B
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
		 * ���Ή��̎q�v�f�����o�����ꍇ�̏������s���܂��B
		 * @param eName
		 */
		protected void handleUnsupportedChildElement(String eName){
			logger.debug("Unsupported child element: {}", eName);
		}

		/**
		 * ���Ή��̒l(�v�f���͑����l)�����o�����ꍇ�̏������s���܂��B<br>
		 * @param target �v�f�����͑�����
		 * @param value �l
		 */
		protected void handleUnsupportedValue(String target, Object value){
			logger.warn("Unsupported value: {}={}", target, (value != null) ? value.toString(): null);
		}
		
}
