package de.peeeq.wurstscript.types;

import org.eclipse.jdt.annotation.Nullable;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;


public class WurstTypeVoid extends WurstType {

	private static final WurstTypeVoid instance = new WurstTypeVoid();

	// make constructor private as we only need one instance
	private WurstTypeVoid() {}
	
	@Override
	public boolean isSubtypeOfIntern(WurstType other, AstElement location) {
		return other instanceof WurstTypeVoid;
	}

	@Override
	public String getName() {
		return "Void";
	}

	@Override
	public String getFullName() {
		return "Void";
	}

	public static WurstTypeVoid instance() {
		return instance;
	}

	@Override
	public ImType imTranslateType(@Nullable AstElement location) {
		return JassIm.ImVoid();
	}

	@Override
	public ImExprOpt getDefaultValue(@Nullable AstElement location) {
		return JassIm.ImNoExpr();
	}
	
	@Override
	public boolean isVoid() {
		return true;
	}

	@Override
	public boolean equals(@Nullable Object other) {
		return this == other;
	}
	
	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}
	
}
