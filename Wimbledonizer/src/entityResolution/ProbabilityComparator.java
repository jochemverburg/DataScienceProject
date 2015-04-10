package entityResolution;

import java.util.Comparator;
import edu.stanford.nlp.util.Pair;

public class ProbabilityComparator<E> implements Comparator<Pair<E, Double>>{
		
		/**
		 * Returns -1 if arg0>arg1 and 1 if arg0<arg1. A sortedList would go from high to low in this case.
		 */
		@Override
		public int compare(Pair<E, Double> arg0, Pair<E, Double> arg1) {

			double p1 = arg0.second();
			double p2 = arg1.second();
			if(p1>p2) return -1;
			else if(p1<p2) return 1;
			else return 0;
		}
}