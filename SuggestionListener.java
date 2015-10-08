
/**
 * Responds to when the user picks a suggestion from the
 * AutoSuggestionTextField or when 'enter/return' is pressed.
 * 
 * @author David X. Wang
 *
 * @param <E> The type of object which will be selected.
 */
public interface SuggestionListener<E> {
        public void onSuggestionSelected(E item);
        public void onEnter(String query);
}
