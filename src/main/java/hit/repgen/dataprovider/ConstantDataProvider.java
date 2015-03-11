package hit.repgen.dataprovider;

import java.util.Map;

import hit.repgen.config.ComponentConfig;
import hit.repgen.config.DataDefinition;
import hit.repgen.datamodel.ResultBase;
import hit.repgen.datamodel.ResultValue;

public class ConstantDataProvider implements DataProvider{

	@Override
	public void init(ComponentConfig config) {
		// nothing to do
	}

	@Override
	public boolean validate() {
		// nothing to do
		return true;
	}

	@Override
	public ResultBase getResult(DataDefinition def, Map<String, Object> params) {
		
		// TODO パラメータをマップする？
		
		String define = def.getDefine();
		return new ResultValue(define);
	}

	@Override
	public void close() {
		// nothing to do
	}

}
