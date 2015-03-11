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

	/** ロガー */
	private Logger logger = LoggerFactory.getLogger(getClass().getName());

	/**
	 * コンストラクタ
	 */
	public DataManager() {
		this(new Properties());
	}

	/**
	 * コンストラクタ
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
	 * データプロバイダーを追加します。
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
			
			// インスタンスを保持
			DataProvider dataProvider = createDataProvider(config);
			if (dataProvider != null) {
				logger.debug("added DataProvider instance: {}", dataProvider);
				this.dataProviderMap.put(dataProviderId, dataProvider);
				count++;
			}
			
			// デフォルトのデータプロバイダを決定
			// TODO XMLタグ(属性)で指定できるように
			if( this.defaultDataProviderId == null ){
				this.defaultDataProviderId = dataProviderId;
			}
			
		}
		
		return count;
	}

	/**
	 * 設定情報に基づいてデータプロバイダのインスタンスを生成します。
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
	 * データプロバイダインスタンスを破棄します。
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
	 * データ定義を追加します。
	 * 
	 * @param dataSet データセット
	 */
	public void addDataSet(DataSet dataSet) {

		// 既定のデータプロバイダIDを決定
		String dataSetDefaultDataProvider = dataSet.getDefaultDataProviderId();
		String defaultDataProviderId;
		if( StringUtils.isNotEmpty(dataSetDefaultDataProvider) ){
			defaultDataProviderId = dataSetDefaultDataProvider;
		}else{
			defaultDataProviderId = this.defaultDataProviderId;
		}
		
		// 既定のスコープを決定
		DataScope dataSetDefaultScope = dataSet.getDefaultScope();
		DataScope defaultScope;
		if( dataSetDefaultScope != null){
			defaultScope = dataSetDefaultScope;
		}else{
			defaultScope = DataScope.DYNAMIC;
		}

		// データ定義を追加
		List<DataDefinition> list = dataSet.getDataDefinitionList();
		for (DataDefinition def : list) {
			addDataDefinition(defaultDataProviderId, defaultScope, def);
		}
	}

	/**
	 * データ定義を追加します。
	 * 
	 * @param dataSet データ定義
	 */
	protected void addDataDefinition(String defaultDataProviderId, DataScope defaultScope, DataDefinition dataDefinition) {

		if (dataDefinition == null) {
			logger.warn("data-definition is null");
			return;
		}

		DataDefinition newDefinition = dataDefinition.clone();
				
		// IDチェック
		String id = dataDefinition.getId();
		if( StringUtils.isEmpty(id) ){
			logger.warn("missing id: " + dataDefinition);
			return;
		}
		
		// データプロバイダ未定義の場合はデフォルト値を適用
		if( StringUtils.isEmpty(newDefinition.getDataProviderId()) ){
			newDefinition.setDataProviderId(defaultDataProviderId);
		}
		
		// スコープ未定義の場合はデフォルト値を適用
		if( newDefinition.getScope() == null ){
			newDefinition.setScope(defaultScope);
		}
		
		// 重複チェック
		if (this.dataDefinitionMap.containsKey(id)) {
			logger.warn("duplicated data-definition-id and replaced it: " + id);
		}
		
		
		this.dataDefinitionMap.put(id, newDefinition);
	}

	/**
	 * STATICスコープのデータをキャッシュします。
	 */
	public void prepareStaticResult(){
		int count = 0;
		for(String id: this.dataDefinitionMap.keySet()){
			DataDefinition def = this.dataDefinitionMap.get(id);
			if( DataScope.STATIC.equals(def.getScope())){
				getResult(id, null); // TODO nullで良い？
				count++;
			}
		}
		logger.info("Cached static data: {}", count);
	}
	
	/**
	 * DataQueryに対するデータを取得します。
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
	 * データを取得します。<br>
	 * IDに対応するデータが定義されている場合はそのデータを返却します。<br>
	 * キャッシュに存在する場合はそのデータを、存在しない場合はデータプロバイダにからデータを取得します。
	 * 
	 * @param dataDefinitionId
	 * @return データ(該当データがない場合はnull)
	 */
	protected ResultBase getResult(String dataDefinitionId, Map<String, Object> params) {

		// IDが無効な場合は無条件でデータはなし
		if (dataDefinitionId == null || dataDefinitionId.length() < 1) {
			return null;
		}

		// データ定義にない場合は無条件でデータなし
		DataDefinition dataDefinition = this.dataDefinitionMap.get(dataDefinitionId);
		if (dataDefinition == null) {
			return null;
		}

		// キャッシュにあればそれを返却
		ResultBase result = this.resultMap.get(dataDefinitionId);
		if (result != null) {
			logger.debug("cache hit: {1}", result); // resultにキーが含まれる想定
			return result;
		}

		// データプロバイダからデータを取得
		String dataProviderId = dataDefinition.getDataProviderId();
		DataProvider dataProvider = this.dataProviderMap.get(dataProviderId);
		if( dataProvider == null ){
			logger.warn("no such data-provider: {}", dataDefinition);
		}
		result = dataProvider.getResult(dataDefinition, params);
		
		// 取得データの確認
		if( result == null || !(result instanceof ResultBase)){
			logger.warn("DataProvider return unexpected result: " + result);
			result = new ResultValue(null);
		}
		
		// スコープがstaticの場合はキャッシュ
		if( DataScope.STATIC.equals(dataDefinition.getScope())){
			logger.debug("cached : {}", dataDefinitionId);
			this.resultMap.put(dataDefinitionId, result);
		}
		
		return result;
	}

	/**
	 * このクラスの保持情報をログにダンプする。
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
	 * このクラスの実行時情報をログにダンプする。
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
