package hit.repgen.dataprovider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hit.repgen.config.ComponentConfig;
import hit.repgen.config.DataDefinition;
import hit.repgen.config.DataType;
import hit.repgen.datamodel.ResultBase;
import hit.repgen.datamodel.ResultList;
import hit.repgen.datamodel.ResultTable;
import hit.repgen.datamodel.ResultTableRow;
import hit.repgen.datamodel.ResultValue;
import hit.repgen.util.ExceptionUtils;
import hit.repgen.util.StringUtils;

public class JdbcDataProvider implements DataProvider {

	/** 設定値 */
	private Properties prop = null;

	// TODO コネクションプーリング

	private Logger logger = LoggerFactory.getLogger(getClass());

	public JdbcDataProvider() {
		this.prop = null;
	}

	@Override
	public void init(ComponentConfig config) {

		Properties prop = config.getProperties();
		this.prop = prop;

		try(Connection conn = getConnection()) {
			// check only, and nothing to do
		} catch (Exception e) {
			throw new RuntimeException("initialization failed", e);
		}
	}

	@Override
	public boolean validate() {

		String validateQuery = prop.getProperty("validateQuery");
		if (StringUtils.isEmpty(validateQuery)) {
			return true;
		}

		try (Connection conn = getConnection()) {
			Statement stmt = conn.createStatement();
			stmt.executeQuery(validateQuery);
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	@Override
	public ResultBase getResult(DataDefinition def, Map<String, Object> params) {

		// TODO パラメータのマップ
		
		ResultBase result = null;
		String define = def.getDefine();
		DataType type = def.getType();
		
		try (Connection conn = getConnection()) {
			Statement stmt = conn.createStatement();
			logger.trace(StringUtils.toSingleLineString(define));
			ResultSet rs = stmt.executeQuery(define);
			ResultSetMetaData rsmd = rs.getMetaData();
			
			switch (type) {
			
			case VALUE:
				// (1,1)の値を検索結果とする
				if( rs.next() ){
					Object value = rs.getObject(1);
					result = new ResultValue(value);
				}else{
					result = null;
				}
				break;
				
			case LIST:
				// (1, N)の値を検索結果とする
				List<Object> list = new ArrayList<>();
				while(rs.next()){
					Object value = rs.getObject(1);
					list.add(value);
				}
				result = new ResultList(list);
				break;
			
			case TABLE:
				// 列名配列を生成
				int columnCount = rsmd.getColumnCount();
				String[] columnNames = new String[columnCount];
				for (int i = 0; i < columnCount; i++) {
					columnNames[i] = rsmd.getColumnLabel(i + 1);
				}
				// 検索結果をResultTable/Rowへマップ
				ResultTable table = new ResultTable(columnNames);
				while (rs.next()) {
					ResultTableRow row = table.newRow();
					for (int i = 0; i < columnCount; i++) {
						Object value = rs.getObject(i+1);
						row.setValue(i, value);
					}
					table.addRow(row);
				}
				result = table;
				break;
			
			default:
				logger.warn("unknown data type: {}", def);
				result = null;
			}
		} catch (Exception e) {
			logger.warn("Exception occured: {}", ExceptionUtils.getStackTrace(e));
			result = null;
		}
		return result;
	}

	@Override
	public void close() {
		// Nothing to do
	}

	protected Connection getConnection() throws SQLException {
		String url = prop.getProperty("url");
		String user = prop.getProperty("user");
		String password = prop.getProperty("password");
		return DriverManager.getConnection(url, user, password);
	}

}
