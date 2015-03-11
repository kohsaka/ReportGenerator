/**
 * 
 */
package hit.repgen.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author kohsaka.N0003
 *
 */
public class StringUtils extends org.apache.commons.lang.StringUtils{
	
	public static String CR = "\r"; // 000D
	
	public static String LF = "\f"; // 000C
	
	public static String CR_LF = CR + LF;
	
	public static String NEW_LINE = "\n"; // 000A
	
	public static String WHITE_SPACE = " ";
	
	private static String REGEXP_METACHARS = "\\*+.?{}()[]^$-|";
	
	/** ���K�\���p�^�[��: �C��(�ŒZ��v) */
	private static String REGEXP_ARBITRY = ".*?";
	
	/**
	 * ���[�y�ѓ����̏璷�ȋ󔒂�^�u���g�������܂��B
	 * @param text
	 * @return
	 */
	public static String trimInner(String text){
		
		if( text == null ){
			return null;
		}
		
		StringBuilder sb = new StringBuilder(); // �ԋp

		boolean isWsContinued = false; // �󔒂��A�����Ă��邩
		char[] chars = text.trim().toCharArray();
		for(int i=0; i<chars.length; i++){
			char ch = chars[i];
			if( !Character.isWhitespace(ch) ){
				if( isWsContinued ){
					sb.append(WHITE_SPACE);
				}
				sb.append(ch);
				isWsContinued = false;
			}else{
				isWsContinued = true;
			}
		}
		return sb.toString();
	}
	
	/**
	 * ���s���蕶�����P��s�̕�����ɕϊ����܂��B
	 * @param text �ϊ��Ώۂ̕�����
	 * @return CR�y��LF���󔒂ɒu��������������
	 */
	public static String toSingleLineString(String text){
		String crReplaced = replace(text, CR, WHITE_SPACE);
		String crLfReplaced = replace(crReplaced, LF, WHITE_SPACE);
		String crLfNlReplaced = replace(crLfReplaced, NEW_LINE, WHITE_SPACE);
		String wsTrimed = trimInner(crLfNlReplaced);
		return wsTrimed;
	}
	
	/**
	 * left, right�ň͂܂ꂽ�l���擾���܂��B
	 * left, right�����K�\���̃��^�L�����̏ꍇ�A�����I�ɃG�X�P�[�v���܂��B
	 * @param str
	 * @param left
	 * @param right
	 * @return
	 */
	public static List<String> getCitingPatternList(String str, String left, String right){
		
		List<String> list = new ArrayList<>();
		if( str == null ){
			return list;
		}
		
		// ���K�\���Ńp�^�[�����擾
		String leftStr = left != null ? left: EMPTY;
		String rightStr = right != null ? right: EMPTY;
		String ptn = escapeRegexp(left) + REGEXP_ARBITRY + escapeRegexp(right);
		Pattern p = Pattern.compile(ptn);
		Matcher m = p.matcher(str);
		while(m.find()){
			String org = m.group(); // �擪�̒��o
			String value = org.substring(leftStr.length(), org.length() - rightStr.length());
			list.add(value);
		}
		return list;
	}
	
	/**
	 * ���K�\���̃��^�L�������G�X�P�[�v���܂��B
	 * @param str
	 * @return
	 */
	public static String escapeRegexp(String str){

		if( str == null ){
			return EMPTY;
		}
		
		StringBuilder builder = new StringBuilder();
		for(int i=0; i<str.length(); i++){
			char c = str.charAt(i);
			if( REGEXP_METACHARS.indexOf(c)>=0 ){
				builder.append("\\");
			}
			builder.append(c);
		}
		return builder.toString();
	}
}
