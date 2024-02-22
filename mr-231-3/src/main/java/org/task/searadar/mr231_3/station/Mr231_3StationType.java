package org.task.searadar.mr231_3.station;

import org.task.searadar.mr231_3.convert.Mr231_3Converter;
import ru.oogis.searadar.api.message.SearadarStationMessage;

/**
 * Класс, который хранит основную информацию о НРЛС МР-231-3.
 *
 * @author Дмитрий Саомородов
 */
public class Mr231_3StationType {
    /**
     * Тип станции
     */
    private static final String STATION_TYPE = "МР-231-3";
    /**
     * Название кодека
     */
    private static final String CODEC_NAME = "mr2313";

    /**
     * Данный метод создаёт конвертер, преобразующий строку в объект класса {@link SearadarStationMessage}.
     *
     * @return объект класса {@link Mr231_3Converter}
     */
    public Mr231_3Converter createConverter() {
        return new Mr231_3Converter();
    }
}
