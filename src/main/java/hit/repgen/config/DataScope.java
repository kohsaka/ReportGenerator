package hit.repgen.config;

/**
 * �f�[�^�̗L���X�R�[�v�B
 * @author kohsaka.N0003
 *
 */
public enum DataScope {
	NON, // �Ȃ�(�ʏ�g�p���܂���) 
	STATIC, // �ÓI�f�[�^
	DYNAMIC; // ���I�f�[�^
	
	/**
	 * �����񂩂�l���擾���܂��B<br>
	 * @param name ����
	 * @param defaultName ��͕s���ɕԋp����l
	 * @return �X�R�[�v
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
