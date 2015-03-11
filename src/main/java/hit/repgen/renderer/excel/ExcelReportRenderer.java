package hit.repgen.renderer.excel;

import hit.repgen.config.Resource;
import hit.repgen.datamodel.DataQuery;
import hit.repgen.datamodel.DataResult;
import hit.repgen.datamodel.ResultBase;
import hit.repgen.datamodel.ResultList;
import hit.repgen.datamodel.ResultTable;
import hit.repgen.datamodel.ResultTableRow;
import hit.repgen.datamodel.ResultValue;
import hit.repgen.exception.CodingException;
import hit.repgen.renderer.Renderer;
import hit.repgen.renderer.RenderingTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.record.TableRecord;
import org.apache.poi.ss.formula.functions.Value;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcelReportRenderer implements Renderer {

	protected final static String PK_DATA_SHEET_NAME = "data-sheet-name";

	protected final static String PK_OUTPUT_ACTIVE_SHEET_INDEX = "output-active-sheet-index";

	protected final static String PLACEHOLDER_BEGIN = "::";

	protected final static int DEFAULT_FILESIZE = 1 * 1024 * 1024; // 1[MByte]

	/** レンダラ―ID */
	protected String rendererId;

	/** プレースホルダ探索対象とするシート名 */
	protected String dataSheetName;

	/** 生成ファイルのアクティブシートのインデックス */
	protected int outputActiveSheetIndex;

	/** ロガー */
	protected Logger logger = LoggerFactory.getLogger(getClass());

	/** コンストラクタ */
	public ExcelReportRenderer() {

		// プロパティ値がない場合の値(既定値)
		this.dataSheetName = "データ";
		this.outputActiveSheetIndex = 0; // 先頭
	}

	@Override
	public void init(String myRendererId, Properties prop) {

		this.rendererId = myRendererId;

		if (prop == null) {
			return;
		}

		String dataSheetName = prop.getProperty(PK_DATA_SHEET_NAME);
		if (StringUtils.isNotEmpty(dataSheetName)) {
			this.dataSheetName = dataSheetName;
		}

		String outputActiveSheetIndexStr = prop.getProperty(PK_OUTPUT_ACTIVE_SHEET_INDEX);
		try {
			if (StringUtils.isNotEmpty(outputActiveSheetIndexStr)) {
				int outputActiveSheetIndex = Integer.parseInt(outputActiveSheetIndexStr);
				this.outputActiveSheetIndex = outputActiveSheetIndex;
			}
		} catch (NumberFormatException e) {
			logger.warn("invalid property: {}={}", PK_OUTPUT_ACTIVE_SHEET_INDEX, outputActiveSheetIndexStr);
		}

	}

	@Override
	public boolean validate() {
		return true;
	}

	@Override
	public RenderingTemplate generateTemplate(Resource resource) throws Exception {

		InputStream is = null;
		XSSFWorkbook wb = null;
		try {

			is = resource.getInputStream(); // TODO キャッシュ
			wb = new XSSFWorkbook(is);

			// プレースホルダの検索対象シートを取得
			List<XSSFSheet> sheetList = getDataSheetList(wb);
			if (sheetList.size() <= 0) {
				logger.warn("no sheets found: {}", resource);
			}

			// 各シートからプレースホルダを取得
			List<ExcelPlaceholder> placeholderList = new ArrayList<>();
			for (XSSFSheet sheet : sheetList) {
				List<ExcelPlaceholder> list = getPlaceholderList(sheet);
				placeholderList.addAll(list);
			}
			if (placeholderList.size() <= 0) {
				logger.warn("no placeholder found");
			}

			// 取得したプレースホルダからクエリを生成
			List<DataQuery> queryList = new ArrayList<>();
			for (ExcelPlaceholder placeholder : placeholderList) {
				DataQuery query = createDataQuery(placeholder);
				placeholder.setDataQueryId(query.getId()); // プレースホルダとクエリの紐づけ
				queryList.add(query);
			}

			// TODO 複数のプレースホルダが同一行にある場合はエラー

			// TODO 複数のプレースホルダが同一シートにある場合はエラー（シフトが面倒なので）

			ExcelReportTemplate template = new ExcelReportTemplate();
			template.setRendererId(this.rendererId);
			template.setResource(resource);
			template.setPlaceholderList(placeholderList);
			template.setDataQueryList(queryList);

			return template;

		} catch (Exception e) {

			logger.warn("template creation failed", e);
			return null;

		} finally {
			if (wb != null) {
				try {
					wb.close();
				} catch (Exception e) {
					logger.debug("ignore exception: {}", e.getMessage());
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
					logger.debug("ignore exception: {}", e.getMessage());
				}
			}
		}
	}

	@Override
	public InputStream render(RenderingTemplate template, DataResult dataResult) throws Exception {

		ExcelReportTemplate excelTemplate = (ExcelReportTemplate) template;
		InputStream is = excelTemplate.getResource().getInputStream();

		try (XSSFWorkbook wb = new XSSFWorkbook(is)) {

			preRendering(wb, excelTemplate);

			// 各クエリに対応する値を埋め込む
			for (ExcelPlaceholder placeholder : excelTemplate.getPlaceholderList()) {

				// クエリIDに対応する結果を取得
				String queryId = placeholder.getDataQueryId();
				ResultBase result = dataResult.getResult(queryId);
				if (result == null) {
					// この状態は通常ありえません
					throw new IllegalStateException("no result found for queryId: " + queryId);
				}

				// 検索結果を埋め込む
				putResult(wb, placeholder, result);
			}

			// レポートの仕上げ処理
			postRendering(wb, excelTemplate);

			// Excelデータを一時的に書き出して、入力ストリームとして返却
			ByteArrayOutputStream baos = new ByteArrayOutputStream(DEFAULT_FILESIZE);
			try {
				wb.write(baos);
			} finally {
				try {
					baos.close();
				} catch (Exception e) {
					logger.debug("ignore closing exception: " + e.getMessage());
				}
			}
			byte[] output = baos.toByteArray();
			return new ByteArrayInputStream(output);
		}
	}

	/**
	 * 解析対象とするシート一覧を取得する。
	 * 
	 * @param wb
	 * @return
	 */
	protected List<XSSFSheet> getDataSheetList(XSSFWorkbook wb) {

		List<XSSFSheet> sheetList = new ArrayList<>();

		Pattern pattern = Pattern.compile(this.dataSheetName);
		Iterator<XSSFSheet> ite = wb.iterator();
		while (ite.hasNext()) {
			XSSFSheet sheet = ite.next();
			String sheetName = sheet.getSheetName();
			Matcher matcher = pattern.matcher(sheetName);
			if (matcher.find()) {
				sheetList.add(sheet);
			}
		}
		return sheetList;
	}

	/**
	 * シートからプレースホルダを取得します。
	 * 
	 * @param sheet
	 * @return
	 */
	protected List<ExcelPlaceholder> getPlaceholderList(XSSFSheet sheet) {

		String sheetName = sheet.getSheetName();
		List<ExcelPlaceholder> placeholderList = new ArrayList<>();

		Iterator<Row> rowIte = sheet.rowIterator();
		while (rowIte.hasNext()) {

			XSSFRow row = (XSSFRow) rowIte.next();
			Iterator<Cell> cellIte = row.cellIterator();
			while (cellIte.hasNext()) {
				XSSFCell cell = (XSSFCell) cellIte.next();
				String cellValue = cell.toString().trim();
				if (cellValue.startsWith(PLACEHOLDER_BEGIN)) {
					String tagName = cellValue.substring(2);
					int colIndex = cell.getColumnIndex();
					int rowIndex = cell.getRowIndex();
					ExcelPlaceholder placeholder = new ExcelPlaceholder(sheetName, tagName, colIndex, rowIndex);
					placeholderList.add(placeholder);
				}
			}
		}
		return placeholderList;
	}

	/**
	 * プレースホルダ情報からクエリを生成します。
	 * 
	 * @param placeholder
	 * @return
	 */
	protected DataQuery createDataQuery(ExcelPlaceholder placeholder) {
		DataQuery dataQuery = new DataQuery();
		dataQuery.setDataDefinitionId(placeholder.getTagName());
		return dataQuery;
	}

	/**
	 * レンダリング前処理を行います。
	 * 
	 * @param wb
	 */
	protected void preRendering(XSSFWorkbook wb, ExcelReportTemplate excelTemplate) {

		// 安全のためにシート選択を解除
		// (選択シートは復元しない。設定によりアクティブシートは復元する。)
		for (int i = 0; i < wb.getNumberOfSheets(); i++) {
			Sheet s = wb.getSheetAt(i);
			s.setSelected(false);
		}

		// TODO ロック解除等の編集のための初期化

	}

	/**
	 * レンダリング後処理を行います。
	 * 
	 * @param wb
	 */
	protected void postRendering(XSSFWorkbook wb, ExcelReportTemplate excelTemplate) {

		// アクティブシートを復元or設定
		// ・データ編集でアクティブシートが変更されてしまうので必要な処理
		// ・アクティブシートが変わると複数シートが選択される場合があるので全シートの選択を解除
		// ・アクティブシートやシート数は安全のためにテンプレートに保持しない(最新の値を使用)
		int activeSheetIndex = wb.getActiveSheetIndex();
		int lastSheetIndex = wb.getNumberOfSheets() - 1;
		if (this.outputActiveSheetIndex < 0) {
			wb.setActiveSheet(activeSheetIndex); // 復元
		} else if (this.outputActiveSheetIndex <= lastSheetIndex) {
			wb.setActiveSheet(this.outputActiveSheetIndex);
		} else {
			// 最大インデックスを超過している場合は先頭シートを選択
			logger.warn("active sheet index exceeded: active={}, max={}", this.outputActiveSheetIndex, lastSheetIndex);
			wb.setActiveSheet(0);
		}
	}

	@Override
	public void close() {
		// Nothing to do
	}

	/**
	 * 検索結果をワークブックに挿入する。
	 * 
	 * @param wb
	 * @param pos
	 * @param result
	 */
	protected void putResult(XSSFWorkbook wb, ExcelPlaceholder pos, ResultBase result) {

		String sheetName = pos.getSheetName();
		int placeholderColIndex = pos.getColumnIndex();
		int placeholderRowIndex = pos.getRowIndex();

		XSSFSheet sheet = wb.getSheet(sheetName);

		logger.trace("start rendering: {}", pos);

		if (result instanceof ResultValue) {

			ResultValue r = (ResultValue) result;
			Object value = r.getValue();
			XSSFCell cell = sheet.getRow(placeholderRowIndex).getCell(placeholderColIndex);
			boolean isOk = putResultValue(cell, value);
			if (!isOk) {
				logger.warn("invalid value: {}(col={}, row={})", sheetName, placeholderColIndex, placeholderRowIndex);
			}

		} else if (result instanceof ResultList) {

			throw new CodingException();

		} else if (result instanceof ResultTable) {

			ResultTable r = (ResultTable) result;
			int resultColCount = r.getColumnCount();
			int resultRowCount = r.getRowCount();

			// 編集前の行の書式を退避して削除
			// (検索結果を挿入したからプレースホルダ行を削除すると遅くなる可能性あり。)
			// TODO 数式はコピー？
			Row placeholderRow = sheet.getRow(placeholderRowIndex);
			List<CellStyle> styleList = createStyleList(wb, placeholderRow); // １行分全てスタイル
			sheet.removeRow(placeholderRow);

			// 検索結果を挿入するためにプレースホルダ以降にあった既存行を下にをずらす
			// ※この処理で当該シートのgetPhysicalNumberOfRows()の行数が変わるので注意！
			// TODO 最大行超過チェック
			int shiftBeginRowIndex = placeholderRowIndex;
			int shiftEndRowIndex = shiftBeginRowIndex + sheet.getPhysicalNumberOfRows() - 1;
			sheet.shiftRows(shiftBeginRowIndex, shiftEndRowIndex, resultRowCount - 1);

			// 検索結果なしの場合、以降のレンダリング処理は不要
			if (resultRowCount <= 0) {
				return;
			}

			// 検索結果を描画
			int beginColIndex = placeholderColIndex;
			int beginRowIndex = placeholderRowIndex; // 上記で行削除しているので+1しない
			for (int i = 0; i < resultRowCount; i++) {
				int rowIndex = beginRowIndex + i;
				ResultTableRow resultRow = r.getRow(i);
				Row row = createDefaultRow(sheet, rowIndex, beginColIndex, resultColCount, styleList);
				for (int j = 0; j < resultColCount; j++) {
					int colIndex = beginColIndex + j;
					logger.trace("render cell: result({}, {}) -> excel({}, {})", j, i, colIndex, rowIndex);
					Cell cell = row.getCell(colIndex);
					Object value = resultRow.getValue(j);
					putResultValue(cell, value);
				}
			}

		} else {
			throw new IllegalArgumentException("unknown result type: " + result);
		}
	}
	
	/**
	 * データの型に合わせてセルの値を設定します。
	 * 
	 * @param cell
	 * @param value
	 * @return
	 */
	protected boolean putResultValue(Cell cell, Object value) {

		// このメソッドは検索結果に応じて数百回、数千回以上実行される可能性があるので
		// 性能劣化しないように注意すること！！

		boolean isSucceeded = true;

		if (value == null) {
			cell.setCellValue((String) null);
		}

		// 値の型に合わせて異なる値設定メソッドを実行
		if (value instanceof String) {
			cell.setCellValue((String) value);
		} else if (value instanceof Integer) {
			cell.setCellValue(((Integer) value).doubleValue());
		} else if (value instanceof Short) {
			cell.setCellValue(((Short) value).doubleValue());
		} else if (value instanceof Long) {
			cell.setCellValue(((Long) value).doubleValue());
		} else if (value instanceof Float) {
			cell.setCellValue(((Float) value).doubleValue());
		} else if (value instanceof Double) {
			cell.setCellValue(((Double) value).doubleValue());
		} else if (value instanceof BigDecimal){
			cell.setCellValue(((BigDecimal)value).doubleValue());
		} else if (value instanceof Boolean) {
			cell.setCellValue(((Boolean) value).booleanValue());
		} else if (value instanceof java.sql.Date) {
			cell.setCellValue((java.util.Date) value); // java.sql.Dateはjava.util.Dateのラッパー
		} else if (value instanceof java.sql.Time) {
			cell.setCellValue((java.util.Date) value); // java.sql.Timeはjava.util.Dateのラッパー
		} else if (value instanceof java.sql.Timestamp) {
			cell.setCellValue((java.util.Date) value); // java.sql.Timestampはjava.util.Dateのラッパー;
		} else {
			String strValue = value.toString();
			String type = value.getClass().getCanonicalName();
			cell.setCellValue(strValue);
			// TODO CELL_TYPE_ERRORだとファイルを開く時にエラーになる
			// cell.setCellType(XSSFCell.CELL_TYPE_ERROR);
			logger.warn("value mapping failed: value={}, type={}", strValue, type);
			isSucceeded = false;
		}

		return isSucceeded;
	}


	/**
	 * 指定した行のセル書式のリストを生成します。<br>
	 * リストに含まれる書式は指定行の書式のクローンなので、指定行に対する削除等の操作の影響を受けません。
	 * 
	 * @param wb
	 *            ワークブック(Cell生成で使用する)
	 * @param row
	 *            行
	 * @return 書式リスト
	 */
	protected List<CellStyle> createStyleList(XSSFWorkbook wb, Row row) {

		List<CellStyle> styleList = new ArrayList<>();

		if (row == null) {
			return styleList;
		}

		// 高速化のために可能な限り書式を再利用
		Map<String, CellStyle> styleMap = new HashMap<>();

		int colCount = row.getPhysicalNumberOfCells();
		for (int i = 0; i < colCount; i++) {
			Cell cell = row.getCell(i);
			if (cell == null) {
				continue;
			}
			CellStyle style = cell.getCellStyle();
			String styleId = style.toString();
			CellStyle clonedStyle = wb.createCellStyle();
			clonedStyle.cloneStyleFrom(style);
			if (!styleMap.containsKey(styleId)) {
				styleMap.put(styleId, clonedStyle);
			}
			styleList.add(clonedStyle);
		}
		logger.trace("cloned styles: columnCount={}, uniqueStyleCount={}", colCount, styleMap.size());
		return styleList;
	}

	/**
	 * 新規行を作成します。<br>
	 * 指定された範囲の列には、styleListの書式が設定されます。
	 * 
	 * @param sheet
	 * @param rowIndex
	 * @param beginCellIndex
	 * @param columnCount
	 * @param styleList
	 * @return
	 */
	protected Row createDefaultRow(XSSFSheet sheet, int rowIndex, int beginCellIndex, int columnCount, List<CellStyle> styleList) {

		int styleSize = styleList.size();

		Row row = sheet.createRow(rowIndex);
		for (int i = 0; i < columnCount; i++) {
			int colIndex = beginCellIndex + i;
			Cell cell = row.createCell(colIndex);
			if (colIndex <= styleSize - 1) {
				cell.setCellStyle(styleList.get(colIndex));
			} else {
				// 範囲外はデフォルトのスタイル
			}
		}

		return row;
	}

}
