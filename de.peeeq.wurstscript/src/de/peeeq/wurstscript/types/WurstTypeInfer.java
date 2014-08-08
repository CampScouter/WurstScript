package de.peeeq.wurstscript.types;

import org.eclipse.jdt.annotation.Nullable;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;

/**
 * the exact type is not known but it will be whatever you want it to be ;)
 * (used for the buildin/native functions, where we cannot check the types) 
 */
public class WurstTypeInfer extends WurstType {

	private static WurstType instance = new WurstTypeInfer();

	private WurstTypeInfer() {}
	
	@Override
	public boolean isSubtypeOfIntern(WurstType other, AstElement location) {
		return true;
	}

	@Override
	public String getName() {
		return "<Infer type>";
	}

	@Override
	public String getFullName() {
		return getName();
	}

	public static WurstType instance() {
		return instance ;
	}

	@Override
	public ImType imTranslateType(AstElement location) {
		throw new Error("not implemented");
	}

	@Override
	public ImExprOpt getDefaultValue(AstElement location) {
		throw new Error("not implemented");
	}
	
	@Override
	public final boolean equals(@Nullable Object other) {
		return this == other;
	}
	
	@Override
	public final int hashCode() {
		return System.identityHashCode(this);
	}


}
