package de.peeeq.wurstscript.types;

import org.eclipse.jdt.annotation.Nullable;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.JassIm;


public final class WurstTypeCode extends WurstTypePrimitive {

	private static final WurstTypeCode instance = new WurstTypeCode();

	// make constructor private as we only need one instance
	private WurstTypeCode() {
		super("code");
	}
	
	@Override
	public boolean isSubtypeOfIntern(WurstType other, AstElement location) {
		return other instanceof WurstTypeCode;
	}

	public static WurstTypeCode instance() {
		return instance;
	}

	@Override
	public ImExprOpt getDefaultValue(AstElement location) {
		return JassIm.ImNull();
	}

	
}
