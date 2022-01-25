package invtweaks.forge;

import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.*;

public class InputEventHandler {

    static int modifierMask = 0;
    static int[] lastMouseAction = new int[8];
    static HashMap<Integer, Integer> lastKeyAction = new HashMap<>();


    public static boolean isShiftDown(){
        return (modifierMask & GLFW_MOD_SHIFT) != 0;
    }
    public static boolean isControlDown(){
        return (modifierMask & GLFW_MOD_CONTROL) != 0;
    }
    public static boolean isAltDown(){
        return (modifierMask & GLFW_MOD_ALT) != 0;
    }

    /**
     * *  Values:
     * @see GLFW#GLFW_MOUSE_BUTTON_LEFT
     * @see GLFW#GLFW_MOUSE_BUTTON_RIGHT
     * @see GLFW#GLFW_MOUSE_BUTTON_MIDDLE
     * @see GLFW#GLFW_MOUSE_BUTTON_4
     * @see  GLFW#GLFW_MOUSE_BUTTON_5
     * @see GLFW#GLFW_PRESS
     * @see GLFW#GLFW_RELEASE
     * @see GLFW#GLFW_MOD_SHIFT
     * @see GLFW#GLFW_MOD_CONTROL
     * @see GLFW#GLFW_MOD_ALT
     * @see GLFW#GLFW_MOD_SUPER
     * */
    @SubscribeEvent
    public static void onMouse(InputEvent.MouseInputEvent event){
        //TODO: listen to input
        lastMouseAction[event.getButton()] = event.getAction();
        modifierMask = event.getModifiers();
    }

    public static boolean isMiddleDown(){
        return lastMouseAction[GLFW_MOUSE_BUTTON_MIDDLE] == GLFW_PRESS;
    }
    public static boolean isLeftDown(){
        return lastMouseAction[GLFW_MOUSE_BUTTON_LEFT] == GLFW_PRESS;
    }
    public static boolean isRightDown(){
        return lastMouseAction[GLFW_MOUSE_BUTTON_RIGHT] == GLFW_PRESS;
    }

    @SubscribeEvent
    public static void onKey(InputEvent.KeyInputEvent event){
        //TODO: listen to input
        lastKeyAction.put(event.getKey(), event.getAction());
        modifierMask = event.getModifiers();
    }
}
