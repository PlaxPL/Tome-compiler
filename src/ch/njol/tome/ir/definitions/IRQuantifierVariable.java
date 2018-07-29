package ch.njol.tome.ir.definitions;

import ch.njol.tome.ast.ASTExpressions.ASTQuantifierVar;
import ch.njol.tome.common.Modifiable;
import ch.njol.tome.common.ModificationListener;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.uses.IRTypeUse;

public class IRQuantifierVariable extends AbstractIRBrokkrVariable implements IRVariableDefinition {

	public IRQuantifierVariable(ASTQuantifierVar ast) {
		super(ast);
	}
	
}
