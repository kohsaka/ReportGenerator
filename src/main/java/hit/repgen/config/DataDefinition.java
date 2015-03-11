/**
 * 
 */
package hit.repgen.config;

import hit.repgen.util.StringUtils;

/**
 * データ定義を保持します。
 * 
 * @author kohsaka.N0003
 * 
 */
public class DataDefinition implements Cloneable {

	private String id;

	private DataType type;
	
	private String description;

	private String dataProviderId;

	private DataScope scope;

	private String define;

	/**
	 * @return id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            セットする id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return type
	 */
	public DataType getType() {
		return type;
	}

	/**
	 * @param type セットする type
	 */
	public void setType(DataType type) {
		this.type = type;
	}

	/**
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            セットする description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return dataProviderId
	 */
	public String getDataProviderId() {
		return dataProviderId;
	}

	/**
	 * @param dataProviderId
	 *            セットする dataProviderId
	 */
	public void setDataProviderId(String dataProviderId) {
		this.dataProviderId = dataProviderId;
	}

	/**
	 * @return scope
	 */
	public DataScope getScope() {
		return scope;
	}

	/**
	 * @param scope
	 *            セットする scope
	 */
	public void setScope(DataScope scope) {
		this.scope = scope;
	}

	/**
	 * @return define
	 */
	public String getDefine() {
		return define;
	}

	/**
	 * @param define
	 *            セットする define
	 */
	public void setDefine(String define) {
		this.define = define;
	}

	@Override
	public DataDefinition clone() {
		try {
			return (DataDefinition) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	/**
	 * 設定内容を文字列化します。
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName());
		sb.append("(");
		sb.append("id=\"" + id + "\"");
		sb.append(", description=\"" + ((description != null) ? description : "") + "\"");
		sb.append(", dataProviderId=" + dataProviderId);
		sb.append(", scope=" + scope);
		sb.append(", type=" + type);
		sb.append(", define=\"" + StringUtils.toSingleLineString(define) + "\"");
		sb.append(")");
		return sb.toString();
	}

}
