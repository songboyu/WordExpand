package com.seal.expand;

import com.seal.rank.Ranker.Feature;


public abstract class Pinniped extends SetExpander {
    public abstract void setEngine(int useEngine);
    public abstract void setFeature(Feature feature);
    public abstract boolean expand(EntityList wrapperSeeds, EntityList pageSeeds, String hint);
}
