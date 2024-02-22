package org.task.searadar.mr231_3.convert;

import org.apache.camel.Exchange;
import ru.oogis.searadar.api.convert.SearadarExchangeConverter;
import ru.oogis.searadar.api.message.InvalidMessage;
import ru.oogis.searadar.api.message.RadarSystemDataMessage;
import ru.oogis.searadar.api.message.SearadarStationMessage;
import ru.oogis.searadar.api.message.TrackedTargetMessage;
import ru.oogis.searadar.api.types.IFF;
import ru.oogis.searadar.api.types.TargetStatus;
import ru.oogis.searadar.api.types.TargetType;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Парсер сообщений по протоколу МР-231-3.
 *
 * @author Дмитрий Самородов
 */
public class Mr231_3Converter implements SearadarExchangeConverter {
    /**
     * Массив корректных значений для шкалы дальности
     */
    private static final Double[] DISTANCE_SCALE = {0.125, 0.25, 0.5, 1.5, 3.0, 6.0, 12.0, 24.0, 48.0, 96.0};
    /**
     * Поля сообщения
     */
    private String[] fields;
    /**
     * Тип передаваемого сообщения
     */
    private String msgType;


    /**
     * <p>Реализация метода {@link SearadarExchangeConverter#convert(Exchange)}.</p>
     *
     * @param exchange контейнер, который хранит входное сообщение
     * @return возвращает список сообщений, вызвав метод {@link Mr231_3Converter#convert(String)},
     * в который передается текст входного сообщения
     */
    @Override
    public List<SearadarStationMessage> convert(Exchange exchange) {
        return convert(exchange.getIn().getBody(String.class));
    }

    /**
     * <p>Данный метод преобразует сообщение в список объектов класса {@link SearadarStationMessage}.</p>
     * <p>Сначала происходит инициализация переменных объекта, вызвав метод {@link Mr231_3Converter#readFields(String)}.</p>
     * <p>После, создается объект класса {@link TrackedTargetMessage}
     * или {@link TrackedTargetMessage} в зависимости от типа сообщения.</p>
     * <p>Объекты необходимого класса создаются с помощью методов {@link Mr231_3Converter#getTTM()}
     * и {@link Mr231_3Converter#getRSD()}.</p>
     * <p>В случае, если тип сообщения {@link RadarSystemDataMessage}, метод проверяет корректность сообщения,
     * в случае, если сообщение некорректно, он добавляет в список объект класса {@link InvalidMessage}</p>
     *
     * @param message сообщение, которое нужно конвертировать в объект класса {@link SearadarStationMessage}
     * @return список объектов класса {@link SearadarStationMessage}
     */
    public List<SearadarStationMessage> convert(String message) {
        List<SearadarStationMessage> msgList = new ArrayList<>();

        readFields(message);

        switch (msgType) {
            case "TTM":
                msgList.add(getTTM());
                break;
            case "RSD":
                RadarSystemDataMessage rsd = getRSD();
                InvalidMessage invalidRSD = checkRSD(rsd);

                if (invalidRSD == null)
                    msgList.add(rsd);
                else
                    msgList.add(invalidRSD);
        }
        return msgList;
    }

    /**
     * <p>Данный метод инициализирует переменные объекта.</p>
     * <p>Согласно протоколу МР-321-3, первые 3 символа предложения всегда соответствуют "$RA",
     * а далее следует идентификатор передаваемых данных, и прочие поля, разделённые символом ",",
     * "*" - последний символ, являющийся разграничителем между информацией и контрольной суммой.</p>
     * <p>Исходя из этого, реализация метода работает так:</p>
     * <p>1. Создается переменная типа String, в которую передаётся подстрока переданного сообщения,
     * от 4 символа до "*" не включительно;
     * 2. Инициализируется {@link Mr231_3Converter#fields}: передаются подстроки по разделителю ",";
     * 3. {@link Mr231_3Converter#msgType} инициализируется первым полем</p>
     *
     * @param msg сообщение
     */
    private void readFields(String msg) {
        String temp = msg.substring(3, msg.indexOf("*")).trim();

        fields = temp.split(Pattern.quote(","));
        msgType = fields[0];
    }

    /**
     * <p>Данный метод создает объект класса {@link TrackedTargetMessage},
     * инициализирует все его переменные в соответствии с протоколом МР-231-3.</p>
     * <p>{@link Mr231_3Converter#fields} хранит сигналы [3-18] (см. Протокол МР-231-3).</p>
     *
     * @return объект класса {@link TrackedTargetMessage}, содержащий сигналы из {@link Mr231_3Converter#fields}
     */
    private TrackedTargetMessage getTTM() {
        TrackedTargetMessage ttm = new TrackedTargetMessage();

        ttm.setMsgTime(System.currentTimeMillis());
        TargetStatus status = TargetStatus.UNRELIABLE_DATA;
        IFF iff = IFF.UNKNOWN;
        TargetType type = TargetType.UNKNOWN;

        switch (fields[11]) {
            case "b":
                iff = IFF.FRIEND;
                break;
            case "p":
                iff = IFF.FOE;
                break;
            case "d":
                iff = IFF.UNKNOWN;
        }

        switch (fields[12]) {
            case "L":
                status = TargetStatus.LOST;
                break;
            case "Q":
                status = TargetStatus.UNRELIABLE_DATA;
                break;
            case "T":
                status = TargetStatus.TRACKED;
        }

        ttm.setMsgRecTime(new Timestamp(System.currentTimeMillis()));
        ttm.setTargetNumber(Integer.parseInt(fields[1]));
        ttm.setDistance(Double.parseDouble(fields[2]));
        ttm.setBearing(Double.parseDouble(fields[3]));
        ttm.setSpeed(Double.parseDouble(fields[5]));
        ttm.setCourse(Double.parseDouble(fields[6]));
        ttm.setStatus(status);
        ttm.setIff(iff);
        ttm.setType(type);

        return ttm;
    }

    /**
     * <p>Данный метод создаёт объект класса {@link RadarSystemDataMessage},
     * инициализирует все его переменные в соответствии с протоколом МР-231-3.</p>
     * <p>{@link Mr231_3Converter#fields} хранит сигналы [3-17] (см. Протокол МР-231-3).</p>
     *
     * @return объект класса {@link RadarSystemDataMessage}, содержащий сигналы из {@link Mr231_3Converter#fields}
     */
    private RadarSystemDataMessage getRSD() {
        RadarSystemDataMessage rsd = new RadarSystemDataMessage();

        rsd.setMsgRecTime(new Timestamp(System.currentTimeMillis()));
        rsd.setInitialDistance(Double.parseDouble(fields[1]));
        rsd.setInitialBearing(Double.parseDouble(fields[2]));
        rsd.setMovingCircleOfDistance(Double.parseDouble(fields[3]));
        rsd.setBearing(Double.parseDouble(fields[4]));
        rsd.setDistanceFromShip(Double.parseDouble(fields[9]));
        rsd.setBearing2(Double.parseDouble(fields[10]));
        rsd.setDistanceScale(Double.parseDouble(fields[11]));
        rsd.setDistanceUnit(fields[12]);
        rsd.setDisplayOrientation(fields[13]);
        rsd.setWorkingMode(fields[14]);

        return rsd;
    }

    /**
     * <p>Данный метод проверяет корректность параметра "Шкала дальности".</p>
     * <p>Если значение шкалы дальности объекта не содержится в массиве {@link Mr231_3Converter#DISTANCE_SCALE},
     * то значение считается некорректным.</p>
     *
     * @param rsd объект класса {@link RadarSystemDataMessage}, содержащий информацию о значении шкалы дальности
     * @return <p>null: если значение шкалы дальности корректное;</p>
     * <p>Объект класса {@link InvalidMessage}: если значение шкалы дальности некорректное</p>
     */
    private InvalidMessage checkRSD(RadarSystemDataMessage rsd) {

        InvalidMessage invalidMessage = new InvalidMessage();
        String infoMsg = "";

        if (!Arrays.asList(DISTANCE_SCALE).contains(rsd.getDistanceScale())) {
            infoMsg = "RSD message. Wrong distance scale value: " + rsd.getDistanceScale();
            invalidMessage.setInfoMsg(infoMsg);
            return invalidMessage;
        }

        return null;
    }
}
