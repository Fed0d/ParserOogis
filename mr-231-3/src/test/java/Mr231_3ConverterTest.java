import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.task.searadar.mr231_3.convert.Mr231_3Converter;
import ru.oogis.searadar.api.message.InvalidMessage;
import ru.oogis.searadar.api.message.RadarSystemDataMessage;
import ru.oogis.searadar.api.message.SearadarStationMessage;
import ru.oogis.searadar.api.message.TrackedTargetMessage;
import ru.oogis.searadar.api.types.IFF;
import ru.oogis.searadar.api.types.TargetStatus;
import ru.oogis.searadar.api.types.TargetType;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;

@DisplayName("Mr231_3Converter Test")
public class Mr231_3ConverterTest {
    private static final Mr231_3Converter converter = new Mr231_3Converter();
    private static List<SearadarStationMessage> msgList;

    private static Stream<Arguments> messageAndType() {
        TrackedTargetMessage ttm = new TrackedTargetMessage();
        ttm.setTargetNumber(66);
        ttm.setDistance(28.71);
        ttm.setBearing(341.1);
        ttm.setSpeed(57.6);
        ttm.setCourse(024.5);
        ttm.setIff(IFF.FRIEND);
        ttm.setStatus(TargetStatus.LOST);
        ttm.setType(TargetType.UNKNOWN);

        RadarSystemDataMessage rsd = new RadarSystemDataMessage();
        rsd.setInitialDistance(36.5);
        rsd.setInitialBearing(331.4);
        rsd.setMovingCircleOfDistance(8.4);
        rsd.setBearing(320.6);
        rsd.setDistanceFromShip(11.6);
        rsd.setBearing2(185.3);
        rsd.setDistanceScale(96.0);
        rsd.setDistanceUnit("N");
        rsd.setDisplayOrientation("N");
        rsd.setWorkingMode("S");

        return Stream.of(
                Arguments.of("$RATTM,66,28.71,341.1,T,57.6,024.5,T,0.4,4.1,N,b,L,,457362,А*42", ttm),
                Arguments.of("$RARSD,36.5,331.4,8.4,320.6,,,,,11.6,185.3,96.0,N,N,S*33", rsd)
        );
    }

    @ParameterizedTest
    @MethodSource("messageAndType")
    @DisplayName("Convert Message Correct Type Test")
    void convertMessageTypeTest(String message, SearadarStationMessage expectedType) {
        msgList = converter.convert(message);

        if (msgList.get(0) instanceof TrackedTargetMessage) {
            expectedType.setMsgRecTime(msgList.get(0).getMsgRecTime());
            ((TrackedTargetMessage) expectedType).setMsgTime(((TrackedTargetMessage) msgList.get(0)).getMsgTime());
            assertThat(msgList.get(0).toString()).isEqualTo(expectedType.toString());
        } else if (msgList.get(0) instanceof RadarSystemDataMessage) {
            expectedType.setMsgRecTime(msgList.get(0).getMsgRecTime());
            assertThat(msgList.get(0).toString()).isEqualTo(expectedType.toString());
        }
    }

    @ParameterizedTest
    @CsvSource({"'$RAVHW,115.6,T,,,46.0,N,,*71'", "'$RARSDA,115.6,T,,,46.0,N,,*71'"})
    @DisplayName("Convert Message Incorrect Type Test")
    void convertMessageTypeTest(String message) {
        msgList = converter.convert(message);

        assertThat(msgList).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({"'$RARSD,14.0,0.0,96.9,306.4,,,,,97.7,11.6,0.3,K,N,S*20'"})
    @DisplayName("Invalid Message Test")
    void invalidMessageTest(String message) {
        msgList = converter.convert(message);

        assertThat(msgList.get(0).getClass()).isEqualTo(InvalidMessage.class);
    }
}