package util;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class Sorter
{	
	public static <K,V extends Comparable<V>> List<Entry<K, V>> sortEntrySet(Set<Entry<K, V>> set, final Boolean cresc)
	{
		List<Entry<K, V>> sortedList = new LinkedList<Entry<K, V>>(set);
		Collections.sort(sortedList, new Comparator<Entry<K, V>>()
        {
            public int compare(Entry<K, V> o1, Entry<K, V> o2)
            {
            	if(cresc)
            	{
            		return o1.getValue().compareTo(o2.getValue());
            	}
            	else
            	{
            		return o2.getValue().compareTo(o1.getValue());
            	}
            }
        });
		return sortedList;
	}
	
	public static <K,V extends Comparable<V>> List<Entry<K, V>> sortEntrySet(Set<Entry<K, V>> set)
	{
		return sortEntrySet(set, true);
	}
}
