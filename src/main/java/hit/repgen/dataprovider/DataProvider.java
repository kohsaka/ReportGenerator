package hit.repgen.dataprovider;

import java.util.Map;

import hit.repgen.config.ComponentConfig;
import hit.repgen.config.DataDefinition;
import hit.repgen.datamodel.ResultBase;

public interface DataProvider {

	/**
	 * ���������܂��B
	 */
	public void init(ComponentConfig config);
	
	/**
	 * ���̃v���o�C�_���L�����ǂ��������؂��܂��B<br>
	 * ���ؓ��e�́A���̃C���^�[�t�F�C�X�����������N���X�Ɉς˂��܂��B
	 */
	public boolean validate();
	
	/**
	 * �f�[�^�\�[�X�����`�ɉ������f�[�^���擾���܂��B
	 * @param define ��`
	 * @return ��������
	 */
	public ResultBase getResult(DataDefinition def, Map<String, Object> params);
	
	/**
	 * �I���������s���܂��B
	 */
	public void close();

}
