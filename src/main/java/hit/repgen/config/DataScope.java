package hit.repgen.config;

/**
 * データの有効スコープ。
 * @author kohsaka.N0003
 *
 */
public enum DataScope {
	NON, // なし(通常使用しません) 
	STATIC, // 静的データ
	DYNAMIC; // 動的データ
	
	/**
	 * 文字列から値を取得します。<br>
	 * @param name 名称
	 * @param defaultName 解析不可時に返却する値
	 * @return スコープ
	 */
	public static DataScope parse(String name){
		if( name == null ) return null;
		DataScope[] vals = {STATIC, DYNAMIC};
		for(DataScope scope: vals){
			if( scope.toString().toLowerCase().equals(name)){
				return scope;
			}
		}
		return null;

	}
}
