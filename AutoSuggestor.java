import java.util.List;

/**
 * Provides suggestions for the AutoSuggestionTextField.
 * 
 * @author David X. Wang
 *
 * @param <E> The type of object which is returned, must have a display-friendly toString method.
 */
public interface AutoSuggestor<E> {
        public List<E> getSuggestions(String query);
}
