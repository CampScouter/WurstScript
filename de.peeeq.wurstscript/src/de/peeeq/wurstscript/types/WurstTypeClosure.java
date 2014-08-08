package de.peeeq.wurstscript.types;

import java.util.List;

import de.peeeq.wurstscript.ast.AstElement;
import de.peeeq.wurstscript.attributes.AttrClosureAbstractMethod;
import de.peeeq.wurstscript.jassIm.ImExprOpt;
import de.peeeq.wurstscript.jassIm.ImType;
import de.peeeq.wurstscript.jassIm.JassIm;

public final class WurstTypeClosure extends WurstType {

	private final List<WurstType> paramTypes;
	private final WurstType returnType;

	public WurstTypeClosure(List<WurstType> paramTypes, WurstType returnType) {
		this.paramTypes = paramTypes;
		this.returnType = returnType;
	}

	@Override
	public boolean isSubtypeOfIntern(WurstType other, AstElement location) {
		if (other instanceof WurstTypeClosure) {
			WurstTypeClosure o = (WurstTypeClosure) other;
			if (paramTypes.size() != o.paramTypes.size()) {
				return false;
			}
			// contravariant parameter types
			for (int i=0; i<paramTypes.size(); i++) {
				if (!o.paramTypes.get(i).isSubtypeOf(paramTypes.get(i), location)) {
					return false;
				}
			}
			// covariant return types
			if (!returnType.isSubtypeOf(o.returnType, location)) {
				return false;
			}
			return true;
		} else if (other instanceof WurstTypeCode) {
			return paramTypes.size() == 0;
		} else {
			FunctionSignature abstractMethod = AttrClosureAbstractMethod.getAbstractMethodSignature(other, location);
			if (abstractMethod != null) {
				return closureImplementsAbstractMethod(abstractMethod, location);
			}
		}
		return false;
	}


	private boolean closureImplementsAbstractMethod(FunctionSignature abstractMethod,
			AstElement location) {
		if (paramTypes.size() != abstractMethod.getParamTypes().size()) {
			return false;
		}
		
		// contravariant parameter types
		for (int i=0; i<paramTypes.size(); i++) {
			if (!abstractMethod.getParamTypes().get(i).isSubtypeOf(paramTypes.get(i), location)) {
				return false;
			}
		}
		// covariant return types
		if (!returnType.isSubtypeOf(abstractMethod.getReturnType(), location)) {
			// void return type accepts every other returntype
			if (!(abstractMethod.getReturnType() instanceof WurstTypeVoid)) {
				return false;
			}
		}
		return true;
	}

	

	
	
	
	
	
	

	@Override
	public String getName() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		boolean first = true;
		for (WurstType t : paramTypes) {
			if (!first) {
				sb.append(", ");
			}
			sb.append(t.getName());
			first = false;
		}
		sb.append(") -> ");
		sb.append(returnType.getName());
		return sb.toString();
	}

	@Override
	public String getFullName() {
		return getName();
	}

	@Override
	public ImType imTranslateType(AstElement location) {
		return WurstTypeInt.instance().imTranslateType(location);
	}

	@Override
	public ImExprOpt getDefaultValue(AstElement location) {
		return JassIm.ImIntVal(0);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((paramTypes == null) ? 0 : paramTypes.hashCode());
		result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WurstTypeClosure other = (WurstTypeClosure) obj;
		if (paramTypes == null) {
			if (other.paramTypes != null)
				return false;
		} else if (!paramTypes.equals(other.paramTypes))
			return false;
		if (returnType == null) {
			if (other.returnType != null)
				return false;
		} else if (!returnType.equals(other.returnType))
			return false;
		return true;
	}

	

}
