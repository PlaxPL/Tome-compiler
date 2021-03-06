package ch.njol.tome.ir.expressions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.interpreter.InterpretedObject;
import ch.njol.tome.interpreter.InterpretedTuple;
import ch.njol.tome.interpreter.InterpreterContext;
import ch.njol.tome.interpreter.InterpreterException;
import ch.njol.tome.interpreter.nativetypes.InterpretedNativeBoolean;
import ch.njol.tome.ir.IRContext;
import ch.njol.tome.ir.IRElement;
import ch.njol.tome.ir.nativetypes.IRTuple.IRTypeTuple;
import ch.njol.tome.ir.uses.IROrTypeUse;
import ch.njol.tome.ir.uses.IRTypeUse;

public class IRIf extends AbstractIRExpression {
	
	private final IRExpression condition;
	private final IRExpression then;
	private final @Nullable IRExpression otherwise;
	
	public IRIf(final IRExpression condition, final IRExpression then, @Nullable final IRExpression otherwise) {
		if (otherwise == null)
			IRElement.assertSameIRContext(condition, then);
		else
			IRElement.assertSameIRContext(condition, then, otherwise);
		this.condition = registerDependency(condition);
		this.then = registerDependency(then);
		this.otherwise = registerDependency(otherwise);
	}
	
	@Override
	public IRTypeUse type() {
		final IRExpression otherwise = this.otherwise;
		if (otherwise == null)
			return IRTypeTuple.emptyTuple(getIRContext());
		return IROrTypeUse.makeNew(then.type(), otherwise.type());
	}
	
	@Override
	public IRContext getIRContext() {
		return condition.getIRContext();
	}
	
	@Override
	public InterpretedObject interpret(final InterpreterContext context) throws InterpreterException {
		if (InterpretedNativeBoolean.getBoolean(context, condition.interpret(context))) {
			return then.interpret(new InterpreterContext(context));
		} else {
			final IRExpression otherwise = this.otherwise;
			if (otherwise == null)
				return InterpretedTuple.emptyTuple(getIRContext());
			return otherwise.interpret(new InterpreterContext(context));
		}
	}
	
}
