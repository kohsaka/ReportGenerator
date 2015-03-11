package hit.repgen.config;

/**
 * データの有効スコープ。
 * @author kohsaka.N0003
 *
 */
public enum DataType {
	VALUE,
	LIST,
	TABLE;
	
	/**
	 * 文字列から値を取得します。<br>
	 * @param name 名称
	 * @param defaultName 解析不可時に返却する値
	 * @return スコープ
	 */
	public static DataType parse(String name){
		if( name == null ) return null;
		DataType[] vals = {VALUE, LIST, TABLE};
		for(DataType type: vals){
			if( type.toString().toLowerCase().equals(name)){
				return type;
			}
		}
		return null;
	}
}
