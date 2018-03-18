package ch.njol.brokkr.ir.definitions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTInterfaces.ASTExpression;
import ch.njol.brokkr.ast.ASTMembers.ASTSimpleParameter;
import ch.njol.brokkr.interpreter.InterpretedObject;
import ch.njol.brokkr.interpreter.InterpreterContext;
import ch.njol.brokkr.interpreter.InterpreterException;

public class IRBrokkrNormalParameterDefinition extends AbstractIRBrokkrParameter implements IRParameterDefinition {
	
	public IRBrokkrNormalParameterDefinition(final ASTSimpleParameter param, final IRAttributeRedefinition attribute) {
		super(param, attribute);
	}
	
	@Override
	public IRBrokkrNormalParameterDefinition definition() {
		return this;
	}
	
	@Override
	public @Nullable InterpretedObject defaultValue(final InterpreterContext context) throws InterpreterException {
		final ASTExpression defaultValueExpr = ((ASTSimpleParameter) ast).defaultValue;
		return defaultValueExpr == null ? null : defaultValueExpr.getIR().interpret(context);
	}
	
}
