/**
 * 
 */
package hit.repgen.core;

import hit.repgen.config.ComponentSet;
import hit.repgen.config.DataConfig;
import hit.repgen.config.DataSet;
import hit.repgen.config.GeneratorConfig;
import hit.repgen.config.Resource;
import hit.repgen.datamodel.DataResult;
import hit.repgen.parser.XmlDataDefinitionReader;
import hit.repgen.parser.XmlGeneratorConfigReader;
import hit.repgen.renderer.RenderingTemplate;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kohsaka.N0003
 * 
 */
public class ReportGenerator {

	/** 既定の設定リソース(ファイル) */
	private static String GENERATOR_CONFIG_RESOURCE = "GeneratorConfig.xml";

	/** 既定のデータ定義リソース(ファイル) */
	private static String DATASET_RESOURCE = "DataDefinition.xml";

	/** ロガー */
	private static final Logger logger = LoggerFactory.getLogger(ReportGenerator.class);

	private RenderingManager renderingManager;

	private DataManager dataManager;

	public ReportGenerator() {
		this.renderingManager = null;
		this.dataManager = null;
	}

	/**
	 * 初期化処理
	 * @throws Exception
	 */
	public void init() throws Exception {

		// 初期化処理 =====================================

		// 設定情報をロード
		XmlGeneratorConfigReader generatorConfigReader = new XmlGeneratorConfigReader();
		generatorConfigReader.init(GENERATOR_CONFIG_RESOURCE);
		generatorConfigReader.load();
		GeneratorConfig generatorConfig = generatorConfigReader.getResult();
		generatorConfigReader.dispose();
		logger.info("generatorConfig: {}", generatorConfig);

		// データ定義をロード
		XmlDataDefinitionReader dataDefinitionReader = new XmlDataDefinitionReader();
		dataDefinitionReader.init(DATASET_RESOURCE);
		dataDefinitionReader.load();
		DataConfig dataConfig = dataDefinitionReader.getResult();
		dataDefinitionReader.dispose();
		logger.info("dataConfig: {}", generatorConfig);

		//
		RenderingManager renderingManager = new RenderingManager();
		DataManager dataManager = new DataManager();
		List<ComponentSet> componentSetList = generatorConfig.getComponentSetList();
		for (ComponentSet componentSet : componentSetList) {
			switch (componentSet.getType()) {
			case DataProvider:
				dataManager.addDataProviders(componentSet);
				break;
			case Renderer:
				renderingManager.addRenderers(componentSet);
				break;
			case UNKOWN:
				throw new Exception(); // TODO
			}
		}

		//
		for (DataSet ds : dataConfig.getDataSetList()) {
			dataManager.addDataSet(ds);
		}

		//
		dataManager.prepareStaticResult();

		this.dataManager = dataManager;
		this.renderingManager = renderingManager;
	}

	/**
	 * レポートを出力する。
	 * @param rendererId
	 * @param resource
	 * @return
	 * @throws Exception
	 */
	public InputStream render(String rendererId, Resource resource) throws Exception {

		// デバッグ
		// logger.debug("CONFIG : " + generatorConfig);
		// logger.debug("DATASET: " + dataConfig);

		dataManager.dump();
		dataManager.dumpRuntime();

		// 個別処理 =====================================

		RenderingTemplate template = renderingManager.generateTemplate(rendererId, resource);
		DataResult result = dataManager.getResult(template.getDataQueryList());
		InputStream resultIo = renderingManager.render(template, result);
		
		return resultIo;

	}

}
