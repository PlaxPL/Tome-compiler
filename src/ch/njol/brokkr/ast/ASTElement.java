package ch.njol.brokkr.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.brokkr.ast.ASTTopLevelElements.ASTBrokkrFile;
import ch.njol.brokkr.common.BrokkrContentAssistProposal;
import ch.njol.brokkr.common.Invalidatable;
import ch.njol.brokkr.common.StringMatcher;
import ch.njol.brokkr.compiler.ASTModule;
import ch.njol.brokkr.compiler.ParseError;
import ch.njol.brokkr.compiler.Token;

public interface ASTElement extends ASTElementPart, Invalidatable {
	
	/**
	 * The parts (i.e. children) of this element. Each element in this list will have its {@link #parent()} set to this element.
	 */
	public List<ASTElementPart> parts();
	
	public default @Nullable ASTDocument document() {
		final ASTElement parent = parent();
		if (parent != null)
			return parent.document();
		return null;
	}
	
	public void addLink(ASTLink<?> link);
	
	public List<ASTLink<?>> links();
	
	@Override
	public abstract String toString();
	
	/**
	 * @param token The hovered token
	 * @return Information to display on hover, or null for none.
	 */
	default public @Nullable String hoverInfo(final Token token) {
		return null;
	}
	
	/**
	 * @param token The token where the content assist was executed on, or the left one if it was done between two tokens.
	 * @param matcher A mather to filter resulting proposals
	 * @return A {@link Stream} of proposals
	 */
	default public @Nullable Stream<BrokkrContentAssistProposal> getContentAssistProposals(final Token token, final StringMatcher matcher) {
		return null;
	}
	
	@Override
	public default void forEach(final Consumer<ASTElementPart> function) {
		function.accept(this);
		for (final ASTElementPart p : parts())
			p.forEach(function);
	}
	
	public List<ParseError> fatalParseErrors();
	
	@SuppressWarnings("unchecked")
	public default <T extends ASTElement> List<T> getDirectChildrenOfType(final Class<T> type) {
		final List<T> r = new ArrayList<>();
		for (final ASTElementPart part : parts()) {
			if (type.isInstance(part))
				r.add((T) part);
		}
		return r;
	}
	
	/**
	 * Invalidates this element and any of its ancestors or descendants.
	 * <p>
	 * This method is used on the topmost AST element that was changed on editing, as any ancestors and descendants may be created anew or changed (in particular, on the
	 * {@link ASTBrokkrFile} or {@link ASTModule} if the whole file was modified somehow, e.g. deleted or reloaded).
	 * <p>
	 * TODO maybe, instead of just assuming this behaviour, the elements actually affected on a change should just be unlinked (still unlinking a whole subtree at once though)
	 */
	default void invalidateSubtreeAndParents() {
		invalidateSubtree();
		invalidateParents();
	}
	
	/**
	 * Invalidates this element only.
	 * <p>
	 * Invalidating is currently used to unlink any links in this element to and from other elements.
	 */
	void invalidateSelf();
	
	/**
	 * Invalidates this element and any descendants.
	 */
	default void invalidateSubtree() {
		invalidateSelf();
		for (final ASTElementPart part : parts()) {
			if (part instanceof ASTElement)
				((ASTElement) part).invalidateSubtree();
		}
		for (final ASTLink<?> link : links())
			link.invalidate();
	}
	
	/**
	 * Invalidates any ancestors of this element.
	 */
	default void invalidateParents() {
		final ASTElement parent = parent();
		if (parent != null) {
			parent.invalidateSelf();
			parent.invalidateParents();
		}
	}
	
}
