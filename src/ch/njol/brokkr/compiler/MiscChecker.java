package ch.njol.brokkr.compiler;

import ch.njol.brokkr.ast.ASTInterfaces.ASTVariable;
import ch.njol.brokkr.ast.ASTTopLevelElements.ASTBrokkrFile;
import ch.njol.brokkr.ir.IRContext;

/**
 * Checks Brokkr source files for various non-error mistakes, bad code style, or similar.
 */
public class MiscChecker implements BrokkrFileChecker {
	
	private final IRContext irContext;
	
	public MiscChecker(final IRContext irContext) {
		this.irContext = irContext;
	}
	
	@Override
	public void check(final ASTBrokkrFile file) {
		file.forEach(e -> {
			if (e instanceof ASTVariable) {
				final ASTVariable var = (ASTVariable) e;
				final String name = var.name();
				if (name != null && name.length() >= 4 && name.startsWith("not") && Character.isUpperCase(name.codePointAt(3)) && var.getIRType().equalsType(irContext.getTypeUse("lang", "Boolean"))) {
					// new ...()
				}
			}
		});
	}
	
}
