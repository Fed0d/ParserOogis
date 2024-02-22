package org.task.searadar.mr231_3.station;

import org.task.searadar.mr231_3.convert.Mr231_3Converter;

public class Mr231_3StationType {
    private static final String STATION_TYPE = "МР-231-3";
    private static final String CODEC_NAME = "mr2313";
    public Mr231_3Converter createConverter() {
        return new Mr231_3Converter();
    }
}
