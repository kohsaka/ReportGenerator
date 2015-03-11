package hit.repgen.dataprovider;

import java.util.Map;

import hit.repgen.config.ComponentConfig;
import hit.repgen.config.DataDefinition;
import hit.repgen.datamodel.ResultBase;

public interface DataProvider {

	/**
	 * 初期化します。
	 */
	public void init(ComponentConfig config);
	
	/**
	 * このプロバイダが有効化どうかを検証します。<br>
	 * 検証内容は、このインターフェイスを実装したクラスに委ねられます。
	 */
	public boolean validate();
	
	/**
	 * データソースから定義に沿ったデータを取得します。
	 * @param define 定義
	 * @return 検索結果
	 */
	public ResultBase getResult(DataDefinition def, Map<String, Object> params);
	
	/**
	 * 終了処理を行います。
	 */
	public void close();

}
