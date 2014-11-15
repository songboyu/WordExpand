 
package com.seal.rank;

public class PageRanker extends GraphRanker {
  
  public static final String DESCRIPTION = "PageRank";
  public static final boolean USE_RESTART = false;
  public static final boolean USE_RELATION = false;
  public static final double DAMPER = 0.85;
  
  public PageRanker() {
    super();
    setDescription(DESCRIPTION);
    setUseRestart(USE_RESTART);
    setUseRelation(USE_RELATION);
    setDamper(DAMPER);
  }
  
  /*public void reset() {
    super.clear();
    setUseRestart(USE_RESTART);
    setUseRelation(USE_RELATION);
    setDamper(DAMPER);
  }*/
}
