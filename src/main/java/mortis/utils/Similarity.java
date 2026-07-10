package mortis.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Similarity {

    public static double tokenOverlap(String a, String b) {
        Set<String> tokensA = new HashSet<>(Arrays.asList(a.toLowerCase().split("[_\\s]+")));
        Set<String> tokensB = new HashSet<>(Arrays.asList(b.toLowerCase().split("[_\\s]+")));

        Set<String> intersection = new HashSet<>(tokensA);
        intersection.retainAll(tokensB);

        Set<String> union = new HashSet<>(tokensA);
        union.addAll(tokensB);

        if (union.isEmpty()) return 0.0;
        return (double) intersection.size() / union.size(); 
    }
}