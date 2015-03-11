/**
 * 
 */
package hit.repgen.core;

import hit.repgen.config.ComponentConfig;
import hit.repgen.config.ComponentSet;
import hit.repgen.config.ComponentSet.ComponentType;
import hit.repgen.config.DataDefinition;
import hit.repgen.config.DataScope;
import hit.repgen.config.DataSet;
import hit.repgen.datamodel.DataQuery;
import hit.repgen.datamodel.DataResult;
import hit.repgen.datamodel.ResultBase;
import hit.repgen.datamodel.ResultValue;
import hit.repgen.dataprovider.DataProvider;
import hit.repgen.exception.CodingException;
import hit.repgen.util.BeanUtils;
import hit.repgen.util.ExceptionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kohsaka.N0003
 * 
 */
public class DataManager {

	private boolean validateFirst = true;

	private Properties properties;

	private Map<String, ComponentConfig> dataProviderConfigMap;

	private Map<String, DataProvider> dataProviderMap;
	
	private String defaultDataProviderId;

	private Map<String, DataSet> dataSetMap;

	private Map<String, DataDefinition> dataDefinitionMap;

	private Map<String, ResultBase> resultMap;

	/** ���K�[ */
	private Logger logger = LoggerFactory.getLogger(getClass().getName());

	/**
	 * �R���X�g���N�^
	 */
	public DataManager() {
		this(new Properties());
	}

	/**
	 * �R���X�g���N�^
	 * 
	 * @param properties
	 */
	public DataManager(Properties properties) {
		if (properties == null) {
			throw new NullPointerException();
		}
		this.properties = properties;
		this.dataProviderConfigMap = new LinkedHashMap<>();
		this.dataProviderMap = new LinkedHashMap<>();
		this.defaultDataProviderId = null;
		this.dataSetMap = new LinkedHashMap<>();
		this.dataDefinitionMap = new HashMap<>();
		this.resultMap = new HashMap<>();
	}

	/**
	 * �f�[�^�v���o�C�_�[��ǉ����܂��B
	 * 
	 * @param config
	 */
	public int addDataProviders(ComponentSet componentSet) {

		int count = 0;

		ComponentType type = componentSet.getType();
		if( !ComponentSet.ComponentType.DataProvider.equals(componentSet.getType() )){
			throw new CodingException("unexpected ComponentType: " + type);
		}
		
		List<ComponentConfig> configList = componentSet.getComponentConfigList();
		for(ComponentConfig config: configList){
			
			String dataProviderId = config.getId();
			
			if (this.dataProviderConfigMap.containsKey(dataProviderId)) {
				logger.warn("duplicated data-provider-id and replaced: " + dataProviderId);
			}
			
			// �C���X�^���X��ێ�
			DataProvider dataProvider = createDataProvider(config);
			if (dataProvider != null) {
				logger.debug("added DataProvider instance: {}", dataProvider);
				this.dataProviderMap.put(dataProviderId, dataProvider);
				count++;
			}
			
			// �f�t�H���g�̃f�[�^�v���o�C�_������
			// TODO XML�^�O(����)�Ŏw��ł���悤��
			if( this.defaultDataProviderId == null ){
				this.defaultDataProviderId = dataProviderId;
			}
			
		}
		
		return count;
	}

	/**
	 * �ݒ���Ɋ�Â��ăf�[�^�v���o�C�_�̃C���X�^���X�𐶐����܂��B
	 * 
	 * @param config
	 * @return
	 * @throws Exception
	 */
	protected DataProvider createDataProvider(ComponentConfig config) {

		String id = config.getId();
		String className = config.getClassName();
		String logPrefix = String.format("data-provider-id=%s, className=%s", id, className);
		
		DataProvider dataProvider = null;
		Class<?> clazz = null;
		try {
			clazz = Class.forName(className);
		}catch(ClassNotFoundException e){
			logger.warn("class not found: {}", logPrefix);
			return null;
		}
		try{
			dataProvider = (DataProvider) clazz.newInstance();
		}catch(Exception e){
			logger.warn("invalid class definition: {}", logPrefix);
			return null;
		}
		try{
			dataProvider.init(config);
			if (!dataProvider.validate()) {
				logger.warn("DataProvider validation failed: {}", logPrefix);
				return null;
			}
		} catch (Exception e) {
			logger.warn("DataProvider initialization failed: " + ExceptionUtils.getStackTrace(e));
			destroyDataProvider(dataProvider);
			return null;
		}
		return dataProvider;
	}
	
	/**
	 * �f�[�^�v���o�C�_�C���X�^���X��j�����܂��B
	 * @param dataProvider
	 */
	protected void destroyDataProvider(DataProvider dataProvider){
		if( dataProvider != null ){
			try{
				dataProvider.close();
			}catch(Exception e){
				logger.debug(ExceptionUtils.getStackTrace(e));
			}
		}
	}

	/**
	 * �f�[�^��`��ǉ����܂��B
	 * 
	 * @param dataSet �f�[�^�Z�b�g
	 */
	public void addDataSet(DataSet dataSet) {

		// ����̃f�[�^�v���o�C�_ID������
		String dataSetDefaultDataProvider = dataSet.getDefaultDataProviderId();
		String defaultDataProviderId;
		if( StringUtils.isNotEmpty(dataSetDefaultDataProvider) ){
			defaultDataProviderId = dataSetDefaultDataProvider;
		}else{
			defaultDataProviderId = this.defaultDataProviderId;
		}
		
		// ����̃X�R�[�v������
		DataScope dataSetDefaultScope = dataSet.getDefaultScope();
		DataScope defaultScope;
		if( dataSetDefaultScope != null){
			defaultScope = dataSetDefaultScope;
		}else{
			defaultScope = DataScope.DYNAMIC;
		}

		// �f�[�^��`��ǉ�
		List<DataDefinition> list = dataSet.getDataDefinitionList();
		for (DataDefinition def : list) {
			addDataDefinition(defaultDataProviderId, defaultScope, def);
		}
	}

	/**
	 * �f�[�^��`��ǉ����܂��B
	 * 
	 * @param dataSet �f�[�^��`
	 */
	protected void addDataDefinition(String defaultDataProviderId, DataScope defaultScope, DataDefinition dataDefinition) {

		if (dataDefinition == null) {
			logger.warn("data-definition is null");
			return;
		}

		DataDefinition newDefinition = dataDefinition.clone();
				
		// ID�`�F�b�N
		String id = dataDefinition.getId();
		if( StringUtils.isEmpty(id) ){
			logger.warn("missing id: " + dataDefinition);
			return;
		}
		
		// �f�[�^�v���o�C�_����`�̏ꍇ�̓f�t�H���g�l��K�p
		if( StringUtils.isEmpty(newDefinition.getDataProviderId()) ){
			newDefinition.setDataProviderId(defaultDataProviderId);
		}
		
		// �X�R�[�v����`�̏ꍇ�̓f�t�H���g�l��K�p
		if( newDefinition.getScope() == null ){
			newDefinition.setScope(defaultScope);
		}
		
		// �d���`�F�b�N
		if (this.dataDefinitionMap.containsKey(id)) {
			logger.warn("duplicated data-definition-id and replaced it: " + id);
		}
		
		
		this.dataDefinitionMap.put(id, newDefinition);
	}

	/**
	 * STATIC�X�R�[�v�̃f�[�^���L���b�V�����܂��B
	 */
	public void prepareStaticResult(){
		int count = 0;
		for(String id: this.dataDefinitionMap.keySet()){
			DataDefinition def = this.dataDefinitionMap.get(id);
			if( DataScope.STATIC.equals(def.getScope())){
				getResult(id, null); // TODO null�ŗǂ��H
				count++;
			}
		}
		logger.info("Cached static data: {}", count);
	}
	
	/**
	 * DataQuery�ɑ΂���f�[�^���擾���܂��B
	 * @param queryList
	 * @return
	 */
	public DataResult getResult(List<DataQuery> queryList){
		DataResult dataResult = new DataResult();
		for(DataQuery query: queryList){
			String queryId = query.getId();
			String dataDefinitionId = query.getDataDefinitionId();
			Map<String, Object> params = query.getParamMap();
			ResultBase result = getResult(dataDefinitionId, params);
			dataResult.addResult(queryId, result);
		}
		return dataResult;
	}
	
	/**
	 * �f�[�^���擾���܂��B<br>
	 * ID�ɑΉ�����f�[�^����`����Ă���ꍇ�͂��̃f�[�^��ԋp���܂��B<br>
	 * �L���b�V���ɑ��݂���ꍇ�͂��̃f�[�^���A���݂��Ȃ��ꍇ�̓f�[�^�v���o�C�_�ɂ���f�[�^���擾���܂��B
	 * 
	 * @param dataDefinitionId
	 * @return �f�[�^(�Y���f�[�^���Ȃ��ꍇ��null)
	 */
	protected ResultBase getResult(String dataDefinitionId, Map<String, Object> params) {

		// ID�������ȏꍇ�͖������Ńf�[�^�͂Ȃ�
		if (dataDefinitionId == null || dataDefinitionId.length() < 1) {
			return null;
		}

		// �f�[�^��`�ɂȂ��ꍇ�͖������Ńf�[�^�Ȃ�
		DataDefinition dataDefinition = this.dataDefinitionMap.get(dataDefinitionId);
		if (dataDefinition == null) {
			return null;
		}

		// �L���b�V���ɂ���΂����ԋp
		ResultBase result = this.resultMap.get(dataDefinitionId);
		if (result != null) {
			logger.debug("cache hit: {1}", result); // result�ɃL�[���܂܂��z��
			return result;
		}

		// �f�[�^�v���o�C�_����f�[�^���擾
		String dataProviderId = dataDefinition.getDataProviderId();
		DataProvider dataProvider = this.dataProviderMap.get(dataProviderId);
		if( dataProvider == null ){
			logger.warn("no such data-provider: {}", dataDefinition);
		}
		result = dataProvider.getResult(dataDefinition, params);
		
		// �擾�f�[�^�̊m�F
		if( result == null || !(result instanceof ResultBase)){
			logger.warn("DataProvider return unexpected result: " + result);
			result = new ResultValue(null);
		}
		
		// �X�R�[�v��static�̏ꍇ�̓L���b�V��
		if( DataScope.STATIC.equals(dataDefinition.getScope())){
			logger.debug("cached : {}", dataDefinitionId);
			this.resultMap.put(dataDefinitionId, result);
		}
		
		return result;
	}

	/**
	 * ���̃N���X�̕ێ��������O�Ƀ_���v����B
	 */
	public void dump(){

		logger.info("Definition of {} ", getClass().getSimpleName());
		
		logger.info("DataProvider instances: {}", this.dataProviderMap.size());
		for(String key: this.dataProviderMap.keySet()){
			DataProvider value = this.dataProviderMap.get(key);
			String marked = (value.equals(this.defaultDataProviderId)) ? "* ": "";
			logger.info("-> {}data-provider-id={}, instance={}", marked, key, value);
		}
		
		logger.info("DataDefinitions: {}", this.dataDefinitionMap.size());
		for(String key: this.dataDefinitionMap.keySet()){
			DataDefinition value = this.dataDefinitionMap.get(key);
			logger.info("-> data-definition-id={}, instance={}", key, value);
		}
	}
	
	/**
	 * ���̃N���X�̎��s���������O�Ƀ_���v����B
	 */
	public void dumpRuntime(){
		logger.info("Runtime of {} ", getClass().getSimpleName());

		logger.info("Cached result: {}", this.resultMap.size());
		for(String key: this.resultMap.keySet()){
			ResultBase value = this.resultMap.get(key);
			logger.info("-> data-definition-id={}, instance={}", key, value);
		}
	}
}
