/**
 * 
 */
package hit.repgen.datamodel;

/**
 * @author kohsaka.N0003
 *
 */
public class ResultValue extends ResultBase{
	
	private Object value;

	/** �R���X�g���N�^ */
	public ResultValue(Object value){
		this.value = value;
	}
	
	/**
	 * @return value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @param value �Z�b�g���� value
	 */
	public void setValue(Object value) {
		this.value = value;
	}
	
	public String toString(){
		return getClass().getSimpleName() + "(value=\"" + this.value + "\")";
	}

}
