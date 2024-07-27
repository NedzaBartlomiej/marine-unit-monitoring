package pl.bartlomiej.marineunitmonitoring.common.util;

import org.springframework.beans.BeanUtils;

import java.lang.reflect.InvocationTargetException;

final public class MapperUtil {

    private MapperUtil() { // todo replace with ModelMapper library
    }

    public static <T> T copyProperties(Object source, Class<T> targetClass) {
        T target;
        try {
            target = targetClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        BeanUtils.copyProperties(source, target);
        return target;
    }
}
