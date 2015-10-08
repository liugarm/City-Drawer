import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LabelAutoSuggestor implements AutoSuggestor<String> {
	private static List<String> suburbs;
	private static String[] SUBURBS = null;

	public LabelAutoSuggestor(List<String> suburbs) {
		this.suburbs = suburbs;
	}

	@Override
	public List<String> getSuggestions(String query) {
		if (query.isEmpty())
			return Collections.<String> emptyList();
		List<String> matches = new ArrayList<String>();
		for (String sub : suburbs) {
			if (sub.toLowerCase().startsWith(query.toLowerCase()))
				matches.add(sub);
		}

		return matches;
	}

	public static String suburbList() {
		int i = 0;
		StringBuilder ans = new StringBuilder();
		for (String s : suburbs) {
			ans.append(s).append(", ");
			if (i % 3 == 2) {
				ans.append("\n");
			}
			i++;
		}
		return ans.toString();
	}

	/*
	 * //bunch of Wellington suburbs public final static String[] SUBURBS = new
	 * String[]{ "Broadmeadows", "Churton Park", "Glenside", "Grenada",
	 * "Grenada North", "Horokiwi", "Johnsonville", "Khandallah", "Newlands",
	 * "Ohariu", "Paprangi", "Tawa", "Takapu Valley", "Woodridge", "Karori",
	 * "Northland", "Crofton Downs", "Kaiwharawhara", "Ngaio", "Ngauranga",
	 * "Makara", "Makara Beach", "Wadestown", "Wilton", "Brooklyn",
	 * "Aro Valley", "Kelburn", "Mount Victoria", "Oriental Bay", "Te Aro",
	 * "Thorndon", "Highbury", "Pipitea", "Berhampore", "Island Bay", "Newtown",
	 * "Vogeltown", "Houghton Bay", "Kingston", "Mornington", "Mount Cook",
	 * "Owhiro Bay", "Southgate", "Hataitai", "Lyall Bay", "Kilbirnie",
	 * "Miramar", "Seatoun", "Breaker Bay", "Karaka Bays", "Maupuia", "Melrose",
	 * "Moa Point", "Rongotai", "Roseneath", "Strathmore"};
	 */

}
