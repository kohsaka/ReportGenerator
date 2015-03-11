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

	/** �����_���\ID */
	protected String rendererId;

	/** �v���[�X�z���_�T���ΏۂƂ���V�[�g�� */
	protected String dataSheetName;

	/** �����t�@�C���̃A�N�e�B�u�V�[�g�̃C���f�b�N�X */
	protected int outputActiveSheetIndex;

	/** ���K�[ */
	protected Logger logger = LoggerFactory.getLogger(getClass());

	/** �R���X�g���N�^ */
	public ExcelReportRenderer() {

		// �v���p�e�B�l���Ȃ��ꍇ�̒l(����l)
		this.dataSheetName = "�f�[�^";
		this.outputActiveSheetIndex = 0; // �擪
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

			is = resource.getInputStream(); // TODO �L���b�V��
			wb = new XSSFWorkbook(is);

			// �v���[�X�z���_�̌����ΏۃV�[�g���擾
			List<XSSFSheet> sheetList = getDataSheetList(wb);
			if (sheetList.size() <= 0) {
				logger.warn("no sheets found: {}", resource);
			}

			// �e�V�[�g����v���[�X�z���_���擾
			List<ExcelPlaceholder> placeholderList = new ArrayList<>();
			for (XSSFSheet sheet : sheetList) {
				List<ExcelPlaceholder> list = getPlaceholderList(sheet);
				placeholderList.addAll(list);
			}
			if (placeholderList.size() <= 0) {
				logger.warn("no placeholder found");
			}

			// �擾�����v���[�X�z���_����N�G���𐶐�
			List<DataQuery> queryList = new ArrayList<>();
			for (ExcelPlaceholder placeholder : placeholderList) {
				DataQuery query = createDataQuery(placeholder);
				placeholder.setDataQueryId(query.getId()); // �v���[�X�z���_�ƃN�G���̕R�Â�
				queryList.add(query);
			}

			// TODO �����̃v���[�X�z���_������s�ɂ���ꍇ�̓G���[

			// TODO �����̃v���[�X�z���_������V�[�g�ɂ���ꍇ�̓G���[�i�V�t�g���ʓ|�Ȃ̂Łj

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

			// �e�N�G���ɑΉ�����l�𖄂ߍ���
			for (ExcelPlaceholder placeholder : excelTemplate.getPlaceholderList()) {

				// �N�G��ID�ɑΉ����錋�ʂ��擾
				String queryId = placeholder.getDataQueryId();
				ResultBase result = dataResult.getResult(queryId);
				if (result == null) {
					// ���̏�Ԃ͒ʏ킠�肦�܂���
					throw new IllegalStateException("no result found for queryId: " + queryId);
				}

				// �������ʂ𖄂ߍ���
				putResult(wb, placeholder, result);
			}

			// ���|�[�g�̎d�グ����
			postRendering(wb, excelTemplate);

			// Excel�f�[�^���ꎞ�I�ɏ����o���āA���̓X�g���[���Ƃ��ĕԋp
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
	 * ��͑ΏۂƂ���V�[�g�ꗗ���擾����B
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
	 * �V�[�g����v���[�X�z���_���擾���܂��B
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
	 * �v���[�X�z���_��񂩂�N�G���𐶐����܂��B
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
	 * �����_�����O�O�������s���܂��B
	 * 
	 * @param wb
	 */
	protected void preRendering(XSSFWorkbook wb, ExcelReportTemplate excelTemplate) {

		// ���S�̂��߂ɃV�[�g�I��������
		// (�I���V�[�g�͕������Ȃ��B�ݒ�ɂ��A�N�e�B�u�V�[�g�͕�������B)
		for (int i = 0; i < wb.getNumberOfSheets(); i++) {
			Sheet s = wb.getSheetAt(i);
			s.setSelected(false);
		}

		// TODO ���b�N�������̕ҏW�̂��߂̏�����

	}

	/**
	 * �����_�����O�㏈�����s���܂��B
	 * 
	 * @param wb
	 */
	protected void postRendering(XSSFWorkbook wb, ExcelReportTemplate excelTemplate) {

		// �A�N�e�B�u�V�[�g�𕜌�or�ݒ�
		// �E�f�[�^�ҏW�ŃA�N�e�B�u�V�[�g���ύX����Ă��܂��̂ŕK�v�ȏ���
		// �E�A�N�e�B�u�V�[�g���ς��ƕ����V�[�g���I�������ꍇ������̂őS�V�[�g�̑I��������
		// �E�A�N�e�B�u�V�[�g��V�[�g���͈��S�̂��߂Ƀe���v���[�g�ɕێ����Ȃ�(�ŐV�̒l���g�p)
		int activeSheetIndex = wb.getActiveSheetIndex();
		int lastSheetIndex = wb.getNumberOfSheets() - 1;
		if (this.outputActiveSheetIndex < 0) {
			wb.setActiveSheet(activeSheetIndex); // ����
		} else if (this.outputActiveSheetIndex <= lastSheetIndex) {
			wb.setActiveSheet(this.outputActiveSheetIndex);
		} else {
			// �ő�C���f�b�N�X�𒴉߂��Ă���ꍇ�͐擪�V�[�g��I��
			logger.warn("active sheet index exceeded: active={}, max={}", this.outputActiveSheetIndex, lastSheetIndex);
			wb.setActiveSheet(0);
		}
	}

	@Override
	public void close() {
		// Nothing to do
	}

	/**
	 * �������ʂ����[�N�u�b�N�ɑ}������B
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

			// �ҏW�O�̍s�̏�����ޔ����č폜
			// (�������ʂ�}����������v���[�X�z���_�s���폜����ƒx���Ȃ�\������B)
			// TODO �����̓R�s�[�H
			Row placeholderRow = sheet.getRow(placeholderRowIndex);
			List<CellStyle> styleList = createStyleList(wb, placeholderRow); // �P�s���S�ăX�^�C��
			sheet.removeRow(placeholderRow);

			// �������ʂ�}�����邽�߂Ƀv���[�X�z���_�ȍ~�ɂ����������s�����ɂ����炷
			// �����̏����œ��Y�V�[�g��getPhysicalNumberOfRows()�̍s�����ς��̂Œ��ӁI
			// TODO �ő�s���߃`�F�b�N
			int shiftBeginRowIndex = placeholderRowIndex;
			int shiftEndRowIndex = shiftBeginRowIndex + sheet.getPhysicalNumberOfRows() - 1;
			sheet.shiftRows(shiftBeginRowIndex, shiftEndRowIndex, resultRowCount - 1);

			// �������ʂȂ��̏ꍇ�A�ȍ~�̃����_�����O�����͕s�v
			if (resultRowCount <= 0) {
				return;
			}

			// �������ʂ�`��
			int beginColIndex = placeholderColIndex;
			int beginRowIndex = placeholderRowIndex; // ��L�ōs�폜���Ă���̂�+1���Ȃ�
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
	 * �f�[�^�̌^�ɍ��킹�ăZ���̒l��ݒ肵�܂��B
	 * 
	 * @param cell
	 * @param value
	 * @return
	 */
	protected boolean putResultValue(Cell cell, Object value) {

		// ���̃��\�b�h�͌������ʂɉ����Đ��S��A�����ȏ���s�����\��������̂�
		// ���\�򉻂��Ȃ��悤�ɒ��ӂ��邱�ƁI�I

		boolean isSucceeded = true;

		if (value == null) {
			cell.setCellValue((String) null);
		}

		// �l�̌^�ɍ��킹�ĈقȂ�l�ݒ胁�\�b�h�����s
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
			cell.setCellValue((java.util.Date) value); // java.sql.Date��java.util.Date�̃��b�p�[
		} else if (value instanceof java.sql.Time) {
			cell.setCellValue((java.util.Date) value); // java.sql.Time��java.util.Date�̃��b�p�[
		} else if (value instanceof java.sql.Timestamp) {
			cell.setCellValue((java.util.Date) value); // java.sql.Timestamp��java.util.Date�̃��b�p�[;
		} else {
			String strValue = value.toString();
			String type = value.getClass().getCanonicalName();
			cell.setCellValue(strValue);
			// TODO CELL_TYPE_ERROR���ƃt�@�C�����J�����ɃG���[�ɂȂ�
			// cell.setCellType(XSSFCell.CELL_TYPE_ERROR);
			logger.warn("value mapping failed: value={}, type={}", strValue, type);
			isSucceeded = false;
		}

		return isSucceeded;
	}


	/**
	 * �w�肵���s�̃Z�������̃��X�g�𐶐����܂��B<br>
	 * ���X�g�Ɋ܂܂�鏑���͎w��s�̏����̃N���[���Ȃ̂ŁA�w��s�ɑ΂���폜���̑���̉e�����󂯂܂���B
	 * 
	 * @param wb
	 *            ���[�N�u�b�N(Cell�����Ŏg�p����)
	 * @param row
	 *            �s
	 * @return �������X�g
	 */
	protected List<CellStyle> createStyleList(XSSFWorkbook wb, Row row) {

		List<CellStyle> styleList = new ArrayList<>();

		if (row == null) {
			return styleList;
		}

		// �������̂��߂ɉ\�Ȍ��菑�����ė��p
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
	 * �V�K�s���쐬���܂��B<br>
	 * �w�肳�ꂽ�͈̗͂�ɂ́AstyleList�̏������ݒ肳��܂��B
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
				// �͈͊O�̓f�t�H���g�̃X�^�C��
			}
		}

		return row;
	}

}
