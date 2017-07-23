package ch.njol.brokkr.ir.definitions;

import org.eclipse.jdt.annotation.NonNull;

public interface IRVariableDefinition extends IRVariableRedefinition {
	
	@Override
	default @NonNull IRVariableDefinition definition() {
		return this;
	}
	
}
