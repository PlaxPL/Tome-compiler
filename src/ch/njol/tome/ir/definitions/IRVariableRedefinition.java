package ch.njol.tome.ir.definitions;

import ch.njol.tome.ir.uses.IRTypeUse;

public interface IRVariableRedefinition extends IRVariableOrAttributeRedefinition {
	
	String name();
	
	public IRTypeUse type();
	
	@Override
	default IRTypeUse mainResultType() {
		return type();
	}
	
	IRVariableDefinition definition();
	
}
