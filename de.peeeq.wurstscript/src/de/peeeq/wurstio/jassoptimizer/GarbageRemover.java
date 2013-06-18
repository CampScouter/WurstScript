package de.peeeq.wurstio.jassoptimizer;

import java.util.Collection;
import java.util.HashMap;

import com.google.common.collect.Sets;

import de.peeeq.wurstscript.jassAst.JassExprFuncRef;
import de.peeeq.wurstscript.jassAst.JassExprFunctionCall;
import de.peeeq.wurstscript.jassAst.JassFunction;
import de.peeeq.wurstscript.jassAst.JassFunctions;
import de.peeeq.wurstscript.jassAst.JassProg;
import de.peeeq.wurstscript.jassAst.JassStmtCall;
import de.peeeq.wurstscript.translation.imoptimizer.RestrictedStandardNames;

public class GarbageRemover {

	
	
	public static void removeGarbage(JassProg prog) {
		// Find uses
		HashMap<String, Boolean> useMap = new HashMap<String, Boolean>();
		findFunctionUses(prog, useMap);
		
		// Remove Unneeded ones
		JassFunctions originalFuncs = prog.getFunctions();
		Collection<JassFunction> toRemove = Sets.newHashSet();
		for ( JassFunction func : originalFuncs ) {
			if ( useMap.get(func.getName())  == null && ! RestrictedStandardNames.contains(func.getName()) ) {
				toRemove.add(func);
			}
		}
		originalFuncs.removeAll(toRemove);
	}

	
	
	private static void findFunctionUses(JassProg prog,
			final HashMap<String, Boolean> useMap) {
		prog.accept(new JassProg.DefaultVisitor() {			
			@Override
			public void visit(JassFunction func) {
				
				func.accept(new JassFunction.DefaultVisitor() {
					
					
					
					@Override
					public void visit(JassExprFuncRef ref ) {
						String name = ref.getFuncName();
						useMap.put(name, true);
					}
					
					@Override
					public void visit(JassExprFunctionCall call ) {
						String name = call.getFuncName();
						useMap.put(name, true);
					}
					
					@Override
					public void visit(JassStmtCall call ) {
						String name = call.getFuncName();
						useMap.put(name, true);
					}
										
					
				});

				
			}
			
		});
		
	}
	
}
