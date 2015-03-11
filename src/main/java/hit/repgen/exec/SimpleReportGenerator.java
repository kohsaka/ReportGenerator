/**
 * 
 */
package hit.repgen.exec;

import hit.repgen.config.Resource;
import hit.repgen.core.ReportGenerator;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kohsaka.N0003
 * 
 */
public class SimpleReportGenerator {

	private enum Prop{
		RendererId, InputFilename, OutputFilename, Params;
	}
	
	private enum ExitCode{
		Succeeded(0), Failed(1);
		int value;
		ExitCode(int value){
			this.value = value;
		}
		public int intValue(){
			return this.value;
		}
	}
	
	/** ロガー */
	private static final Logger logger = LoggerFactory.getLogger(SimpleReportGenerator.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		ExitCode exitCode = ExitCode.Failed;
		
		// 引数の解析
		Map<String, String> argsMap = null;
		try{
			argsMap = parseArgument(args);
		}catch(IllegalArgumentException e){
			System.exit(exitCode.intValue());
		}
		
		System.out.println("generating report...");

		// メイン処理
		InputStream reportInputStream = null;
		FileOutputStream fileOutputStream = null;;
		try {

			
			String rendererId = argsMap.get(Prop.RendererId.toString());
			Resource resource = new Resource(Resource.Type.LocalFile, argsMap.get(Prop.InputFilename.toString()));
			String outputFilename = argsMap.get(Prop.OutputFilename.toString());
			
			ReportGenerator generator = new ReportGenerator();
			generator.init();
			reportInputStream = generator.render(rendererId, resource);
			
			// ファイルに出力
			fileOutputStream = new FileOutputStream(outputFilename);
			try(BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream)){
				int redCount;
				byte[] buf = new byte[4096];
				while( (redCount = reportInputStream.read(buf)) >= 0 ){
					bos.write(buf, 0, redCount);
				}
			}

			exitCode = ExitCode.Succeeded;
			
		} catch (Exception e) {

			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.error(sw.toString());
			System.err.println(sw.toString());

		}finally{
			if( reportInputStream != null ){
				try{
					reportInputStream.close();
				}catch(Exception e){
					// ignore
				}
			}
			if( fileOutputStream != null ){
				try{
					fileOutputStream.close();
				}catch(Exception e){
					// ignore
				}
			}
		}

		System.out.println("report generation end: " + exitCode);

		// このプログラムの終了コード
		System.exit(exitCode.intValue());
	}
	
	protected static Map<String, String> parseArgument(String[] args) throws IllegalArgumentException {
		
		Map<String, String> argsMap = new HashMap<>();
		
		// TODO オプション整理
		Option renderIdOption = new Option("r", true, "render id");
		renderIdOption.setRequired(true);
		Option inputFileOption = new Option("i", true, "input filename");
		inputFileOption.setRequired(true);
		Option outputFileOption = new Option("o", true, "output filename");
		outputFileOption.setRequired(true);
		Options options = new Options();
		options.addOption(renderIdOption);
		options.addOption(inputFileOption);
		options.addOption(outputFileOption);

		try{
			
			CommandLineParser parser = new BasicParser();
			CommandLine cmd = parser.parse(options, args);
			String[] cmdArgs = cmd.getArgs();

			argsMap.put(Prop.RendererId.toString(), cmd.getOptionValue("r"));
			argsMap.put(Prop.InputFilename.toString(), cmd.getOptionValue("i"));
			argsMap.put(Prop.OutputFilename.toString(), cmd.getOptionValue("o"));
			argsMap.put(Prop.Params.toString(), cmd.hasOption("p") ? cmd.getOptionValue("p"): null);
			return argsMap;
			
		}catch(Exception e){
			String myName = SimpleReportGenerator.class.getSimpleName();
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(128, myName + "<render-> <input-file> <output-file> [-p key=value,key=value]", null, options, null);
			throw new IllegalArgumentException();
		}
	}

}
