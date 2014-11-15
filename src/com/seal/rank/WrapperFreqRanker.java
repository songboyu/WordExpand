 
package com.seal.rank;

import java.util.HashSet;
import java.util.Set;

import com.seal.expand.Entity;
import com.seal.expand.EntityList;
import com.seal.expand.Wrapper;
import com.seal.expand.Wrapper.EntityLiteral;
import com.seal.fetch.Document;
import com.seal.fetch.DocumentSet;

public class WrapperFreqRanker extends Ranker {
  
  public static final String DESCRIPTION = "Extracted Wrapper Frequency";
  
  private Set<Integer> contentHashSet;
  
  public WrapperFreqRanker() {
    super();
    setDescription(DESCRIPTION);
    contentHashSet = new HashSet<Integer>();
  }
  
  public void load(EntityList entities, DocumentSet documentSet) {
    for (Document document : documentSet) {
      for (Wrapper wrapper : document.getWrappers()) {
          // kmr 5 June 2012 converted for EntityLiteral
        for (EntityLiteral content : wrapper.getContents()) {
          int hashCode = getHashCode(document, wrapper, content);
          if (contentHashSet.add(hashCode)) {
//            entityDist.add(content, 1);
            Entity entity = entities.add(content);
            if (entity == null) continue;
            entity.addWeight(getRankerID(), 1);
          }
        }
      }
    }
    entities.reduceSize(getRankerID());
  }
}
