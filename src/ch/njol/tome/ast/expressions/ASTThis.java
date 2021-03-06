package ch.njol.tome.ast.expressions;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.tome.ast.ASTInterfaces.ASTExpression;
import ch.njol.tome.ast.AbstractASTElement;
import ch.njol.tome.compiler.Token;
import ch.njol.tome.ir.expressions.IRExpression;
import ch.njol.tome.ir.expressions.IRThis;
import ch.njol.tome.ir.uses.IRSelfTypeUse;
import ch.njol.tome.ir.uses.IRTypeUse;
import ch.njol.tome.parser.Parser;

/**
 * The keyword 'this', representing 'the current object'.
 */
public class ASTThis extends AbstractASTElement implements ASTExpression {
	@Override
	public String toString() {
		return "this";
	}
	
	@Override
	public @Nullable String hoverInfo(final Token token) {
		return null; // TODO current type, and maybe more
	}
	
	public static ASTThis parse(final Parser parent) {
		return parent.one(p -> {
			final ASTThis ast = new ASTThis();
			p.one("this");
			return ast;
		});
	}
	
	@Override
	public IRTypeUse getIRType() {
		return IRSelfTypeUse.makeNew(this);
	}
	
	@Override
	public IRExpression getIR() {
		return IRThis.makeNew(this);
	}
}
