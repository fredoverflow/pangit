import java.awt.event.InputEvent;

public class Platform {
    public static final boolean isMacintosh = System.getProperty("os.name").toLowerCase().contains("mac");

    public static final int CTRL_RESPECTIVELY_META = isMacintosh ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;

    public static boolean isControlRespectivelyCommandDown(InputEvent event) {
        return (event.getModifiersEx() & CTRL_RESPECTIVELY_META) != 0;
    }
}
