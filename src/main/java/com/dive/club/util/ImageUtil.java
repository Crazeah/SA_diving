package com.dive.club.util;

import org.springframework.stereotype.Component;

@Component
public class ImageUtil {

    public static final String DEFAULT_ACTIVITY_IMAGE = "/images/default/diving-default.jpg";
    public static final String DEFAULT_SCUBA_IMAGE = "/images/default/scuba-diving.jpg";
    public static final String DEFAULT_FREE_DIVING_IMAGE = "/images/default/free-diving.jpg";
    public static final String DEFAULT_SNORKELING_IMAGE = "/images/default/snorkeling.jpg";
    public static final String DEFAULT_TRAINING_IMAGE = "/images/default/training.jpg";
    
    public static String getDefaultImageByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return DEFAULT_ACTIVITY_IMAGE;
        }
        
        String normalizedCategory = category.toLowerCase().trim();
        
        switch (normalizedCategory) {
            case "水肺潛水":
            case "scuba":
            case "fun_dive":
                return DEFAULT_SCUBA_IMAGE;
                
            case "自由潛水":
            case "freediving":
                return DEFAULT_FREE_DIVING_IMAGE;
                
            case "浮潛":
            case "snorkeling":
                return DEFAULT_SNORKELING_IMAGE;
                
            case "進階訓練":
            case "訓練":
            case "course":
            case "潛水訓練":
                return DEFAULT_TRAINING_IMAGE;
                
            default:
                return DEFAULT_ACTIVITY_IMAGE;
        }
    }
}