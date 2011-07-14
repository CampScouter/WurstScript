package de.peeeq.pscript.intermediateLang;

public class ILconstBool implements ILconst {

	private boolean val;

	public ILconstBool(String boolVal) {
		if (boolVal.equals("true")) {
			this.val = true;
		} else if (boolVal.equals("false")) {
			this.val = false;
		} else {
			throw new Error("unsupported boolean constant");
		}
		
	}
	
	public boolean getVal() {
		return val;
	}

	@Override
	public String print() {
		return val ? "true" : "false";
	}

}
