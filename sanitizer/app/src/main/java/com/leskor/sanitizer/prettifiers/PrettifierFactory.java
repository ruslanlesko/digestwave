package com.leskor.sanitizer.prettifiers;

import com.leskor.sanitizer.prettifiers.sites.*;

import java.util.HashMap;
import java.util.Map;

public class PrettifierFactory {
    private static final Prettifier DEFAULT_PRETTIFIER = new SimplePrettifier();

    private final Map<String, Prettifier> prettifierMap;

    public PrettifierFactory() {
        prettifierMap = new HashMap<>();
        prettifierMap.put("FIN", new FinanceUaPrettifier());
        prettifierMap.put("MFN", new MinfinUaPrettifier());
        prettifierMap.put("LIG", new LigaNetPrettifier());
        prettifierMap.put("EPR", new EpravdaUaPrettifier());
        prettifierMap.put("NV", new NVUaPrettifier());
        prettifierMap.put("KDR", new KeddrComPrettifier());
        prettifierMap.put("GGT", new GagadgetComPrettifier());
    }

    public Prettifier createPrettifier(String siteCode) {
        return prettifierMap.getOrDefault(siteCode, DEFAULT_PRETTIFIER);
    }
}
