package hit.repgen.config;

/**
 * �f�[�^�̗L���X�R�[�v�B
 * @author kohsaka.N0003
 *
 */
public enum DataType {
	VALUE,
	LIST,
	TABLE;
	
	/**
	 * �����񂩂�l���擾���܂��B<br>
	 * @param name ����
	 * @param defaultName ��͕s���ɕԋp����l
	 * @return �X�R�[�v
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
