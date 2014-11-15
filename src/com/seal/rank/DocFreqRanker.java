 
package com.seal.rank;

import java.util.HashSet;
import java.util.Set;

import com.seal.expand.Entity;
import com.seal.expand.EntityList;
import com.seal.expand.Wrapper.EntityLiteral;
import com.seal.fetch.Document;
import com.seal.fetch.DocumentSet;

public class DocFreqRanker extends Ranker {
  
  public static final String DESCRIPTION = "Extracted Document Frequency";
  
  private Set<Integer> contentHashSet;
  
  public DocFreqRanker() {
    super();
    setDescription(DESCRIPTION);
    contentHashSet = new HashSet<Integer>();
  }
  
  public void load(EntityList entities, DocumentSet documentSet) {
    for (Document document : documentSet) {
      for (EntityLiteral content : document.getExtractions()) {
        int hashCode = getHashCode(document, content, null);
        if (contentHashSet.add(hashCode)) {
//          entityDist.add(content, 1);
          Entity entity = entities.add(content);
          if (entity == null) continue;
          entity.addWeight(getRankerID(), 1);
        }
      }
    }
    entities.reduceSize(getRankerID());
  }
}
