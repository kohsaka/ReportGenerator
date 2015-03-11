package hit.repgen.exception;

public class CodingException extends RuntimeException{

	/** シリアルバージョン */
	private static final long serialVersionUID = 1L;
	
	/** コンストラクタ */
	public CodingException(){
		super();
	}
	
	/** コンストラクタ */
	public CodingException(String message){
		super(message);
	}
	
}
