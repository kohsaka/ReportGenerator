package hit.repgen.renderer.file;

import hit.repgen.config.Resource;
import hit.repgen.datamodel.DataQuery;
import hit.repgen.datamodel.DataResult;
import hit.repgen.exception.CodingException;
import hit.repgen.renderer.Renderer;
import hit.repgen.renderer.RenderingTemplate;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileReportRenderer implements Renderer{

	protected static final String[] ACCEPT_EXTENSIONS = {"txt", "csv", "tsv"};

	protected static final int MAX_BUFSIZE = 4096;
	
	protected static final String ESCAPE = "@@";
	
	protected static final String QUERY_BEGIN = "@{";
	
	protected static final String QUERY_END = "}@";
	
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public void init(String myRendererId, Properties prop) {
		
	}

	@Override
	public boolean validate() {
		return true;
	}

	@Override
	public RenderingTemplate generateTemplate(Resource resource) throws Exception{
//		try{
//			BufferedInputStream bis = new BufferedInputStream(is);
//			InputStreamReader isr = new InputStreamReader(bis);
//
//			// テンプレート内容をロード(テキストが前提)
//			StringWriter sw = new StringWriter();
//			PrintWriter pw = new PrintWriter(sw);
//			char[] buf = new char[MAX_BUFSIZE];
//			int redCount;
//			while((redCount = isr.read(buf))>=0){
//				pw.write(buf, 0, redCount);
//			}
//			String contents = sw.toString();
//			
//			// クエリを抽出
//			List<DataQuery> queryList = parseQuery(contents);
//			
//			// テンプレート情報を返却
//			FileReportTemplate template = new FileReportTemplate();
//			template.setTemplateContents(contents);
//			template.addDataQuery(queryList);
//			return template;
//			
//		}catch(Exception e){
//			logger.warn("failed to create template", e);
//			return null;
//		}
		return null; // TODO
	}
	
	protected List<DataQuery> parseQuery(String contents) throws IOException{
		throw new CodingException("");
	}

	@Override
	public InputStream render(RenderingTemplate template, DataResult dataResult) {
		throw new CodingException("");
	}

	@Override
	public void close() {
		// Nothing to do
	}

}
